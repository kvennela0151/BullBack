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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bullback.MainActivity
import com.example.bullback.R
import com.example.bullback.data.model.auth.CommodityData
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.MarketApi
import com.example.bullback.data.repository.AuthRepository
import com.example.bullback.ui.orders.OrdersFragment
import com.example.bullback.utlis.Resource
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MarketFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var rvMarketOverview: RecyclerView
    private lateinit var commodityAdapter: CommodityAdapter
    private lateinit var authRepository: AuthRepository
    private lateinit var marketApi: MarketApi

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_market, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository.getInstance(requireContext())
        marketApi = RetrofitClient.createService(MarketApi::class.java)

        tabLayout = view.findViewById(R.id.tabLayout)
        rvMarketOverview = view.findViewById(R.id.rvMarketOverview)

        // Set RecyclerView to 2-column grid
        commodityAdapter = CommodityAdapter(emptyList())
        rvMarketOverview.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // 2 columns
            adapter = commodityAdapter
        }

        setupTabLayout()
    }

    private fun initViews(view: View) {
        tabLayout = view.findViewById(R.id.tabLayout)
        rvMarketOverview = view.findViewById(R.id.rvMarketOverview)
    }

    private fun setupRecyclerView() {
        commodityAdapter = CommodityAdapter(emptyList())
        rvMarketOverview.layoutManager = LinearLayoutManager(requireContext())
        rvMarketOverview.adapter = commodityAdapter
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showStaticIndicesData()
                    1 -> fetchCommoditiesData() // REAL API CALL
                    2 -> showStaticCryptoData()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        showStaticIndicesData()
    }

    // ===================== REAL API CALL =====================

    private fun fetchCommoditiesData() {
        lifecycleScope.launch {
            try {
                val token = authRepository.getTokenSync()

                if (token.isNullOrEmpty()) {
                    showError("Token missing. Please login again.")
                    return@launch
                }

                Log.d("MarketFragment", "Using token: $token")

                val response = marketApi.getTopCommodities(token)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status) {
                        commodityAdapter.updateData(body.data)
                    } else {
                        showError("Empty data")
                    }
                } else {
                    showError("API Error: ${response.code()}")
                    Log.e("MarketFragment", "Error body: ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                showError("Network error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // ===================== STATIC DATA =====================

    private fun showStaticIndicesData() {
        val staticIndices = listOf(
            createStaticCommodity("NIFTY 50", 19435.50),
            createStaticCommodity("SENSEX", 64958.20),
            createStaticCommodity("BANKNIFTY", 43850.25)
        )
        commodityAdapter.updateData(staticIndices)
    }

    private fun showStaticCryptoData() {
        val staticCrypto = listOf(
            createStaticCommodity("BITCOIN", 42500.00),
            createStaticCommodity("ETHEREUM", 2250.50),
            createStaticCommodity("CARDANO", 0.52)
        )
        commodityAdapter.updateData(staticCrypto)
    }

    private fun createStaticCommodity(
        name: String,
        price: Double
    ): CommodityData {
        return CommodityData(
            instrumentToken = 0L,
            exchangeToken = "",
            tradingSymbol = name,
            name = name,
            lastPrice = price,
            expiry = "",
            strike = 0.0,
            tickSize = 0.0,
            lotSize = 0,
            instrumentType = "",
            segment = "",
            exchange = ""
        )
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}