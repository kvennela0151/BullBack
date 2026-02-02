package com.example.bullback.ui.positions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bullback.R
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.bullback.data.model.positions.PositionsItem
import com.example.bullback.data.model.websocket.AppWebSocketManager
import com.example.bullback.databinding.FragmentAddExitPositionsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONArray
import org.json.JSONObject
import android.util.Log
import kotlin.math.abs

class AddExitPositionsBottomSheet(
    private val position: PositionsItem,
    private val mode: Mode = Mode.ADD
) : BottomSheetDialogFragment() {

    enum class Mode {
        ADD,    // Add more to position
        EXIT    // Exit position (partial or full)
    }

    private var _binding: FragmentAddExitPositionsBottomSheetBinding? = null
    private val binding get() = _binding!!

    // Track if we're already subscribed
    private var isSubscribed = false

    // Current market data
    private var currentLtp: Double = 0.0
    private var currentBid: Double = 0.0
    private var currentAsk: Double = 0.0
    private var currentOpen: Double = 0.0
    private var currentHigh: Double = 0.0
    private var currentLow: Double = 0.0
    private var currentClose: Double = 0.0
    private var currentChange: Double = 0.0
    private var currentChangePercent: Double = 0.0

    // Position data
    private var averagePrice: Double = 0.0
    private var netQuantity: Double = 0.0
    private var lotSize: Int = 30 // Default lot size
    private var maxLots: Int = 200 // Default max lots
    private var orderLots: Int = 200 // Default order lots

    // Order type
    private var isMarketOrder = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExitPositionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initial UI setup
        setupUI()

        // Subscribe to real-time updates
        subscribeToUpdates()

        // Setup listeners
        setupListeners()
    }

    private fun setupUI() {
        binding.apply {
            // Set title based on mode
            tvTitle.text = if (mode == Mode.ADD) "Add More Position" else "Exit Position"

            // Symbol and segment
            tvSymbol.text = position.symbol
            tvSegment.text = "${position.exchange} | MIS"

            // Expiry (if available)
            tvExpiry.text = "-" // Positions don't have expiry in current model

            // Store initial values
            averagePrice = position.averagePrice.toDoubleOrNull() ?: 0.0
            netQuantity = position.netQuantity.toDoubleOrNull() ?: 0.0

            // Use buy_price as initial LTP if available, otherwise use average price
            currentLtp = position.buyPrice.toDoubleOrNull() ?: averagePrice

            // Initial LTP
            tvLtp.text = String.format("%.2f", currentLtp)

            // Initial Bid/Ask
            tvBid.text = "Bid: -"
            tvAsk.text = "Ask: -"

            // Initial Change
            tvChange.text = "-"

            // Initial OHLC
            tvOpen.text = "O: -"
            tvHigh.text = "H: -"
            tvLow.text = "L: -"
            tvClose.text = "C: -"

            // Calculate and display initial P&L
            calculateAndDisplayPnL()

            // Set up lot size and max lots (these should come from instrument data)
            // For now using defaults, but you should fetch from your instrument repository
            lotSize = 30 // You should get this from position.lotSize or instrument data
            maxLots = 200
            orderLots = 200

            // Set initial lots value
            etLots.setText("1")

            // Update lots hint for EXIT mode to show current position
            if (mode == Mode.EXIT) {
                val currentLots = (abs(netQuantity) / lotSize).toInt()
                lotsLayout.hint = "Lots (Current: $currentLots)"
            }

            // Market order is default
            tvMarket.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tvMarket.textSize = 16f
            tvLimit.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tvLimit.textSize = 16f

            // Lock price field for market orders
            etPrice.setText(String.format("%.2f", currentLtp))
            etPrice.isEnabled = false
            priceLayout.isEnabled = false

            // Update button text based on mode
            btnAction.text = if (mode == Mode.ADD) "Add More" else "Exit Position"
            btnAction.backgroundTintList = if (mode == Mode.ADD)
                ContextCompat.getColorStateList(requireContext(), R.color.button_green_dark)
            else
                ContextCompat.getColorStateList(requireContext(), R.color.red)

            // Update funds info (these should come from your wallet/funds API)
            tvIntraday.text = "Intraday: -"
            tvCarry.text = "Carry: -"
            tvAvailableFunds.text = "Avail. Funds: -"

            // Show/hide Exit All button based on mode
            btnExitAll.visibility = if (mode == Mode.EXIT) View.VISIBLE else View.GONE

            // Show/hide PNL layout based on mode (only visible for EXIT mode)
            pnlLayout.visibility = if (mode == Mode.EXIT) View.VISIBLE else View.GONE

            // Adjust divider visibility based on PNL layout visibility
            divider3.visibility = if (mode == Mode.EXIT) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.apply {
            // Back button
            btnBack.setOnClickListener {
                dismiss()
            }

            // Switch for Stop Loss & Target
            switchSL.setOnCheckedChangeListener { _, isChecked ->
                // Show/hide Stop Loss & Target fields
                layoutStoplossTarget.visibility = if (isChecked) View.VISIBLE else View.GONE

                if (isChecked) {
                    // Pre-populate with suggested values if needed
                    // You can calculate suggested SL/Target based on current position
                    val suggestedSL = calculateSuggestedStopLoss()
                    val suggestedTarget = calculateSuggestedTarget()

                    if (suggestedSL > 0) {
                        etStoploss.setText(String.format("%.2f", suggestedSL))
                    }
                    if (suggestedTarget > 0) {
                        etTarget.setText(String.format("%.2f", suggestedTarget))
                    }
                }
            }

            // Market/Limit toggle
            tvMarket.setOnClickListener {
                if (!isMarketOrder) {
                    isMarketOrder = true
                    updateOrderTypeUI()
                }
            }

            tvLimit.setOnClickListener {
                if (isMarketOrder) {
                    isMarketOrder = false
                    updateOrderTypeUI()
                }
            }

            // Lots input listener
            etLots.addTextChangedListener {
                validateAndUpdateLots()
            }

            // Price input listener (for limit orders)
            etPrice.addTextChangedListener {
                // Recalculate margin/funds if needed
            }

            // Exit All button
            btnExitAll.setOnClickListener {
                // Exit entire position
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Exit Entire Position?")
                    .setMessage("Are you sure you want to exit the entire position of ${position.symbol}?")
                    .setPositiveButton("Exit All") { _, _ ->
                        exitEntirePosition()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            // Main action button
            btnAction.setOnClickListener {
                if (mode == Mode.ADD) {
                    addToPosition()
                } else {
                    exitPosition()
                }
            }
        }
    }

    private fun updateOrderTypeUI() {
        binding.apply {
            if (isMarketOrder) {
                // Market order selected
                tvMarket.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvMarket.textSize = 16f
                android.graphics.Typeface.create(tvMarket.typeface, android.graphics.Typeface.BOLD).let {
                    tvMarket.typeface = it
                }

                tvLimit.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvLimit.textSize = 16f
                tvLimit.typeface = android.graphics.Typeface.DEFAULT

                // Lock price field
                etPrice.setText(String.format("%.2f", currentLtp))
                etPrice.isEnabled = false
                priceLayout.isEnabled = false
                priceLayout.endIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_lock)
            } else {
                // Limit order selected
                tvLimit.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvLimit.textSize = 16f
                android.graphics.Typeface.create(tvLimit.typeface, android.graphics.Typeface.BOLD).let {
                    tvLimit.typeface = it
                }

                tvMarket.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvMarket.textSize = 16f
                tvMarket.typeface = android.graphics.Typeface.DEFAULT

                // Unlock price field
                etPrice.isEnabled = true
                priceLayout.isEnabled = true
                priceLayout.endIconDrawable = null
            }
        }
    }

    private fun validateAndUpdateLots() {
        val lotsText = binding.etLots.text.toString()
        if (lotsText.isEmpty()) return

        try {
            val lots = lotsText.toInt()
            if (lots > maxLots) {
                binding.etLots.error = "Max lots: $maxLots"
            } else if (lots <= 0) {
                binding.etLots.error = "Minimum 1 lot"
            } else {
                binding.etLots.error = null
            }
        } catch (e: NumberFormatException) {
            binding.etLots.error = "Invalid number"
        }
    }

    private fun subscribeToUpdates() {
        AppWebSocketManager.setMarketListener { json ->
            Log.d("AddExitPositionsSheet", "Received WebSocket message: $json")

            if (json.optString("type") != "market_data") {
                return@setMarketListener
            }

            val data = json.optJSONObject("data") ?: return@setMarketListener

            // Determine if this update is for our position
            if (!isUpdateForThisPosition(data)) {
                return@setMarketListener
            }

            Log.d("AddExitPositionsSheet", "Update is for us! Updating UI with data: $data")
            // Extract and update UI
            updateMarketData(data)
        }

        // Send subscription
        if (!isSubscribed && position.instrumentToken.isNotEmpty()) {
            val subscribe = JSONObject().apply {
                put("type", "subscribe")
                put("tokens", JSONArray().apply {
                    put(position.instrumentToken)
                })
            }
            Log.d("AddExitPositionsSheet", "Subscribing to token: ${position.instrumentToken}")
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

    private fun updateMarketData(data: JSONObject) {
        // Extract LTP
        currentLtp = when {
            data.has("LTP") -> data.optDouble("LTP")
            data.has("ltp") -> data.optDouble("ltp")
            data.has("last_price") -> data.optDouble("last_price")
            else -> currentLtp
        }

        // Extract Bid/Ask
        try {
            if (data.has("bid") || data.has("ask")) {
                currentBid = data.optDouble("bid", 0.0)
                currentAsk = data.optDouble("ask", 0.0)
            } else if (data.has("Buy") && data.has("Sell")) {
                // Bid is the highest buying price
                val buyArray = data.optJSONArray("Buy")
                if (buyArray != null && buyArray.length() > 0) {
                    var maxBid = 0.0
                    for (i in 0 until buyArray.length()) {
                        val buyObj = buyArray.optJSONObject(i)
                        val price = buyObj?.optDouble("price") ?: 0.0
                        if (price > maxBid) maxBid = price
                    }
                    currentBid = maxBid
                }

                // Ask is the lowest selling price
                val sellArray = data.optJSONArray("Sell")
                if (sellArray != null && sellArray.length() > 0) {
                    var minAsk = Double.MAX_VALUE
                    for (i in 0 until sellArray.length()) {
                        val sellObj = sellArray.optJSONObject(i)
                        val price = sellObj?.optDouble("price") ?: Double.MAX_VALUE
                        if (price < minAsk) minAsk = price
                    }
                    currentAsk = if (minAsk == Double.MAX_VALUE) 0.0 else minAsk
                }
            }
        } catch (e: Exception) {
            Log.e("AddExitPositionsSheet", "Error extracting bid/ask", e)
        }

        // Extract OHLC
        currentOpen = data.optDouble("O", data.optDouble("open", currentOpen))
        currentHigh = data.optDouble("H", data.optDouble("high", currentHigh))
        currentLow = data.optDouble("L", data.optDouble("low", currentLow))
        currentClose = data.optDouble("C", data.optDouble("close", currentClose))

        // Calculate change
        if (data.has("change") && data.has("change_percent")) {
            currentChange = data.optDouble("change", 0.0)
            currentChangePercent = data.optDouble("change_percent", 0.0)
        } else if (currentClose > 0) {
            currentChange = currentLtp - currentClose
            currentChangePercent = if (currentClose != 0.0) (currentChange / currentClose) * 100 else 0.0
        }

        // Update UI on main thread
        requireActivity().runOnUiThread {
            updateUI()
        }
    }

    private fun updateUI() {
        // Determine decimal places based on price magnitude
        val decimalPlaces = when {
            currentLtp < 1.0 -> 6
            currentLtp < 10.0 -> 5
            currentLtp < 100.0 -> 5
            currentLtp < 1000.0 -> 2
            else -> 2
        }

        binding.apply {
            // Update LTP
            if (currentLtp > 0) {
                tvLtp.text = String.format("%.${decimalPlaces}f", currentLtp)

                // Update price field if market order
                if (isMarketOrder) {
                    etPrice.setText(String.format("%.${decimalPlaces}f", currentLtp))
                }
            }

            // Update Bid/Ask
            if (currentBid > 0) {
                tvBid.text = "Bid: %.${decimalPlaces}f".format(currentBid)
                tvBid.setTextColor(ContextCompat.getColor(requireContext(), R.color.button_green_dark))
            }
            if (currentAsk > 0) {
                tvAsk.text = "Ask: %.${decimalPlaces}f".format(currentAsk)
                tvAsk.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            }

            // Update OHLC
            if (currentOpen > 0) tvOpen.text = "O: %.${decimalPlaces}f".format(currentOpen)
            if (currentHigh > 0) tvHigh.text = "H: %.${decimalPlaces}f".format(currentHigh)
            if (currentLow > 0) tvLow.text = "L: %.${decimalPlaces}f".format(currentLow)
            if (currentClose > 0) tvClose.text = "C: %.${decimalPlaces}f".format(currentClose)

            // Update Change
            val changeDecimalPlaces = if (abs(currentChange) < 1.0) decimalPlaces else 2
            val changeFormatted = if (currentChange >= 0) {
                String.format("+%.${changeDecimalPlaces}f", currentChange)
            } else {
                String.format("%.${changeDecimalPlaces}f", currentChange)
            }
            val percentFormatted = String.format("%.2f", abs(currentChangePercent))

            tvChange.text = "$changeFormatted ($percentFormatted%)"

            // Update colors for LTP and Change
            val color = if (currentChange >= 0)
                ContextCompat.getColor(requireContext(), R.color.button_green_dark)
            else
                ContextCompat.getColor(requireContext(), R.color.red)

            tvLtp.setTextColor(color)
            tvChange.setTextColor(color)

            // Recalculate and update P&L
            calculateAndDisplayPnL()
        }
    }

    private fun calculateAndDisplayPnL() {
        // Calculate P&L based on current LTP and average price
        val pnl = (currentLtp - averagePrice) * netQuantity

        binding.apply {
            tvAmount.text = String.format("%.2f", pnl)

            // Set color based on profit or loss
            val pnlColor = if (pnl >= 0)
                ContextCompat.getColor(requireContext(), R.color.button_green_dark)
            else
                ContextCompat.getColor(requireContext(), R.color.red)

            tvAmount.setTextColor(pnlColor)
        }
    }

    private fun addToPosition() {
        // Validate lots
        val lotsText = binding.etLots.text.toString()
        if (lotsText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter number of lots", Toast.LENGTH_SHORT).show()
            return
        }

        val lots = try {
            lotsText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid lot number", Toast.LENGTH_SHORT).show()
            return
        }

        if (lots <= 0) {
            Toast.makeText(requireContext(), "Lots must be greater than 0", Toast.LENGTH_SHORT).show()
            return
        }

        if (lots > maxLots) {
            Toast.makeText(requireContext(), "Maximum $maxLots lots allowed", Toast.LENGTH_SHORT).show()
            return
        }

        // Get price
        val priceText = binding.etPrice.text.toString()
        val price = try {
            priceText.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid price", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate quantity
        val quantity = lots * lotSize

        // TODO: Place order via API
        Log.d("AddExitPositionsSheet", "Adding to position: $quantity units at $price (${if (isMarketOrder) "Market" else "Limit"})")

        Toast.makeText(
            requireContext(),
            "Adding $lots lot(s) to ${position.symbol}",
            Toast.LENGTH_SHORT
        ).show()

        dismiss()
    }

    private fun exitPosition() {
        // Validate lots
        val lotsText = binding.etLots.text.toString()
        if (lotsText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter number of lots", Toast.LENGTH_SHORT).show()
            return
        }

        val lots = try {
            lotsText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid lot number", Toast.LENGTH_SHORT).show()
            return
        }

        if (lots <= 0) {
            Toast.makeText(requireContext(), "Lots must be greater than 0", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if lots exceed current position
        val currentLots = (abs(netQuantity) / lotSize).toInt()
        if (lots > currentLots) {
            Toast.makeText(
                requireContext(),
                "Cannot exit $lots lots. Current position: $currentLots lots",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Get price
        val priceText = binding.etPrice.text.toString()
        val price = try {
            priceText.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid price", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate quantity
        val quantity = lots * lotSize

        // TODO: Place order via API
        Log.d("AddExitPositionsSheet", "Exiting position: $quantity units at $price (${if (isMarketOrder) "Market" else "Limit"})")

        Toast.makeText(
            requireContext(),
            "Exiting $lots lot(s) from ${position.symbol}",
            Toast.LENGTH_SHORT
        ).show()

        dismiss()
    }

    private fun exitEntirePosition() {
        // Calculate total lots in position
        val currentLots = (abs(netQuantity) / lotSize).toInt()

        // TODO: Place market order to exit entire position
        Log.d("AddExitPositionsSheet", "Exiting entire position: $currentLots lots at market price")

        Toast.makeText(
            requireContext(),
            "Exiting entire position of ${position.symbol}",
            Toast.LENGTH_SHORT
        ).show()

        dismiss()
    }

    private fun calculateSuggestedStopLoss(): Double {
        // Calculate suggested stop loss based on current LTP and position side
        // For BUY positions: SL should be below average price
        // For SELL positions: SL should be above average price

        val slPercentage = 0.02 // 2% default stop loss

        return when (position.side) {
            "BUY" -> currentLtp * (1 - slPercentage)
            "SELL" -> currentLtp * (1 + slPercentage)
            else -> 0.0
        }
    }

    private fun calculateSuggestedTarget(): Double {
        // Calculate suggested target based on current LTP and position side
        // For BUY positions: Target should be above average price
        // For SELL positions: Target should be below average price

        val targetPercentage = 0.05 // 5% default target

        return when (position.side) {
            "BUY" -> currentLtp * (1 + targetPercentage)
            "SELL" -> currentLtp * (1 - targetPercentage)
            else -> 0.0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Unsubscribe when bottom sheet is closed
        if (isSubscribed && position.instrumentToken.isNotEmpty()) {
            val unsubscribe = JSONObject().apply {
                put("type", "unsubscribe")
                put("tokens", JSONArray().apply {
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