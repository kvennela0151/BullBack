package com.example.bullback.ui.watchlist.searchinstrument

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bullback.data.model.auth.watchlist.instrumentsearch.SearchInstrument
import com.example.bullback.databinding.InstrumentListItemBinding

class InstrumentSearchAdapter(
    private val onAddClicked: (SearchInstrument) -> Unit
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
            binding.tvSymbol.text = item.symbol
            binding.tvMeta.text = "${item.segment} · ${item.expiryDate ?: ""}"

            // Update button based on isAdded state
            binding.btnAdd.apply {
                text = if (item.isAdded) "Added" else "Add"
                isEnabled = !item.isAdded

                setOnClickListener {
                    if (!item.isAdded) {
                        onAddClicked(item)
                    }
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

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size
}
