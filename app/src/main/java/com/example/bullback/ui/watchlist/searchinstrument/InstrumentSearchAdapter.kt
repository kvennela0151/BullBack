package com.example.bullback.ui.watchlist.searchinstrument

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bullback.data.model.auth.watchlist.addwatchlist.AddWatchlistRequest
import com.example.bullback.data.model.auth.watchlist.instrumentsearch.SearchInstrument
import com.example.bullback.databinding.InstrumentListItemBinding

class InstrumentSearchAdapter(
    private val segment: String,
    private val onAddClicked: (AddWatchlistRequest) -> Unit
) : RecyclerView.Adapter<InstrumentSearchAdapter.VH>() {

    private val items = mutableListOf<SearchInstrument>()

    fun submitList(list: List<SearchInstrument>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val binding: InstrumentListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchInstrument) {
            // Set symbol and meta info
            binding.tvSymbol.text = item.tradingsymbol
            binding.tvMeta.text = "${item.segment} · ${item.expiryDate ?: item.instrumentType}"

            // Update button text based on isAdded state
            binding.btnAdd.apply {
                text = if (item.isAdded) "Added" else "Add"
                isEnabled = !item.isAdded
            }

            binding.btnAdd.setOnClickListener {
                if (!item.isAdded) {
                    // Create AddWatchlistRequest
                    val request = AddWatchlistRequest(
                        script = item.name, // This should be the script name
                        symbol = item.tradingsymbol,
                        token = item.instrumentToken?.toString() ?: item.exchange,
                        instrumentType = item.instrumentType,
                        segment = segment, // Use the segment passed to adapter
                        exchange = item.exchange,
                        expiryDate = item.expiryDate ?: "",
                        lotSize = item.lotSize,
                        strike = item.strike
                    )
                    onAddClicked(request)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = InstrumentListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}