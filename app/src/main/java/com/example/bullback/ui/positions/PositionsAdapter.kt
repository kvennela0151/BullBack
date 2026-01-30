package com.example.bullback.ui.positions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bullback.R
import com.example.bullback.data.model.positions.PositionsItem

// PositionsAdapter.kt
class PositionsAdapter : RecyclerView.Adapter<PositionsAdapter.PositionViewHolder>() {

    private val items = mutableListOf<PositionsItem>()

    fun submitList(list: List<PositionsItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
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
            tvQtyPrice.text = "${item.netQuantity} x ${item.averagePrice}"
            tvLtp.text = "Avg: ${item.averagePrice}"

            val pnl = item.realizedPnl.toDoubleOrNull() ?: 0.0
            tvPnl.text = String.format("%.2f", pnl)
            tvPnl.setTextColor(if (pnl >= 0) Color.parseColor("#2E7D32") else Color.parseColor("#C62828"))

            tvBuyBadge.text = if (item.side == "BUY") "B" else "S"
        }
    }
}
