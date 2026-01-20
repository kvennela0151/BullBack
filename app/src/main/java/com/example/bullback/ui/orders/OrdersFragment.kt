package com.example.bullback.ui.orders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bullback.MainActivity
import com.example.bullback.R
import com.example.bullback.data.model.auth.Order
import com.example.bullback.data.model.auth.OrderStatus
import com.example.bullback.databinding.FragmentOrdersBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch


class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrdersViewModel by viewModels()
    private lateinit var ordersAdapter: OrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabLayout()
        observeViewModel()
        loadOrders()
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter { order ->
            showOrderClickFeedback(order)
        }

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ordersAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.onTabSelected(OrderStatus.OPEN)
                    1 -> viewModel.onTabSelected(OrderStatus.EXECUTED)
                    2 -> viewModel.onTabSelected(OrderStatus.REJECTED)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedTab.observe(viewLifecycleOwner) { status ->
                updateTabSelection(status)
            }
        }
    }

    private fun updateTabSelection(status: OrderStatus) {
        when (status) {
            OrderStatus.OPEN -> binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
            OrderStatus.EXECUTED -> binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1))
            OrderStatus.REJECTED -> binding.tabLayout.selectTab(binding.tabLayout.getTabAt(2))
        }
    }


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.orders.observe(viewLifecycleOwner) { resource ->
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredOrders.observe(viewLifecycleOwner) { orders ->
                ordersAdapter.submitList(orders)
            }
        }
    }

    private fun loadOrders() {
        viewModel.loadOrders()
    }

    private fun showOrderClickFeedback(order: Order) {
        Toast.makeText(
            requireContext(),
            "${order.symbol} - ${order.transactionType} (${order.status})",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
