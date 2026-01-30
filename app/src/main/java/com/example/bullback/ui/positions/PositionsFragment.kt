package com.example.bullback.ui.positions

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bullback.R
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.repository.PositionsRepository
import com.example.bullback.databinding.FragmentPositionsBinding
import com.google.android.material.tabs.TabLayout

// PositionsFragment.kt
class PositionsFragment : Fragment(R.layout.fragment_positions) {

    private var _binding: FragmentPositionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PositionsViewModel
    private lateinit var adapter: PositionsAdapter

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPositionsBinding.bind(view)

        setupViewModel()
        setupRecyclerView()
        setupTabLayout()
        observeData()

        // Load default tab (Open)
        viewModel.setStatus("OPEN")
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[PositionsViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = PositionsAdapter()
        binding.rvPositions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPositions.adapter = adapter
    }

    private fun setupTabLayout() {
        binding.tabLayoutPositions.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text) {
                    "Open" -> viewModel.setStatus("OPEN")
                    "Active" -> viewModel.setStatus("ACTIVE")
                    "Closed" -> viewModel.setStatus("CLOSED")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeData() {
        viewModel.positions.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.openPnl.observe(viewLifecycleOwner) {
            binding.tvOpenPnl.text = String.format("%.2f", it)
            binding.tvOpenPnl.setTextColor(
                if (it >= 0) Color.parseColor("#2E7D32")
                else Color.parseColor("#C62828")
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

