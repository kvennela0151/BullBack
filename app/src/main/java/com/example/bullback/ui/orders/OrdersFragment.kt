package com.example.bullback.ui.orders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bullback.data.model.auth.Order
import com.example.bullback.databinding.FragmentOrdersBinding
import com.example.bullback.utlis.Resource
import com.google.android.material.tabs.TabLayout


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

        setupTabs()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupTabs() {
        binding.tabLayout.apply {
            addTab(newTab().setText("Open"))
            addTab(newTab().setText("Executed"))
            addTab(newTab().setText("Rejected"))

            //load orders
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when(tab?.position){
                        0 -> viewModel.loadOrdersByType("OPEN")
                        1 -> viewModel.loadOrdersByType("EXECUTED")
                        2 -> viewModel.loadOrdersByType("REJECTED")
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }

        // Load default tab (OPEN)
        viewModel.loadOrdersByType("OPEN")
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter { order ->
            onOrderClicked(order)
        }

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ordersAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {

        // Observe OPEN orders list
        viewModel.filteredOrders.observe(viewLifecycleOwner) { orders ->
            ordersAdapter.submitList(orders)
        }

        viewModel.orders.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Error) {
                Toast.makeText(
                    requireContext(),
                    resource.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun onOrderClicked(order: Order) {
        Toast.makeText(
            requireContext(),
            "${order.symbol} - ${order.status}",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
