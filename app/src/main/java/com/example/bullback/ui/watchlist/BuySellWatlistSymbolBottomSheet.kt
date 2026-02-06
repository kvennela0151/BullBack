package com.example.bullback.ui.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bullback.databinding.FragmentBuySellWatlistSymbolBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.core.content.ContextCompat
import com.example.bullback.R
import com.example.bullback.data.model.websocket.AppWebSocketManager
import org.json.JSONObject
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.bullback.data.model.placeorders.PlaceOrderRequest
import com.example.bullback.data.repository.OrdersRepository
import com.example.bullback.utlis.Resource
import kotlinx.coroutines.launch

class BuySellWatlistSymbolBottomSheet(
    private val symbol: String,
    private val segment: String,
    private var ltp: Double,
    private val orderType: String, // "BUY" or "SELL"
    private val token: String,
    private val exchange: String
) : BottomSheetDialogFragment() {

    private var _binding: FragmentBuySellWatlistSymbolBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var isSubscribed = false
    private val ordersRepository = OrdersRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuySellWatlistSymbolBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSymbol.text = symbol
        binding.tvSegment.text = segment
        binding.tvLtp.text = String.format("%.2f", ltp)
        binding.etPrice.setText(ltp.toString())

        subscribeToUpdates()

        binding.switchSL.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutStoplossTarget.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> { // Market tab
                        binding.etPrice.isEnabled = false
                        binding.priceLayout.isEndIconVisible = true
                    }
                    1 -> { // Limit tab
                        binding.etPrice.isEnabled = true
                        binding.priceLayout.isEndIconVisible = false
                    }
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        binding.btnPlaceOrder.text = if (orderType == "BUY") "Buy" else "Sell"
        binding.btnPlaceOrder.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            if (orderType == "BUY") R.color.button_green_dark else R.color.red
        )

        binding.btnPlaceOrder.setOnClickListener {
            placeOrder()
        }

        binding.btnBack.setOnClickListener {
            dismiss()
        }
    }

    private fun placeOrder() {
        val priceText = binding.etPrice.text.toString()
        val lotsText = binding.etLots.text.toString()

        if (lotsText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter lots", Toast.LENGTH_SHORT).show()
            return
        }

        val lots = lotsText.toDoubleOrNull()
        if (lots == null || lots <= 0) {
            Toast.makeText(requireContext(), "Please enter valid lots", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedOrderType = when (binding.tabLayout.selectedTabPosition) {
            0 -> "MARKET"
            1 -> "LIMIT"
            else -> "MARKET"
        }

        val price: Double? = if (selectedOrderType == "LIMIT") {
            val parsedPrice = priceText.toDoubleOrNull()
            if (parsedPrice == null || parsedPrice <= 0) {
                Toast.makeText(requireContext(), "Please enter valid price", Toast.LENGTH_SHORT).show()
                return
            }
            parsedPrice
        } else {
            null
        }

        val stoploss = if (binding.switchSL.isChecked) {
            binding.etStoploss.text.toString().toDoubleOrNull()
        } else null

        val target = if (binding.switchSL.isChecked) {
            binding.etTarget.text.toString().toDoubleOrNull()
        } else null

        val lotSize = getLotSize(segment, symbol)
        val quantity = (lots * lotSize).toInt()

        val request = PlaceOrderRequest(
            transaction_type = orderType,
            order_type = selectedOrderType,
            quantity = quantity,
            lots = lotsText,
            price = price,
            stoploss = stoploss,
            target = target,
            exchange = exchange,
            instrument_token = token,
            symbol = symbol,
            product_type = "MIS"
        )

        binding.btnPlaceOrder.isEnabled = false
        binding.btnPlaceOrder.text = "Placing..."

        lifecycleScope.launch {
            when (val result = ordersRepository.placeOrder(request)) {
                is Resource.Success -> {
                    val response = result.data
                    Toast.makeText(
                        requireContext(),
                        "Order placed successfully! Order ID: ${response.data.id}",
                        Toast.LENGTH_LONG
                    ).show()
                    dismiss()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Failed: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.btnPlaceOrder.isEnabled = true
                    binding.btnPlaceOrder.text = if (orderType == "BUY") "Buy" else "Sell"
                }
                is Resource.Loading -> {
                    // Already showing loading state
                }
            }
        }
    }

    private fun getLotSize(segment: String, symbol: String): Int {
        return when {
            segment.contains("NFO", ignoreCase = true) && symbol.contains("NIFTY", ignoreCase = true) -> {
                when {
                    symbol.contains("BANKNIFTY", ignoreCase = true) -> 15
                    symbol.contains("FINNIFTY", ignoreCase = true) -> 25
                    symbol.contains("MIDCPNIFTY", ignoreCase = true) -> 50
                    else -> 25
                }
            }
            segment.contains("MCX", ignoreCase = true) -> 1
            segment.contains("CRYPTO", ignoreCase = true) -> 1
            segment.contains("COMEX", ignoreCase = true) -> 1
            else -> 1
        }
    }

    private fun subscribeToUpdates() {
        AppWebSocketManager.setMarketListener { json ->
            if (json.optString("type") != "market_data") return@setMarketListener

            val data = json.optJSONObject("data") ?: return@setMarketListener

            val incomingToken: String?
            val isForUs: Boolean

            if (data.has("Token")) {
                incomingToken = data.optLong("Token").toString()
                isForUs = incomingToken == token
            } else if (data.has("instrument_token")) {
                incomingToken = data.optString("instrument_token")
                isForUs = incomingToken == token || incomingToken == symbol
            } else {
                return@setMarketListener
            }

            if (!isForUs) return@setMarketListener

            updateUI(data)
        }

        if (!isSubscribed) {
            val subscribe = JSONObject().apply {
                put("type", "subscribe")
                put("tokens", org.json.JSONArray().apply {
                    put(token)
                })
            }
            AppWebSocketManager.sendMessage(subscribe.toString())
            isSubscribed = true
        }
    }

    private fun updateUI(data: JSONObject) {
        val newLtp = when {
            data.has("LTP") -> data.optDouble("LTP")
            data.has("ltp") -> data.optDouble("ltp")
            data.has("last_price") -> data.optDouble("last_price")
            else -> 0.0
        }

        if (newLtp > 0) {
            this.ltp = newLtp
        }

        var bid = 0.0
        var ask = 0.0

        try {
            if (data.has("bid") || data.has("ask")) {
                bid = data.optDouble("bid", 0.0)
                ask = data.optDouble("ask", 0.0)
            } else if (data.has("Buy") && data.has("Sell")) {
                val buyArray = data.optJSONArray("Buy")
                if (buyArray != null && buyArray.length() > 0) {
                    var maxBid = 0.0
                    for (i in 0 until buyArray.length()) {
                        val buyObj = buyArray.optJSONObject(i)
                        val price = buyObj?.optDouble("price") ?: 0.0
                        if (price > 0 && price > maxBid) maxBid = price
                    }
                    bid = maxBid
                }

                val sellArray = data.optJSONArray("Sell")
                if (sellArray != null && sellArray.length() > 0) {
                    var minAsk = Double.MAX_VALUE
                    for (i in 0 until sellArray.length()) {
                        val sellObj = sellArray.optJSONObject(i)
                        val price = sellObj?.optDouble("price") ?: Double.MAX_VALUE
                        if (price > 0 && price < minAsk) minAsk = price
                    }
                    ask = if (minAsk == Double.MAX_VALUE) 0.0 else minAsk
                }
            }
        } catch (e: Exception) {
            bid = data.optDouble("bid", 0.0)
            ask = data.optDouble("ask", 0.0)
        }

        val open = data.optDouble("O", data.optDouble("open", 0.0))
        val high = data.optDouble("H", data.optDouble("high", 0.0))
        val low = data.optDouble("L", data.optDouble("low", 0.0))
        val close = data.optDouble("C", data.optDouble("close", 0.0))

        val change: Double
        val changePercent: Double

        if (data.has("change") && data.has("change_percent")) {
            change = data.optDouble("change", 0.0)
            changePercent = data.optDouble("change_percent", 0.0)
        } else {
            val previousClose = close
            change = newLtp - previousClose
            changePercent = if (previousClose != 0.0) (change / previousClose) * 100 else 0.0
        }

        val expiryDate = data.optString("expiry_date", "")

        requireActivity().runOnUiThread {
            val decimalPlaces = when {
                newLtp < 1.0 -> 6
                newLtp < 10.0 -> 5
                newLtp < 100.0 -> 5
                newLtp < 1000.0 -> 2
                else -> 2
            }

            binding.tvLtp.text = String.format("%.${decimalPlaces}f", newLtp)

            if (binding.tabLayout.selectedTabPosition == 0) {
                binding.etPrice.setText(String.format("%.${decimalPlaces}f", newLtp))
            }

            val bidAskText = if (bid > 0 && ask > 0) {
                "Bid: %.${decimalPlaces}f   Ask: %.${decimalPlaces}f".format(bid, ask)
            } else {
                "Bid: -   Ask: -"
            }
            binding.tvBidAsk.text = bidAskText

            binding.tvOpen.text = "O: %.${decimalPlaces}f".format(open)
            binding.tvHigh.text = "H: %.${decimalPlaces}f".format(high)
            binding.tvLow.text = "L: %.${decimalPlaces}f".format(low)
            binding.tvClose.text = "C: %.${decimalPlaces}f".format(close)

            if (expiryDate.isNotEmpty()) {
                binding.tvDate.text = expiryDate
            }

            val changeDecimalPlaces = if (Math.abs(change) < 1.0) decimalPlaces else 2
            val changeFormatted = if (change >= 0) {
                String.format("+%.${changeDecimalPlaces}f", change)
            } else {
                String.format("%.${changeDecimalPlaces}f", change)
            }
            val percentFormatted = String.format("%.2f", Math.abs(changePercent))

            binding.tvChange.text = "$changeFormatted ($percentFormatted%)"

            val color = if (change >= 0)
                ContextCompat.getColor(requireContext(), R.color.button_green_dark)
            else
                ContextCompat.getColor(requireContext(), R.color.red)

            binding.tvLtp.setTextColor(color)
            binding.tvChange.setTextColor(color)
            binding.tvBidAsk.setTextColor(color)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (isSubscribed) {
            val unsubscribe = JSONObject().apply {
                put("type", "unsubscribe")
                put("tokens", org.json.JSONArray().apply {
                    put(token)
                })
            }
            AppWebSocketManager.sendMessage(unsubscribe.toString())
            isSubscribed = false
        }

        AppWebSocketManager.setMarketListener(null)
        _binding = null
    }
}