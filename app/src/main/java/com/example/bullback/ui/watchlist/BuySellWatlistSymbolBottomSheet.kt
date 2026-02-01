package com.example.bullback.ui.watchlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bullback.databinding.FragmentBuySellWatlistSymbolBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BuySellWatlistSymbolBottomSheet(
    private val symbol: String,
    private val segment: String,
    private val ltp: Double,
    private val orderType: String // "BUY" or "SELL"
) : BottomSheetDialogFragment() {

    private var _binding: FragmentBuySellWatlistSymbolBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuySellWatlistSymbolBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSymbol.text = symbol
        binding.tvSegment.text = segment
        binding.etPrice.setText(ltp.toString())

        // Toggle Stoploss & Target fields
        binding.switchSL.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutStoplossTarget.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Place Order
        binding.btnPlaceOrder.text = orderType
        binding.btnPlaceOrder.setOnClickListener {
            val price = binding.etPrice.text.toString()
            val lots = binding.etLots.text.toString()
            val stoploss = binding.etStoploss.text.toString()
            val target = binding.etTarget.text.toString()

            // TODO: Call API or handle BUY/SELL order
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
