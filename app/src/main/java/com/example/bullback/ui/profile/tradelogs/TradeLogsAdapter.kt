package com.example.bullback.ui.profile.tradelogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bullback.R
import com.example.bullback.data.model.tradelogs.TradeItem
import java.text.SimpleDateFormat
import java.util.Locale

class TradeLogsAdapter(
    private val list: List<TradeItem>
) : RecyclerView.Adapter<TradeLogsAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvSymbol = view.findViewById<TextView>(R.id.tvSymbol)
        val tvTypeTag = view.findViewById<TextView>(R.id.tvTypeTag)
        val tvPnL = view.findViewById<TextView>(R.id.tvPnL)
        val tvBuySell = view.findViewById<TextView>(R.id.tvBuySell)
        val tvEntry = view.findViewById<TextView>(R.id.tvEntry)
        val tvTradeDate = view.findViewById<TextView>(R.id.tvTradeDate)
        val tvExit = view.findViewById<TextView>(R.id.tvExit)
        val tvQty = view.findViewById<TextView>(R.id.tvQty)
        val tvBrokerage = view.findViewById<TextView>(R.id.tvBrokerage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflate = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trade_overview, parent, false)
        return ViewHolder(inflate)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvSymbol.text = item.symbol
        holder.tvTypeTag.text = item.exchange
        holder.tvPnL.text = "₹${item.realized_pnl}"

        // Profit / Loss color
        if (item.realized_pnl.toDouble() >= 0) {
            holder.tvPnL.setTextColor(Color.parseColor("#24C469"))
        } else {
            holder.tvPnL.setTextColor(Color.parseColor("#FF4B55"))
        }

        // Buy / Sell
        holder.tvBuySell.text = if (item.side == "BUY") "B" else "S"
        holder.tvBuySell.backgroundTintList = ColorStateList.valueOf(
            if (item.side == "BUY") Color.parseColor("#128C4F") else Color.parseColor("#E53935")
        )

        // Entry
        val qty = item.buy_quantity
        holder.tvEntry.text = "$qty   @   ₹${item.buy_price}"

        // Exit
        holder.tvExit.text = "Exit @ ₹${item.sell_price}"

        // Qty
        holder.tvQty.text = "Qty: ${item.buy_quantity}"

        // Brokerage
        holder.tvBrokerage.text = "Brokerage: ₹${item.brokerage}"

        // Date formatting
        holder.tvTradeDate.text = formatDate(item.updated_at)
    }

    private fun formatDate(apiDate: String): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val output = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val date = input.parse(apiDate)
            output.format(date!!)
        } catch (e: Exception) {
            apiDate
        }
    }
}
