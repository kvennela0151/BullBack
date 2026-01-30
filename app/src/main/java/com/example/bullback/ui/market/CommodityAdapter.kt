package com.example.bullback.ui.market

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color
import com.example.bullback.R
import com.example.bullback.data.model.auth.CommodityData



class CommodityAdapter(
    private var commodities: List<CommodityData>
) : RecyclerView.Adapter<CommodityAdapter.CommodityViewHolder>() {

    class CommodityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvCommodityName)
        val price: TextView = view.findViewById(R.id.tvCommodityPrice)
        val change: TextView = view.findViewById(R.id.tvCommodityChange)
        val percent: TextView = view.findViewById(R.id.tvCommodityChangePercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommodityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_commodity, parent, false)
        return CommodityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommodityViewHolder, position: Int) {
        val item = commodities[position]

        holder.name.text = item.name
        holder.price.text =
            if (item.lastPrice > 0) String.format("%.2f", item.lastPrice) else "--"

        holder.change.text =
            if (item.change >= 0) "+%.2f".format(item.change) else "%.2f".format(item.change)

        holder.percent.text =
            if (item.changePercent >= 0)
                "+%.2f%%".format(item.changePercent)
            else
                "%.2f%%".format(item.changePercent)

        val color =
            if (item.change >= 0) Color.parseColor("#2E7D32")
            else Color.parseColor("#C62828")

        holder.change.setTextColor(color)
        holder.percent.setTextColor(color)
    }

    override fun getItemCount(): Int = commodities.size

    fun updateData(newList: List<CommodityData>) {
        commodities = newList
        notifyDataSetChanged()
    }

    fun updateLivePrice(token: String, ltp: Double, change: Double,
                        changePercent: Double) {
        val index = commodities.indexOfFirst {
            it.instrumentToken == token
        }

        if (index == -1) return

        val item = commodities[index]
        item.lastPrice = ltp
        item.change = change
        item.changePercent = changePercent

        notifyItemChanged(index)
    }
}
