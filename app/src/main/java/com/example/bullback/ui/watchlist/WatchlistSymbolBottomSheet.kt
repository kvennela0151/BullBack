package com.example.bullback.ui.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bullback.databinding.FragmentWatchlistSymbolBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.core.content.ContextCompat
import com.example.bullback.R
import com.example.bullback.data.model.websocket.AppWebSocketManager
import org.json.JSONObject

class WatchlistSymbolBottomSheet(
    private val symbol: String,
    private val segment: String,
    private val token: String,
    private val ltp: Double,
) : BottomSheetDialogFragment() {

    private var _binding: FragmentWatchlistSymbolBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchlistSymbolBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set initial values
        binding.tvSymbol.text = symbol
        binding.tvSegment.text = segment
        binding.tvLtp.text = String.format("%.2f", ltp)

        // Start WebSocket listener only for this token
        startWebSocketListener()

        binding.btnBuy.setOnClickListener {
            val sheet = BuySellWatlistSymbolBottomSheet(symbol, segment, ltp, "BUY")
            sheet.show(parentFragmentManager, "Buy")
            dismiss()
        }

        binding.btnSell.setOnClickListener {
            val sheet = BuySellWatlistSymbolBottomSheet(symbol, segment, ltp, "SELL")
            sheet.show(parentFragmentManager, "Sell")
            dismiss()
        }
    }

    private fun startWebSocketListener() {
        AppWebSocketManager.setMarketListener { json ->

            if (json.optString("type") != "market_data") return@setMarketListener
            val data = json.optJSONObject("data") ?: return@setMarketListener

            // Identify token type: INDEX/MCX uses "Token", Crypto uses "instrument_token"
            val incomingToken = when {
                data.has("Token") -> data.optLong("Token").toString()
                data.has("instrument_token") -> data.optString("instrument_token")
                else -> return@setMarketListener
            }

            if (incomingToken != token) return@setMarketListener

            val newLtp = data.optDouble("LTP")

            val bid = data.optDouble("bid")
            val ask = data.optDouble("ask")
            val high = data.optDouble("H")
            val low = data.optDouble("L")
            val open = data.optDouble("O")
            val close = data.optDouble("C")

            val change = newLtp - close
            val percent = if (close != 0.0) (change / close) * 100 else 0.0

            requireActivity().runOnUiThread {
                // Update LTP
                binding.tvLtp.text = String.format("%.2f", newLtp)

                // Bid / Ask
                binding.tvBid.text = "Bid: $bid"
                binding.tvAsk.text = "Ask: $ask"

                // OHLC
                binding.tvOpen.text = "O: $open"
                binding.tvHigh.text = "H: $high"
                binding.tvLow.text = "L: $low"
                binding.tvClose.text = "C: $close"

                // Change text
                binding.tvChange.text =
                    String.format("%.2f (%.2f%%)", change, percent)

                // Apply color
                val color = if (change >= 0)
                    ContextCompat.getColor(requireContext(), R.color.green)
                else
                    ContextCompat.getColor(requireContext(), R.color.red)

                binding.tvLtp.setTextColor(color)
                binding.tvChange.setTextColor(color)
            }
        }

        // Send subscribe message for this token
        val subscribe = JSONObject().apply {
            put("type", "subscribe")
            put("tokens", listOf(token))
        }
        AppWebSocketManager.sendMessage(subscribe.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        AppWebSocketManager.setMarketListener(null)
        _binding = null
    }
}

