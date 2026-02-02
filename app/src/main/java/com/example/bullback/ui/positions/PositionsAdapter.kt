package com.example.bullback.ui.positions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bullback.R
import com.example.bullback.data.model.positions.PositionsItem

class PositionsAdapter(
    private val onPositionClick: (PositionsItem) -> Unit = {}
) : RecyclerView.Adapter<PositionsAdapter.PositionViewHolder>() {

    private val items = mutableListOf<PositionsItem>()

    // Store live prices for each position
    private val livePrices = mutableMapOf<String, LivePriceData>()

    data class LivePriceData(
        val ltp: Double,
        val change: Double,
        val changePercent: Double
    )

    fun submitList(list: List<PositionsItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun getCurrentItems(): List<PositionsItem> = items.toList()

    fun getLivePriceForPosition(instrumentToken: String): Double? {
        return livePrices[instrumentToken]?.ltp
    }

    fun updateLivePrice(instrumentToken: String, ltp: Double, change: Double, changePercent: Double) {
        // Find position by instrument token
        val index = items.indexOfFirst { it.instrumentToken == instrumentToken }

        if (index != -1) {
            livePrices[instrumentToken] = LivePriceData(ltp, change, changePercent)
            notifyItemChanged(index)
        }
    }

    fun updateLivePriceBySymbol(symbol: String, ltp: Double, change: Double, changePercent: Double) {
        // Find position by symbol
        val index = items.indexOfFirst {
            it.symbol.equals(symbol, ignoreCase = true) ||
                    it.instrumentToken.equals(symbol, ignoreCase = true)
        }

        if (index != -1) {
            val item = items[index]
            livePrices[item.instrumentToken] = LivePriceData(ltp, change, changePercent)
            notifyItemChanged(index)
        }
    }

    private fun calculatePnl(position: PositionsItem): Double {
        // Parse values
        val netQty = position.netQuantity.toDoubleOrNull() ?: 0.0
        val avgPrice = position.averagePrice.toDoubleOrNull() ?: 0.0
        val realizedPnl = position.realizedPnl.toDoubleOrNull() ?: 0.0

        // 1️⃣ Closed position → backend truth wins
        if (netQty == 0.0) {
            return realizedPnl
        }

        // 2️⃣ Open position → calculate unrealized
        val liveData = livePrices[position.instrumentToken]
        val currentLtp = liveData?.ltp ?: avgPrice

        if (currentLtp == 0.0) {
            return realizedPnl
        }

        val openQty = Math.abs(netQty)
        val entryPrice = avgPrice
        var unrealized = 0.0

        // Calculate P&L based on side
        if (position.side == "BUY") {
            unrealized = (currentLtp - entryPrice) * openQty
        } else if (position.side == "SELL") {
            unrealized = (entryPrice - currentLtp) * openQty
        } else {
            // Default calculation if side is unknown
            unrealized = (currentLtp - entryPrice) * netQty
        }

        return unrealized
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PositionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_positions_overview, parent, false)
        return PositionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PositionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class PositionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSymbol: TextView = view.findViewById(R.id.tvSymbol)
        private val tvSegment: TextView = view.findViewById(R.id.tvSegment)
        private val tvPnl: TextView = view.findViewById(R.id.tvPnl)
        private val tvQtyPrice: TextView = view.findViewById(R.id.tvQtyPrice)
        private val tvLtp: TextView = view.findViewById(R.id.tvLtp)
        private val tvBuyBadge: TextView = view.findViewById(R.id.tvBuyBadge)

        fun bind(item: PositionsItem) {
            tvSymbol.text = item.symbol
            tvSegment.text = item.exchange

            // Parse values from String
            val netQty = item.netQuantity.toDoubleOrNull() ?: 0.0
            val avgPrice = item.averagePrice.toDoubleOrNull() ?: 0.0

            tvQtyPrice.text = "${netQty.toInt()} x ${String.format("%.2f", avgPrice)}"

            // Get live price if available, otherwise use average price
            val liveData = livePrices[item.instrumentToken]
            val currentLtp = liveData?.ltp ?: avgPrice

            // Display LTP with proper formatting
            val decimalPlaces = when {
                currentLtp < 1.0 -> 6
                currentLtp < 10.0 -> 5
                currentLtp < 100.0 -> 5
                currentLtp < 1000.0 -> 2
                else -> 2
            }
            tvLtp.text = "LTP: ${String.format("%.${decimalPlaces}f", currentLtp)}"

            // Calculate P&L using the proper calculation
            val pnl = calculatePnl(item)

            tvPnl.text = String.format("%.2f", pnl)
            tvPnl.setTextColor(
                if (pnl >= 0) Color.parseColor("#2E7D32")
                else Color.parseColor("#C62828")
            )

            // Buy/Sell badge
            tvBuyBadge.text = if (item.side == "BUY") "B" else "S"
            tvBuyBadge.setBackgroundResource(
                if (item.side == "BUY") R.drawable.bg_circle_green
                else R.drawable.bg_circle_red
            )

            // Set click listener
            itemView.setOnClickListener {
                onPositionClick(item)
            }
        }
    }
}