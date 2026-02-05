package com.example.bullback.ui.profile.tradelogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.TradeApiService
import com.example.bullback.data.repository.TradeRepository
import com.example.bullback.databinding.FragmentTradeLogsBinding
import android.app.DatePickerDialog
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.widget.ListPopupWindow
import java.util.Calendar

class TradeLogsFragment : Fragment() {

    private lateinit var binding: FragmentTradeLogsBinding

    private val viewModel: TradeLogsViewModel by viewModels {
        TradeLogsVMFactory(
            TradeRepository(
                RetrofitClient.createService(TradeApiService::class.java)
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTradeLogsBinding.inflate(inflater, container, false)

        setupRecycler()
        setupObservers()
        setupClickListeners()

        viewModel.loadClosedTrades()   // initial load

        return binding.root
    }

    /* -------------------- SETUP FUNCTIONS -------------------- */

    private fun setupRecycler() {
        binding.tradeLogsRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupObservers() {
        viewModel.tradeLogs.observe(viewLifecycleOwner) { list ->
            binding.tradeLogsRecycler.adapter = TradeLogsAdapter(list)
        }
    }

    private fun setupClickListeners() {

        // Open Calendar
        binding.startDate.setOnClickListener { openCalendar(binding.startDate) }
        binding.endDate.setOnClickListener { openCalendar(binding.endDate) }

        // Dropdown (20 / 50 / 100)
        binding.resultsPerPage.setOnClickListener { showDropdown(binding.resultsPerPage) }

        // Apply filter
        binding.btnApply.setOnClickListener {
            val start = binding.startDate.text.toString()
            val end = binding.endDate.text.toString()
            val limit = binding.resultsPerPage.text.toString().toIntOrNull() ?: 20

            viewModel.loadFilteredTrades(start, end, limit)
        }

        // Download button (extend later)
        binding.btnDownload.setOnClickListener {
            // TODO: implement download
        }
    }

    /* -------------------- UI HELPERS -------------------- */

    private fun openCalendar(target: EditText) {
        val c = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selected = "$day-${month + 1}-$year"
                target.setText(selected)
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showDropdown(target: EditText) {
        val options = listOf("20", "50", "100")

        val popup = ListPopupWindow(requireContext())
        popup.anchorView = target
        popup.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, options)
        )
        popup.setOnItemClickListener { _, _, pos, _ ->
            target.setText(options[pos])
            popup.dismiss()
        }
        popup.show()
    }
}
