package com.example.bullback.ui.market

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color
import com.example.bullback.R
import com.example.bullback.data.model.auth.CommodityData


class CommodityAdapter(private var commodities: List<CommodityData>) :
    RecyclerView.Adapter<CommodityAdapter.CommodityViewHolder>() {

    class CommodityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCommodityName: TextView = view.findViewById(R.id.tvCommodityName)
        val tvCommodityPrice: TextView = view.findViewById(R.id.tvCommodityPrice)
        val tvCommodityChange: TextView = view.findViewById(R.id.tvCommodityChange)
        val tvCommodityChangePercent: TextView = view.findViewById(R.id.tvCommodityChangePercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommodityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_commodity, parent, false)
        return CommodityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommodityViewHolder, position: Int) {
        val commodity = commodities[position]

        // Display commodity name (e.g., "GOLD")
        holder.tvCommodityName.text = commodity.name

        // Display last price
        holder.tvCommodityPrice.text = if (commodity.lastPrice > 0) {
            String.format("%.2f", commodity.lastPrice)
        } else {
            "N/A"
        }

        // Format change with + or - sign
        val changeText = if (commodity.change >= 0) {
            "+${String.format("%.2f", commodity.change)}"
        } else {
            String.format("%.2f", commodity.change)
        }
        holder.tvCommodityChange.text = changeText

        // Format percentage
        val percentText = if (commodity.changePercent >= 0) {
            "+${String.format("%.2f", commodity.changePercent)}%"
        } else {
            "${String.format("%.2f", commodity.changePercent)}%"
        }
        holder.tvCommodityChangePercent.text = percentText

        // Set color based on positive or negative change
        val color = if (commodity.change >= 0) {
            Color.parseColor("#4CAF50") // Green
        } else {
            Color.parseColor("#F44336") // Red
        }
        holder.tvCommodityChange.setTextColor(color)
        holder.tvCommodityChangePercent.setTextColor(color)
    }

    override fun getItemCount() = commodities.size

    fun updateData(newCommodities: List<CommodityData>) {
        commodities = newCommodities
        notifyDataSetChanged()
    }
}