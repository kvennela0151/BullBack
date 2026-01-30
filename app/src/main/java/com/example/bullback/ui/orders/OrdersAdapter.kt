package com.example.bullback.ui.orders

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bullback.R
import com.example.bullback.data.model.auth.Order
import com.example.bullback.databinding.ItemOrdersOpenOverviewBinding
import java.text.SimpleDateFormat
import java.util.*

class OrdersAdapter(
    private val onOrderClick: (Order) -> Unit = {}
) : ListAdapter<Order, OrdersAdapter.OrderViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrdersOpenOverviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(
        private val binding: ItemOrdersOpenOverviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) = with(binding) {

            // Symbol
            tvSymbol.text = order.symbol

            // Segment
            tvSegment.text = when (order.exchange) {
                "NSE", "BSE" -> "EQUITY"
                "NFO" -> "NFO"
                "MCX" -> "COMMODITY"
                "CRYPTO" -> "CRYPTO"
                else -> order.exchange
            }

            tvSegment.setBackgroundResource(R.drawable.bg_symbol)



            // Status (STRING BASED)
            tvStatus.text = order.status
            when (order.status.uppercase()) {
                "OPEN" -> {
                    tvStatus.setTextColor(Color.parseColor("#FF9800"))
                    tvStatus.setBackgroundResource(R.drawable.bg_status_open)
                }
                "EXECUTED", "FILLED", "COMPLETE" -> {
                    tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                    tvStatus.setBackgroundResource(R.drawable.bg_status_executed)
                }
                "REJECTED", "CANCELLED", "CANCELED" -> {
                    tvStatus.setTextColor(Color.parseColor("#F44336"))
                    tvStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                }
            }

            // BUY / SELL (STRING BASED)
            when (order.transactionType.uppercase()) {
                "BUY" -> {
                    tvBuy.text = "B"
                    tvBuy.setBackgroundResource(R.drawable.bg_circle_green)
                    tvBuy.visibility = View.VISIBLE
                }
                "SELL" -> {
                    tvBuy.text = "S"
                    tvBuy.setBackgroundResource(R.drawable.bg_circle_red)
                    tvBuy.visibility = View.VISIBLE
                }
                else -> tvBuy.visibility = View.GONE
            }

            // Qty x Price
            tvQtyPrice.text = "${order.quantity} x ${order.price}"

            // Order Type
            tvOrderType.text = order.orderType

            // Date
            tvDate.text = formatDate(order.createdAt)

            // Click listener
            root.setOnClickListener {
                onOrderClick(order)
            }
        }

        private fun formatDate(dateTime: String): String {
            return try {
                val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val output = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                output.format(input.parse(dateTime) ?: Date())
            } catch (e: Exception) {
                dateTime
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}