package com.example.bullback.ui.orders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bullback.R
import com.example.bullback.databinding.FragmentOrdersBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.util.Log
import com.example.bullback.data.model.websocket.AppWebSocketManager
import org.json.JSONObject
import androidx.core.content.ContextCompat

class OrderBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentOrdersBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var currentSymbol: String = ""
    private var currentToken: String = ""
    private var currentLtp: Double = 0.0

    // Track if we're already subscribed
    private var isSubscribed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("OrderBottomSheet", "onViewCreated called")

        // ---------------------------
        // READ PASSED ARGUMENTS
        // ---------------------------
        currentSymbol = arguments?.getString("symbol") ?: ""
        val status = arguments?.getString("status") ?: ""
        val price = arguments?.getString("price") ?: ""
        val qty = arguments?.getInt("quantity", 0) ?: 0
        val orderId = arguments?.getInt("orderId", 0) ?: 0
        currentToken = arguments?.getString("token") ?: "" // Get token from arguments

        Log.d("OrderBottomSheet", "Arguments - Symbol: $currentSymbol, Status: $status, Price: $price, Qty: $qty, OrderId: $orderId, Token: $currentToken")

        // ---------------------------
        // SET UI VALUES FROM ARGUMENTS
        // ---------------------------
        binding.tvSymbol.text = currentSymbol
        binding.etPrice.setText(price)
        binding.etLots.setText(qty.toString())

        // Set some default/static values
        binding.tvSegment.text = "INDEX-FUT"
        binding.tvDate.text = "2026-02-03"

        // Initialize with passed price or default
        if (price.isNotEmpty()) {
            try {
                currentLtp = price.toDoubleOrNull() ?: 0.0
                binding.tvLtp.text = price
            } catch (e: Exception) {
                Log.e("OrderBottomSheet", "Error parsing price", e)
            }
        }

        // ---------------------------
        // ADD TABS PROGRAMMATICALLY
        // ---------------------------
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Market"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Limit"))

        // Select first tab by default
        binding.tabLayout.getTabAt(0)?.select()

        // Tab selection listener
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                Log.d("OrderBottomSheet", "Tab selected: ${tab?.text}")
                when (tab?.position) {
                    0 -> {
                        // Market - disable price field
                        binding.etPrice.isEnabled = false
                    }
                    1 -> {
                        // Limit - enable price field
                        binding.etPrice.isEnabled = true
                    }
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        // ---------------------------
        // SWITCH LOGIC
        // ---------------------------
        binding.switchStoplossTarget.setOnCheckedChangeListener { _, isChecked ->
            binding.llStoplossTarget.visibility = if (isChecked) View.VISIBLE else View.GONE
            Log.d("OrderBottomSheet", "Stoploss/Target visibility: ${if (isChecked) "VISIBLE" else "GONE"}")
        }

        // ---------------------------
        // BACK BUTTON
        // ---------------------------
        binding.btnBack.setOnClickListener {
            Log.d("OrderBottomSheet", "Back button clicked")
            dismiss()
        }

        // ---------------------------
        // MODIFY BUTTON
        // ---------------------------
        binding.btnModify.setOnClickListener {
            val lots = binding.etLots.text.toString()
            val priceValue = binding.etPrice.text.toString()
            val stoploss = binding.etStoploss.text.toString()
            val target = binding.etTarget.text.toString()

            Log.d("OrderBottomSheet", "Modify Order: ID=$orderId, Lots=$lots, Price=$priceValue, SL=$stoploss, Target=$target")
            // TODO: Call your API to modify the order
        }

        // ---------------------------
        // DELETE BUTTON
        // ---------------------------
        binding.btnDelete.setOnClickListener {
            Log.d("OrderBottomSheet", "Delete Order: ID=$orderId")
            // TODO: Call your API to delete the order
        }

        // ---------------------------
        // SETUP WEBSOCKET
        // ---------------------------
        subscribeToUpdates()
    }

    private fun subscribeToUpdates() {
        Log.d("OrderBottomSheet", "Setting up WebSocket for token: $currentToken")

        AppWebSocketManager.setMarketListener { json ->
            Log.d("OrderBottomSheet", "Received WebSocket message: $json")

            if (json.optString("type") != "market_data") {
                Log.d("OrderBottomSheet", "Not market_data type, ignoring")
                return@setMarketListener
            }

            val data = json.optJSONObject("data")
            if (data == null) {
                Log.d("OrderBottomSheet", "No data object found")
                return@setMarketListener
            }

            // Determine if this update is for our symbol/token
            val incomingToken: String?
            val isForUs: Boolean

            if (data.has("Token")) {
                // Index/MCX format - match by numeric token
                incomingToken = data.optLong("Token").toString()
                isForUs = if (currentToken.isNotEmpty()) {
                    incomingToken == currentToken
                } else {
                    // Fallback: if no token, check if it's NIFTY-related
                    currentSymbol.contains("NIFTY", ignoreCase = true) && incomingToken.matches(Regex("\\d+"))
                }
                Log.d("OrderBottomSheet", "Index/MCX: incoming=$incomingToken, our=$currentToken, match=$isForUs")
            } else if (data.has("instrument_token")) {
                // Crypto/Comex format - match by instrument_token as symbol
                incomingToken = data.optString("instrument_token")
                // Check both token and symbol for flexibility
                isForUs = incomingToken == currentToken || incomingToken == currentSymbol
                Log.d("OrderBottomSheet", "Crypto: incoming=$incomingToken, our=$currentToken, symbol=$currentSymbol, match=$isForUs")
            } else {
                Log.d("OrderBottomSheet", "No Token or instrument_token field found")
                return@setMarketListener
            }

            if (!isForUs) {
                Log.d("OrderBottomSheet", "Update not for us, ignoring")
                return@setMarketListener
            }

            Log.d("OrderBottomSheet", "✓ Update is for us! Updating UI with data: $data")
            // Extract and update UI
            updateUI(data)
        }

        // Send subscription only if we have a token
        if (!isSubscribed && currentToken.isNotEmpty()) {
            val subscribe = JSONObject().apply {
                put("type", "subscribe")
                put("tokens", org.json.JSONArray().apply {
                    put(currentToken)
                })
            }
            Log.d("OrderBottomSheet", "Subscribing to token: $currentToken with message: $subscribe")
            AppWebSocketManager.sendMessage(subscribe.toString())
            isSubscribed = true
        } else if (currentToken.isEmpty()) {
            Log.w("OrderBottomSheet", "No token provided, cannot subscribe to updates")
        }
    }

    private fun updateUI(data: JSONObject) {
        try {
            // Extract LTP
            val newLtp = when {
                data.has("LTP") -> data.optDouble("LTP")
                data.has("ltp") -> data.optDouble("ltp")
                data.has("last_price") -> data.optDouble("last_price")
                else -> 0.0
            }

            // Update the class-level ltp variable
            if (newLtp > 0) {
                currentLtp = newLtp
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
                Log.e("OrderBottomSheet", "Error parsing bid/ask", e)
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
            } else if (data.has("NetChg")) {
                // Index/MCX format - use NetChg
                change = data.optDouble("NetChg", 0.0)
                // Calculate percentage from close price
                val previousClose = close
                changePercent = if (previousClose != 0.0) (change / previousClose) * 100 else 0.0
            } else {
                // Fallback: calculate from close price
                val previousClose = close
                change = newLtp - previousClose
                changePercent = if (previousClose != 0.0) (change / previousClose) * 100 else 0.0
            }

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
                Log.d("OrderBottomSheet", "Updated LTP: ${binding.tvLtp.text}")

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
                Log.d("OrderBottomSheet", "Updated Bid/Ask: $bidAskText")

                // Update OHLC
                binding.tvOpen.text = "O: %.${decimalPlaces}f".format(open)
                binding.tvHigh.text = "H: %.${decimalPlaces}f".format(high)
                binding.tvLow.text = "L: %.${decimalPlaces}f".format(low)
                binding.tvClose.text = "C: %.${decimalPlaces}f".format(close)
                Log.d("OrderBottomSheet", "Updated OHLC - O: $open, H: $high, L: $low, C: $close")

                // Update Change
                val changeDecimalPlaces = if (Math.abs(change) < 1.0) decimalPlaces else 2
                val changeFormatted = if (change >= 0) {
                    String.format("+%.${changeDecimalPlaces}f", change)
                } else {
                    String.format("%.${changeDecimalPlaces}f", change)
                }
                val percentFormatted = String.format("%.2f", Math.abs(changePercent))

                binding.tvChange.text = "$changeFormatted ($percentFormatted%)"
                Log.d("OrderBottomSheet", "Updated Change: ${binding.tvChange.text}")

                // Update colors
                val color = if (change >= 0)
                    ContextCompat.getColor(requireContext(), R.color.button_green_dark)
                else
                    ContextCompat.getColor(requireContext(), R.color.red)

                binding.tvLtp.setTextColor(color)
                binding.tvChange.setTextColor(color)
            }

        } catch (e: Exception) {
            Log.e("OrderBottomSheet", "Error updating UI", e)
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Log.d("OrderBottomSheet", "onDestroyView called")

        // Unsubscribe when bottom sheet is closed
        if (isSubscribed && currentToken.isNotEmpty()) {
            val unsubscribe = JSONObject().apply {
                put("type", "unsubscribe")
                put("tokens", org.json.JSONArray().apply {
                    put(currentToken)
                })
            }
            Log.d("OrderBottomSheet", "Unsubscribing from token: $currentToken")
            AppWebSocketManager.sendMessage(unsubscribe.toString())
            isSubscribed = false
        }

        // Clear the listener to avoid memory leaks
        AppWebSocketManager.setMarketListener(null)

        _binding = null
    }
}