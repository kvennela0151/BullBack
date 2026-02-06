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

    private var isSubscribed = false

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

        binding.tvSymbol.text = symbol
        binding.tvSegment.text = segment
        binding.tvLtp.text = String.format("%.2f", ltp)

        subscribeToUpdates()

        binding.btnBuy.setOnClickListener {
            val exchange = getExchangeFromSegment(segment)
            val sheet = BuySellWatlistSymbolBottomSheet(
                symbol,
                segment,
                ltp,
                "BUY",
                token,
                exchange
            )
            sheet.show(parentFragmentManager, "BuySheet")
        }

        binding.btnSell.setOnClickListener {
            val exchange = getExchangeFromSegment(segment)
            val sheet = BuySellWatlistSymbolBottomSheet(
                symbol,
                segment,
                ltp,
                "SELL",
                token,
                exchange
            )
            sheet.show(parentFragmentManager, "SellSheet")
        }
    }

    private fun getExchangeFromSegment(segment: String): String {
        return when {
            segment.contains("INDEX-FUT", ignoreCase = true) -> "NFO"
            segment.contains("NFO", ignoreCase = true) -> "NFO"
            segment.contains("NSE", ignoreCase = true) -> "NSE"
            segment.contains("BSE", ignoreCase = true) -> "BSE"
            segment.contains("MCX", ignoreCase = true) -> "MCX"
            segment.contains("CRYPTO", ignoreCase = true) -> "CRYPTO"
            segment.contains("COMEX", ignoreCase = true) -> "COMEX"
            else -> "NSE"
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
        val ltp = when {
            data.has("LTP") -> data.optDouble("LTP")
            data.has("ltp") -> data.optDouble("ltp")
            data.has("last_price") -> data.optDouble("last_price")
            else -> 0.0
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
            change = ltp - previousClose
            changePercent = if (previousClose != 0.0) (change / previousClose) * 100 else 0.0
        }

        val expiryDate = data.optString("expiry_date", "")

        requireActivity().runOnUiThread {
            val decimalPlaces = when {
                ltp < 1.0 -> 6
                ltp < 10.0 -> 5
                ltp < 100.0 -> 5
                ltp < 1000.0 -> 2
                else -> 2
            }

            binding.tvLtp.text = String.format("%.${decimalPlaces}f", ltp)
            binding.tvBid.text = if (bid > 0) "Bid: %.${decimalPlaces}f".format(bid) else "Bid: -"
            binding.tvAsk.text = if (ask > 0) "Ask: %.${decimalPlaces}f".format(ask) else "Ask: -"
            binding.tvOpen.text = "O: %.${decimalPlaces}f".format(open)
            binding.tvHigh.text = "H: %.${decimalPlaces}f".format(high)
            binding.tvLow.text = "L: %.${decimalPlaces}f".format(low)
            binding.tvClose.text = "C: %.${decimalPlaces}f".format(close)

            if (expiryDate.isNotEmpty()) {
                binding.tvExpiry.text = expiryDate
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
                ContextCompat.getColor(requireContext(), R.color.green)
            else
                ContextCompat.getColor(requireContext(), R.color.red)

            binding.tvLtp.setTextColor(color)
            binding.tvChange.setTextColor(color)
            binding.tvBid.setTextColor(ContextCompat.getColor(requireContext(), R.color.button_green_dark))
            binding.tvAsk.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
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