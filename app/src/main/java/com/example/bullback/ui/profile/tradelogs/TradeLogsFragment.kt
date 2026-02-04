package com.example.bullback.ui.profile.tradelogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bullback.R
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.TradeApiService
import com.example.bullback.data.repository.TradeRepository
import com.example.bullback.databinding.FragmentTradeLogsBinding

class TradeLogsFragment : Fragment() {

    private lateinit var binding: FragmentTradeLogsBinding
    private val viewModel: TradeLogsViewModel by viewModels {
        TradeLogsVMFactory(TradeRepository(RetrofitClient.createService(TradeApiService::class.java)))
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTradeLogsBinding.inflate(inflater, container, false)

        binding.tradeLogsRecycler.layoutManager = LinearLayoutManager(requireContext())

        viewModel.tradeLogs.observe(viewLifecycleOwner) {
            binding.tradeLogsRecycler.adapter = TradeLogsAdapter(it)
        }

        viewModel.loadClosedTrades()

        return binding.root
    }
}
