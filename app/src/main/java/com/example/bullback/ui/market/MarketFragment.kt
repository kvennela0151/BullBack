package com.example.bullback.ui.market

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bullback.R
import com.example.bullback.data.model.auth.CommodityData
import com.example.bullback.data.model.websocket.AppWebSocketManager
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.MarketApi
import com.example.bullback.data.repository.AuthRepository
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MarketFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView

    private lateinit var indicesAdapter: CommodityAdapter
    private lateinit var commodityAdapter: CommodityAdapter
    private lateinit var cryptoAdapter: CommodityAdapter

    private lateinit var authRepository: AuthRepository
    private lateinit var marketApi: MarketApi

    private lateinit var progressBar: View


    private var currentTokens: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_market, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        progressBar = view.findViewById(R.id.progressBar)

        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository.getInstance(requireContext())
        marketApi = RetrofitClient.createService(MarketApi::class.java)

        tabLayout = view.findViewById(R.id.tabLayout)
        recyclerView = view.findViewById(R.id.rvMarketOverview)

        indicesAdapter = CommodityAdapter(emptyList())
        commodityAdapter = CommodityAdapter(emptyList())
        cryptoAdapter = CommodityAdapter(emptyList())

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = indicesAdapter

        setupTabs()
    }

    override fun onStart() {
        super.onStart()

        AppWebSocketManager.setMarketListener { json ->
            if (json.optString("type") != "market_data") return@setMarketListener

            val data = json.optJSONObject("data") ?: return@setMarketListener

            val token: String
            val ltp: Double
            val change: Double
            val changePercent: Double

            if (data.has("Token")) {
                // Indices / Commodities
                token = data.optLong("Token").toString()
                ltp = data.optDouble("LTP")
                val close = data.optDouble("C")

                val diff = ltp - close
                change = diff
                changePercent = if (close != 0.0) (diff / close) * 100 else 0.0
            } else {
                // Crypto
                token = data.optString("instrument_token")
                ltp = data.optDouble("LTP")
                change = data.optDouble("change")
                changePercent = data.optDouble("change_percent")
            }

            requireActivity().runOnUiThread {
                when (AppWebSocketManager.currentTab) {
                    "indices" ->
                        indicesAdapter.updateLivePrice(token, ltp, change, changePercent)

                    "commodities" ->
                        commodityAdapter.updateLivePrice(token, ltp, change, changePercent)

                    "crypto" ->
                        cryptoAdapter.updateLivePrice(token, ltp, change, changePercent)
                }
            }
        }

    }

    override fun onStop() {
        super.onStop()
        AppWebSocketManager.setMarketListener(null)
    }

    /* ---------------- Tabs ---------------- */

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadIndices()
                    1 -> loadCommodities()
                    2 -> loadCrypto()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        loadIndices()
    }

    /* ---------------- Loaders ---------------- */

    private fun loadIndices() {
        AppWebSocketManager.currentTab = "indices"
        recyclerView.adapter = indicesAdapter

        val data = listOf(
            createStatic("NIFTY 50", "256265"),
            createStatic("BANKNIFTY", "260105"),
            createStatic("SENSEX", "265"),
            createStatic("FINNIFTY", "257801"),
            createStatic("NIFTYNXT50", "270857"),
            createStatic("INDIAVIX", "264969")
        )

        indicesAdapter.updateData(data)
        subscribeWebSocket(data.map { it.instrumentToken })

    }

    private fun loadCommodities() {
        AppWebSocketManager.currentTab = "commodities"
        recyclerView.adapter = commodityAdapter

        lifecycleScope.launch {
            val token = authRepository.getTokenSync()
            if (token.isNullOrEmpty()) {
                showError("Session expired")
                return@launch
            }

            try {
                val response = marketApi.getTopCommodities(token)
                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()!!.data
                    commodityAdapter.updateData(data)
                    subscribeWebSocket(data.map { it.instrumentToken })
                } else {
                    showError("Failed to load commodities")
                }
            } catch (e: Exception) {
                showError("Network error")
            }
        }
    }

    private fun loadCrypto() {
        AppWebSocketManager.currentTab = "crypto"
        recyclerView.adapter = cryptoAdapter

        val data = listOf(
            createStatic("BTCUSDT", "BTCUSDT"),
            createStatic("ETHUSDT", "ETHUSDT"),
            createStatic("SOLUSDT", "SOLUSDT"),
            createStatic("BNBUSDT", "BNBUSDT"),
            createStatic("DOGEUSD", "DOGEUSD"),
            createStatic("XRPUSD", "XRPUSD")
        )

        cryptoAdapter.updateData(data)
        subscribeWebSocket(data.map { it.instrumentToken })
    }

    /* ---------------- WebSocket ---------------- */

    private fun subscribeWebSocket(tokens: List<String>) {
        if (tokens.isEmpty() || tokens == currentTokens) return

        currentTokens = tokens

        val message = JSONObject().apply {
            put("type", "subscribe")
            put("tokens", JSONArray(tokens))
        }

        AppWebSocketManager.sendMessage(message.toString())
        Log.d("WS_SUBSCRIBE", tokens.joinToString())
    }

    /* ---------------- Utils ---------------- */

    private fun createStatic(name: String, token: String): CommodityData {
        return CommodityData(
            instrumentToken = token,
            exchangeToken = "",
            tradingSymbol = name,
            name = name,
            lastPrice = 0.0,
            change = 0.0,
            changePercent = 0.0,
            expiry = "",
            strike = 0.0,
            tickSize = 0.0,
            lotSize = 0,
            instrumentType = "",
            segment = "",
            exchange = ""
        )
    }

    private fun showError(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun showLoader() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun hideLoader() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }


}



