package com.example.bullback.ui.watchlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bullback.databinding.FragmentBuySellWatlistSymbolBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import androidx.core.content.ContextCompat
import com.example.bullback.R
import com.example.bullback.data.model.websocket.AppWebSocketManager
import org.json.JSONObject

class BuySellWatlistSymbolBottomSheet(
    private val symbol: String,
    private val segment: String,
    private var ltp: Double,
    private val orderType: String, // "BUY" or "SELL"
    private val token: String // Add token parameter
) : BottomSheetDialogFragment() {

    private var _binding: FragmentBuySellWatlistSymbolBottomSheetBinding? = null
    private val binding get() = _binding!!

    // Track if we're already subscribed
    private var isSubscribed = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuySellWatlistSymbolBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initial UI setup
        binding.tvSymbol.text = symbol
        binding.tvSegment.text = segment
        binding.tvLtp.text = String.format("%.2f", ltp)
        binding.etPrice.setText(ltp.toString())

        // Subscribe to real-time updates
        subscribeToUpdates()

        // Toggle Stoploss & Target fields
        binding.switchSL.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutStoplossTarget.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Tab selection listener (Market/Limit)
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

        // Place Order
        binding.btnPlaceOrder.text = if (orderType == "BUY") "Buy" else "Sell"
        binding.btnPlaceOrder.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            if (orderType == "BUY") R.color.button_green_dark else R.color.red
        )

        binding.btnPlaceOrder.setOnClickListener {
            val price = binding.etPrice.text.toString()
            val lots = binding.etLots.text.toString()
            val stoploss = binding.etStoploss.text.toString()
            val target = binding.etTarget.text.toString()

            // TODO: Call API or handle BUY/SELL order
            dismiss()
        }

        // Back button
        binding.btnBack.setOnClickListener {
            dismiss()
        }
    }

    private fun subscribeToUpdates() {
        AppWebSocketManager.setMarketListener { json ->
            android.util.Log.d("BuySellSheet", "Received WebSocket message: $json")

            if (json.optString("type") != "market_data") {
                android.util.Log.d("BuySellSheet", "Not market_data type, ignoring")
                return@setMarketListener
            }

            val data = json.optJSONObject("data")
            if (data == null) {
                android.util.Log.d("BuySellSheet", "No data object found")
                return@setMarketListener
            }

            // Determine if this update is for our symbol/token
            val incomingToken: String?
            val isForUs: Boolean

            if (data.has("Token")) {
                // Index/MCX format - match by numeric token
                incomingToken = data.optLong("Token").toString()
                isForUs = incomingToken == token
                android.util.Log.d("BuySellSheet", "Index/MCX: incoming=$incomingToken, our=$token, match=$isForUs")
            } else if (data.has("instrument_token")) {
                // Crypto/Comex format - match by instrument_token as symbol
                incomingToken = data.optString("instrument_token")
                // Check both token and symbol for flexibility
                isForUs = incomingToken == token || incomingToken == symbol
                android.util.Log.d("BuySellSheet", "Crypto: incoming=$incomingToken, our=$token, symbol=$symbol, match=$isForUs")
            } else {
                android.util.Log.d("BuySellSheet", "No Token or instrument_token field found")
                return@setMarketListener
            }

            if (!isForUs) {
                android.util.Log.d("BuySellSheet", "Update not for us, ignoring")
                return@setMarketListener
            }

            android.util.Log.d("BuySellSheet", "Update is for us! Updating UI with data: $data")
            // Extract and update UI
            updateUI(data)
        }

        // Send subscription only once
        if (!isSubscribed) {
            val subscribe = JSONObject().apply {
                put("type", "subscribe")
                put("tokens", org.json.JSONArray().apply {
                    put(token)
                })
            }
            android.util.Log.d("BuySellSheet", "Subscribing to token: $token with message: $subscribe")
            AppWebSocketManager.sendMessage(subscribe.toString())
            isSubscribed = true
        }
    }

    private fun updateUI(data: JSONObject) {
        // Extract LTP
        val newLtp = when {
            data.has("LTP") -> data.optDouble("LTP")
            data.has("ltp") -> data.optDouble("ltp")
            data.has("last_price") -> data.optDouble("last_price")
            else -> 0.0
        }

        // Update the class-level ltp variable
        if (newLtp > 0) {
            this.ltp = newLtp
        }

        // Extract Bid/Ask
        var bid = 0.0
        var ask = 0.0

        try {
            // Check if we have direct bid/ask fields first (Crypto/Comex format)
            if (data.has("bid") || data.has("ask")) {
                bid = data.optDouble("bid", 0.0)
                ask = data.optDouble("ask", 0.0)
            }
            // Otherwise check for Buy/Sell arrays (Index/MCX format)
            else if (data.has("Buy") && data.has("Sell")) {
                // Bid is the highest buying price (from Buy array)
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

                // Ask is the lowest selling price (from Sell array)
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
            // Fallback to direct fields if arrays not available
            bid = data.optDouble("bid", 0.0)
            ask = data.optDouble("ask", 0.0)
        }

        // Extract OHLC
        val open = data.optDouble("O", data.optDouble("open", 0.0))
        val high = data.optDouble("H", data.optDouble("high", 0.0))
        val low = data.optDouble("L", data.optDouble("low", 0.0))
        val close = data.optDouble("C", data.optDouble("close", 0.0))

        // Calculate change - handle both formats
        val change: Double
        val changePercent: Double

        if (data.has("change") && data.has("change_percent")) {
            // Crypto/Comex format - use provided change values
            change = data.optDouble("change", 0.0)
            changePercent = data.optDouble("change_percent", 0.0)
        } else {
            // Index/MCX format - calculate from close price
            val previousClose = close
            change = newLtp - previousClose
            changePercent = if (previousClose != 0.0) (change / previousClose) * 100 else 0.0
        }

        // Extract expiry date if available
        val expiryDate = data.optString("expiry_date", "")

        // Update UI on main thread
        requireActivity().runOnUiThread {
            // Determine decimal places based on price magnitude
            val decimalPlaces = when {
                newLtp < 1.0 -> 6      // For very small values like 0.000123
                newLtp < 10.0 -> 5     // For small values like 5.12345
                newLtp < 100.0 -> 5    // For medium values like 17.10061
                newLtp < 1000.0 -> 2   // For higher values like 117.60
                else -> 2              // For large values like 25343.00
            }

            // Update LTP
            binding.tvLtp.text = String.format("%.${decimalPlaces}f", newLtp)

            // Update Price field if in Market mode (tab 0)
            if (binding.tabLayout.selectedTabPosition == 0) {
                binding.etPrice.setText(String.format("%.${decimalPlaces}f", newLtp))
            }

            // Update Bid/Ask (combined field)
            val bidAskText = if (bid > 0 && ask > 0) {
                "Bid: %.${decimalPlaces}f   Ask: %.${decimalPlaces}f".format(bid, ask)
            } else {
                "Bid: -   Ask: -"
            }
            binding.tvBidAsk.text = bidAskText

            // Update OHLC
            binding.tvOpen.text = "O: %.${decimalPlaces}f".format(open)
            binding.tvHigh.text = "H: %.${decimalPlaces}f".format(high)
            binding.tvLow.text = "L: %.${decimalPlaces}f".format(low)
            binding.tvClose.text = "C: %.${decimalPlaces}f".format(close)

            // Update Date/Expiry
            if (expiryDate.isNotEmpty()) {
                binding.tvDate.text = expiryDate
            }

            // Update Change
            val changeDecimalPlaces = if (Math.abs(change) < 1.0) decimalPlaces else 2
            val changeFormatted = if (change >= 0) {
                String.format("+%.${changeDecimalPlaces}f", change)
            } else {
                String.format("%.${changeDecimalPlaces}f", change)
            }
            val percentFormatted = String.format("%.2f", Math.abs(changePercent))

            binding.tvChange.text = "$changeFormatted ($percentFormatted%)"

            // Update colors
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

        // Unsubscribe when bottom sheet is closed
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
