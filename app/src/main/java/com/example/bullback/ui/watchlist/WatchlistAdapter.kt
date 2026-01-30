package com.example.bullback.ui.watchlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bullback.data.model.watchlist.WatchlistSymbol
import com.example.bullback.databinding.ItemWatchlistBinding
import android.graphics.Color
import android.view.View

class WatchlistAdapter(
    private val onItemClicked: (symbol: String, segment: String, token: String, ltp: Double) -> Unit
) : ListAdapter<WatchlistSymbol, WatchlistAdapter.ViewHolder>(DiffCallback()) {

    private val livePrices = mutableMapOf<String, WatchlistSymbol>()

    // Delete mode
    var isDeleteMode: Boolean = false
    private val selectedTokens = mutableSetOf<String>()

    // Callback for fragment
    var onSelectionChanged: (() -> Unit)? = null

    inner class ViewHolder(val binding: ItemWatchlistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WatchlistSymbol) {

            // Show checkbox in delete mode
            binding.cbSelectAll.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
            binding.cbSelectAll.setOnCheckedChangeListener(null)
            binding.cbSelectAll.isChecked = selectedTokens.contains(item.token)

            // Checkbox listener
            binding.cbSelectAll.setOnCheckedChangeListener { _, checked ->
                if (checked) selectedTokens.add(item.token)
                else selectedTokens.remove(item.token)
                onSelectionChanged?.invoke()
            }

            // Bind data
            binding.tvSymbol.text = item.symbol
            binding.tvTag.text = item.exchange
            binding.tvData.text = if (item.expiryDate.isNotEmpty()) item.expiryDate else item.segment
            binding.tvLtp.text = "LTP: %.2f".format(item.lastPrice)
            binding.tvPrice.text = "%.2f".format(item.lastPrice)
            binding.tvChange.text = if (item.change >= 0)
                "+%.2f (%.2f%%)".format(item.change, item.changePercent)
            else
                "%.2f (%.2f%%)".format(item.change, item.changePercent)
            binding.tvClosePrice.text = "Close: %.2f".format(item.closePrice)

            val color = if (item.change >= 0) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
            binding.tvPrice.setTextColor(color)
            binding.tvChange.setTextColor(color)

            // Item click
            binding.root.setOnClickListener {
                if (isDeleteMode) {
                    binding.cbSelectAll.isChecked = !binding.cbSelectAll.isChecked
                } else {
                    onItemClicked(item.symbol, item.segment, item.token, item.lastPrice)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWatchlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = livePrices[currentList[position].token] ?: currentList[position]
        holder.bind(item)
    }

    fun updateLivePrice(token: String, ltp: Double, change: Double, changePercent: Double, closePrice: Double) {
        // Try exact token match first
        var index = currentList.indexOfFirst { it.token == token }

        // If not found, try instrument_token (for backward compatibility)
        if (index == -1) {
            index = currentList.indexOfFirst { it.instrumentToken == token }
        }

        if (index == -1) return

        val item = currentList[index].copy(
            lastPrice = ltp,
            change = change,
            changePercent = changePercent,
            closePrice = closePrice
        )
        livePrices[token] = item
        notifyItemChanged(index)
    }

    // Add a new method to update by symbol
    fun updateLivePriceBySymbol(symbol: String, ltp: Double, change: Double, changePercent: Double, closePrice: Double) {
        // Find item by script or symbol field
        val index = currentList.indexOfFirst {
            it.script.equals(symbol, ignoreCase = true) ||
                    it.symbol.equals(symbol, ignoreCase = true)
        }

        if (index == -1) {
            // Try matching instrument_token if available
            val fallbackIndex = currentList.indexOfFirst {
                it.instrumentToken.equals(symbol, ignoreCase = true)
            }
            if (fallbackIndex == -1) return
            updateItem(fallbackIndex, ltp, change, changePercent, closePrice)
        } else {
            updateItem(index, ltp, change, changePercent, closePrice)
        }
    }

    private fun updateItem(index: Int, ltp: Double, change: Double, changePercent: Double, closePrice: Double) {
        val item = currentList[index].copy(
            lastPrice = ltp,
            change = change,
            changePercent = changePercent,
            closePrice = closePrice
        )
        // Use token as key for caching
        livePrices[item.token] = item
        notifyItemChanged(index)
    }

    // Delete helpers
    fun selectAll() {
        selectedTokens.clear()
        selectedTokens.addAll(currentList.map { it.token })
        notifyDataSetChanged()
        onSelectionChanged?.invoke()
    }

    fun clearSelection() {
        selectedTokens.clear()
        notifyDataSetChanged()
        onSelectionChanged?.invoke()
    }

    fun getSelectedTokens(): List<String> = selectedTokens.toList()

    class DiffCallback : DiffUtil.ItemCallback<WatchlistSymbol>() {
        override fun areItemsTheSame(old: WatchlistSymbol, new: WatchlistSymbol) = old.token == new.token
        override fun areContentsTheSame(old: WatchlistSymbol, new: WatchlistSymbol) =
            old.symbol == new.symbol &&
                    old.segment == new.segment &&
                    old.lastPrice == new.lastPrice &&
                    old.change == new.change &&
                    old.changePercent == new.changePercent &&
                    old.closePrice == new.closePrice
    }
}


