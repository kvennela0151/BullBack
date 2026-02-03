package com.example.bullback.ui.positions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bullback.R
import androidx.core.content.ContextCompat
import com.example.bullback.data.model.positions.PositionsItem
import com.example.bullback.data.model.websocket.AppWebSocketManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONObject
import android.util.Log
import com.example.bullback.databinding.FragmentPositionsBottomSheetBinding
import kotlin.math.abs

class PositionDetailsBottomSheet(
    private val position: PositionsItem
) : BottomSheetDialogFragment() {

    private var _binding: FragmentPositionsBottomSheetBinding? = null
    private val binding get() = _binding!!

    // Track if we're already subscribed
    private var isSubscribed = false

    // Current market data
    private var currentLtp: Double = 0.0
    private var averagePrice: Double = 0.0
    private var netQuantity: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPositionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initial UI setup
        setupUI()

        // Subscribe to real-time updates
        subscribeToUpdates()

        // Button listeners
        setupButtons()
    }

    private fun setupUI() {
        binding.apply {
            // Symbol and segment
            tvSymbol.text = position.symbol
            tvSegment.text = when (position.exchange) {
                "NSE", "BSE" -> "EQUITY"
                "NFO" -> "NFO"
                "MCX" -> "COMMODITY"
                "CRYPTO" -> "CRYPTO"
                else -> position.exchange
            }

            // Store initial values
            averagePrice = position.averagePrice.toDoubleOrNull() ?: 0.0
            netQuantity = position.netQuantity.toDoubleOrNull() ?: 0.0

            // Use buy_price as initial LTP if available, otherwise use average price
            currentLtp = position.buyPrice.toDoubleOrNull() ?: averagePrice

            // Initial LTP
            tvLtp.text = String.format("%.2f", currentLtp)

            // Initial Bid/Ask - will be updated by WebSocket
            tvBid.text = "Bid: -"
            tvAsk.text = "Ask: -"

            // Initial OHLC - will be updated by WebSocket
            tvOpen.text = "O: -"
            tvHigh.text = "H: -"
            tvLow.text = "L: -"
            tvClose.text = "C: -"

            // Initial Change - will be updated by WebSocket
            tvChange.text = "-"

            // Expiry date - positions don't have expiry in this model
            tvExpiry.text = "-"

            // Calculate and display initial P&L
            calculateAndDisplayPnL()
        }
    }

    private fun setupButtons() {
        binding.apply {
            btnBuy.setOnClickListener {
                // Open AddExitPositionsBottomSheet in ADD mode
                val addBottomSheet = AddExitPositionsBottomSheet(
                    position = position,
                    mode = AddExitPositionsBottomSheet.Mode.ADD
                )
                dismiss() // Close current sheet
                addBottomSheet.show(parentFragmentManager, "AddPositionBottomSheet")
            }

            btnSell.setOnClickListener {
                // Open AddExitPositionsBottomSheet in EXIT mode
                val exitBottomSheet = AddExitPositionsBottomSheet(
                    position = position,
                    mode = AddExitPositionsBottomSheet.Mode.EXIT
                )
                dismiss() // Close current sheet
                exitBottomSheet.show(parentFragmentManager, "ExitPositionBottomSheet")
            }

            bntExitAll.setOnClickListener {
                // Confirm and exit entire position
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Exit Entire Position?")
                    .setMessage("Are you sure you want to exit the entire position of ${position.symbol}?")
                    .setPositiveButton("Exit All") { _, _ ->
                        exitEntirePosition()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun exitEntirePosition() {
        // TODO: Implement exit entire position API call
        Log.d("PositionDetailsSheet", "Exiting entire position for ${position.symbol}")

        // Close the bottom sheet
        dismiss()

        // Optionally, refresh positions in the parent fragment
        // You can use a callback or shared ViewModel for this
    }

    private fun subscribeToUpdates() {
        AppWebSocketManager.setMarketListener { json ->
            Log.d("PositionDetailsSheet", "Received WebSocket message: $json")

            if (json.optString("type") != "market_data") {
                Log.d("PositionDetailsSheet", "Not market_data type, ignoring")
                return@setMarketListener
            }

            val data = json.optJSONObject("data")
            if (data == null) {
                Log.d("PositionDetailsSheet", "No data object found")
                return@setMarketListener
            }

            // Determine if this update is for our position
            val isForUs = isUpdateForThisPosition(data)

            if (!isForUs) {
                Log.d("PositionDetailsSheet", "Update not for us, ignoring")
                return@setMarketListener
            }

            Log.d("PositionDetailsSheet", "Update is for us! Updating UI with data: $data")
            // Extract and update UI
            updateUI(data)
        }

        // Send subscription
        if (!isSubscribed && position.instrumentToken.isNotEmpty()) {
            val subscribe = JSONObject().apply {
                put("type", "subscribe")
                put("tokens", org.json.JSONArray().apply {
                    put(position.instrumentToken)
                })
            }
            Log.d("PositionDetailsSheet", "Subscribing to token: ${position.instrumentToken} with message: $subscribe")
            AppWebSocketManager.sendMessage(subscribe.toString())
            isSubscribed = true
        }
    }

    private fun isUpdateForThisPosition(data: JSONObject): Boolean {
        // Try to match by token first
        if (data.has("Token")) {
            val incomingToken = data.optLong("Token").toString()
            if (incomingToken == position.instrumentToken) return true
        }

        // Try instrument_token (for crypto/comex)
        if (data.has("instrument_token")) {
            val incomingToken = data.optString("instrument_token")
            if (incomingToken == position.instrumentToken || incomingToken == position.symbol) return true
        }

        // Fallback: try matching by symbol
        val symbol = data.optString("symbol", "")
        if (symbol.equals(position.symbol, ignoreCase = true)) return true

        return false
    }

    private fun updateUI(data: JSONObject) {
        // Extract LTP
        val ltp = when {
            data.has("LTP") -> data.optDouble("LTP")
            data.has("ltp") -> data.optDouble("ltp")
            data.has("last_price") -> data.optDouble("last_price")
            else -> 0.0
        }

        if (ltp > 0) {
            currentLtp = ltp
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
            Log.e("PositionDetailsSheet", "Error extracting bid/ask", e)
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
            change = currentLtp - previousClose
            changePercent = if (previousClose != 0.0) (change / previousClose) * 100 else 0.0
        }

        // Extract expiry date if available
        val expiryDate = data.optString("expiry_date", "")

        // Update UI on main thread
        requireActivity().runOnUiThread {
            // Determine decimal places based on price magnitude
            val decimalPlaces = when {
                currentLtp < 1.0 -> 6      // For very small values like 0.000123
                currentLtp < 10.0 -> 5     // For small values like 5.12345
                currentLtp < 100.0 -> 5    // For medium values like 17.10061
                currentLtp < 1000.0 -> 2   // For higher values like 117.60
                else -> 2                  // For large values like 25343.00
            }

            binding.apply {
                // Update LTP
                tvLtp.text = String.format("%.${decimalPlaces}f", currentLtp)

                // Update Bid/Ask
                if (bid > 0) {
                    tvBid.text = "Bid: %.${decimalPlaces}f".format(bid)
                    tvBid.setTextColor(ContextCompat.getColor(requireContext(), R.color.button_green_dark))
                }
                if (ask > 0) {
                    tvAsk.text = "Ask: %.${decimalPlaces}f".format(ask)
                    tvAsk.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }

                // Update OHLC
                if (open > 0) tvOpen.text = "O: %.${decimalPlaces}f".format(open)
                if (high > 0) tvHigh.text = "H: %.${decimalPlaces}f".format(high)
                if (low > 0) tvLow.text = "L: %.${decimalPlaces}f".format(low)
                if (close > 0) tvClose.text = "C: %.${decimalPlaces}f".format(close)

                // Update Expiry if available
                if (expiryDate.isNotEmpty()) {
                    tvExpiry.text = expiryDate
                }

                // Update Change
                val changeDecimalPlaces = if (abs(change) < 1.0) decimalPlaces else 2
                val changeFormatted = if (change >= 0) {
                    String.format("+%.${changeDecimalPlaces}f", change)
                } else {
                    String.format("%.${changeDecimalPlaces}f", change)
                }
                val percentFormatted = String.format("%.2f", abs(changePercent))

                tvChange.text = "$changeFormatted ($percentFormatted%)"

                // Update colors for LTP and Change
                val color = if (change >= 0)
                    ContextCompat.getColor(requireContext(), R.color.button_green_dark)
                else
                    ContextCompat.getColor(requireContext(), R.color.red)

                tvLtp.setTextColor(color)
                tvChange.setTextColor(color)

                // Recalculate and update P&L
                calculateAndDisplayPnL()
            }
        }
    }

    private fun calculateAndDisplayPnL() {
        // Calculate P&L based on current LTP and average price
        // P&L = (Current LTP - Average Price) * Net Quantity
        val pnl = (currentLtp - averagePrice) * netQuantity

        binding.apply {
            tvAmount.text = String.format("%.2f", pnl)

            // Set color based on profit or loss
            val pnlColor = if (pnl >= 0)
                ContextCompat.getColor(requireContext(), R.color.button_green_dark)
            else
                ContextCompat.getColor(requireContext(), R.color.red)

            tvAmount.setTextColor(pnlColor)

            // Also update button color for "Exit All" based on P&L
            bntExitAll.backgroundTintList = if (pnl >= 0)
                ContextCompat.getColorStateList(requireContext(), R.color.button_green_dark)
            else
                ContextCompat.getColorStateList(requireContext(), R.color.red)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Unsubscribe when bottom sheet is closed
        if (isSubscribed && position.instrumentToken.isNotEmpty()) {
            val unsubscribe = JSONObject().apply {
                put("type", "unsubscribe")
                put("tokens", org.json.JSONArray().apply {
                    put(position.instrumentToken)
                })
            }
            AppWebSocketManager.sendMessage(unsubscribe.toString())
            isSubscribed = false
        }

        AppWebSocketManager.setMarketListener(null)
        _binding = null
    }


}