package com.example.bullback.ui.profile.marginSettings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bullback.data.model.auth.MarginUiModel
import androidx.recyclerview.widget.ListAdapter
import com.example.bullback.databinding.ItemMarginSettingBinding


class MarginSettingsAdapter(
    private var items: List<MarginUiModel> = emptyList()
) : RecyclerView.Adapter<MarginSettingsAdapter.MarginViewHolder>() {

    inner class MarginViewHolder(val binding: ItemMarginSettingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarginViewHolder {
        val binding = ItemMarginSettingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MarginViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MarginViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvExchange.text = item.exchange
            tvTradingAllowed.text = if (item.tradingAllowed) "Yes" else "No"
            tvIntraday.text = item.intraday.toString()
            tvHoldingLeverage.text = item.holding.toString()
            tvStrikeRange.text = item.strikeRange.toString()
            tvMaxLot.text = item.maxLot.toString()
            tvMaxOrderLot.text = item.maxOrderLot.toString()
            tvCommissionType.text = item.commissionType
            tvCommissionValue.text = item.commissionValue
        }
    }

    override fun getItemCount(): Int = items.size

    // ✅ Add this function
    fun updateList(newList: List<MarginUiModel>) {
        items = newList
        notifyDataSetChanged()
    }
}