package com.example.bullback.ui.profile.wallet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bullback.R
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.WalletApiService
import com.example.bullback.data.repository.WalletRepository
import com.example.bullback.databinding.FragmentWalletBinding
import com.google.android.material.tabs.TabLayout

class WalletFragment : Fragment(R.layout.fragment_wallet) {

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WalletViewModel
    private lateinit var adapter: WalletTransactionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWalletBinding.bind(view)

        setupViewModel()
        setupRecyclerView()
        setupTabs()

        // Default tab load
        viewModel.loadTransactions("DEPOSIT")

        observeViewModel()
    }

    private fun setupViewModel() {
        // ✅ CORRECT WAY
        val api = RetrofitClient.createService(WalletApiService::class.java)

        val repository = WalletRepository(api)
        val factory = WalletViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[WalletViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = WalletTransactionAdapter()
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter
        binding.rvTransactions.isNestedScrollingEnabled = false
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun setupTabs() {
        binding.tabWallet.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> viewModel.loadTransactions("DEPOSIT")
                    1 -> viewModel.loadTransactions("WITHDRAWAL")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
