package com.example.bullback.ui.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bullback.data.model.auth.Order
import androidx.recyclerview.widget.ListAdapter
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.example.bullback.R
import com.example.bullback.data.model.auth.OrderSide
import com.example.bullback.data.model.auth.OrderStatus
import com.example.bullback.databinding.ItemOrdersOpenOverviewBinding
import java.text.SimpleDateFormat
import java.util.*


class OrdersAdapter(
    private val onOrderClick: (Order) -> Unit = {}
) : ListAdapter<Order, OrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrdersOpenOverviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding, onOrderClick)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    inner class OrderViewHolder(
        private val binding: ItemOrdersOpenOverviewBinding,
        private val onOrderClick: (Order) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var currentOrder: Order

        init {
            binding.root.setOnClickListener {
                onOrderClick(currentOrder)
            }
        }

        fun bind(order: Order) {
            currentOrder = order
            binding.apply {
                // Set symbol
                tvSymbol.text = order.symbol

                // Set segment (using exchange)
                tvSegment.text = when (order.exchange) {
                    "NSE", "BSE" -> "EQUITY"
                    "NFO" -> "F&O"
                    "MCX" -> "COMMODITY"
                    "CRYPTO" -> "CRYPTO"
                    else -> order.exchange
                }

                // Set status with colors
                tvStatus.text = order.status
                when (order.orderStatus) {
                    OrderStatus.OPEN -> {
                        tvStatus.setTextColor(Color.parseColor("#FF9800")) // Orange
                        tvStatus.setBackgroundResource(R.drawable.bg_status_open)
                    }
                    OrderStatus.EXECUTED -> {
                        tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
                        tvStatus.setBackgroundResource(R.drawable.bg_status_executed)
                    }
                    OrderStatus.REJECTED -> {
                        tvStatus.setTextColor(Color.parseColor("#F44336")) // Red
                        tvStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                    }
                }

                // Set buy/sell indicator
                when (order.orderSide) {
                    OrderSide.BUY -> {
                        tvBuy.apply {
                            text = "B"
                            setBackgroundResource(R.drawable.bg_circle_green)
                            visibility = View.VISIBLE
                        }
                    }
                    OrderSide.SELL -> {
                        tvBuy.apply {
                            text = "S"
                            setBackgroundResource(R.drawable.bg_circle_red)
                            visibility = View.VISIBLE
                        }
                    }
                }

                // Set quantity and price
                val qtyText = "${order.quantity} x ${order.price}"
                tvQtyPrice.text = qtyText

                // Set order type
                tvOrderType.text = "Order Type: ${order.orderType}"

                // Format and set date
                val formattedDate = formatDateTime(order.createdAt)
                tvDate.text = formattedDate
            }
        }

        private fun formatDateTime(dateTimeString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.getDefault())
                val date = inputFormat.parse(dateTimeString)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateTimeString
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}