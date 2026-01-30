package com.example.bullback.ui.profile.wallet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bullback.R
import com.example.bullback.data.model.wallet.WalletTransaction
import java.text.SimpleDateFormat
import java.util.Locale

class WalletTransactionAdapter :
    RecyclerView.Adapter<WalletTransactionAdapter.TransactionViewHolder>() {

    private val items = mutableListOf<WalletTransaction>()

    fun submitList(list: List<WalletTransaction>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvRemark: TextView = view.findViewById(R.id.tvRemark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallet_overview, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = items[position]

        // Type
        holder.tvType.text = item.type

        // Amount
        holder.tvAmount.text = "₹${item.amount}"

        // Status
        holder.tvStatus.text = item.status

        // Date formatting
        holder.tvDate.text = formatDate(item.createdAt)

        // Remark
        if (item.adminRemark.isNullOrEmpty()) {
            holder.tvRemark.visibility = View.GONE
        } else {
            holder.tvRemark.visibility = View.VISIBLE
            holder.tvRemark.text = "Remark: ${item.adminRemark}"
        }

        // Icon & colors based on type
        if (item.type == "DEPOSIT") {
            holder.ivIcon.setImageResource(R.drawable.ic_arrow_down)
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")) // green
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_arrow_right)
            holder.tvStatus.setTextColor(Color.parseColor("#D32F2F")) // red
        }
    }

    override fun getItemCount(): Int = items.size

    private fun formatDate(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            input
        }
    }
}
