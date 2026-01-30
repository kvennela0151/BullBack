package com.example.bullback.ui.watchlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.bullback.R
import com.example.bullback.data.model.watchlist.WatchlistSymbol
import com.example.bullback.databinding.FragmentWatchlistSymbolBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WatchlistSymbolBottomSheet(
    private val symbol: String,
    private val segment: String,
    private val ltp: Double
) : BottomSheetDialogFragment() {

    private var _binding: FragmentWatchlistSymbolBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchlistSymbolBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSymbol.text = symbol
        binding.tvSegment.text = segment
        binding.tvLtp.text = ltp.toString()

        binding.btnBuy.setOnClickListener {
            val buySellSheet = BuySellWatlistSymbolBottomSheet(symbol, segment, ltp, "BUY")
            buySellSheet.show(parentFragmentManager, "BuySellBottomSheet")
            dismiss()
        }

        binding.btnSell.setOnClickListener {
            val buySellSheet = BuySellWatlistSymbolBottomSheet(symbol, segment, ltp, "SELL")
            buySellSheet.show(parentFragmentManager, "BuySellBottomSheet")
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
