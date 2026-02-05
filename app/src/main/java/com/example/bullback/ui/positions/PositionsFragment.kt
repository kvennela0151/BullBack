package com.example.bullback.ui.positions

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bullback.R
import com.example.bullback.databinding.FragmentPositionsBinding
import com.google.android.material.tabs.TabLayout
import com.example.bullback.data.model.websocket.AppWebSocketManager
import com.example.bullback.data.repository.AuthRepository
import org.json.JSONArray
import org.json.JSONObject
import android.util.Log
import com.example.bullback.data.model.positions.PositionsItem

class PositionsFragment : Fragment(R.layout.fragment_positions) {

    private var _binding: FragmentPositionsBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: PositionsViewModel
    private lateinit var adapter: PositionsAdapter
    private lateinit var authRepository: AuthRepository

    private var currentTokens: List<String> = emptyList()
    private var currentTab: String = "OPEN" // Track current tab

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPositionsBinding.bind(view)

        authRepository = AuthRepository.getInstance(requireContext())

        setupViewModel()
        setupRecyclerView()
        setupTabLayout()
        setupExitAllButton()
        observeData()

        // Load default tab (Open)
        viewModel.setStatus("OPEN")
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[PositionsViewModel::class.java]
    }

    private fun setupRecyclerView() {
        // Pass click listener to adapter
        adapter = PositionsAdapter { position ->
            onPositionClicked(position)
        }
        binding.rvPositions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPositions.adapter = adapter
    }

    private fun setupTabLayout() {
        binding.tabLayoutPositions.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text) {
                    "Open" -> {
                        currentTab = "OPEN"
                        viewModel.setStatus("OPEN")
                        showOpenPnlCard(true)
                    }
                    "Active" -> {
                        currentTab = "ACTIVE"
                        viewModel.setStatus("ACTIVE")
                        showOpenPnlCard(false)
                    }
                    "Closed" -> {
                        currentTab = "CLOSED"
                        viewModel.setStatus("CLOSED")
                        showOpenPnlCard(false)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showOpenPnlCard(show: Boolean) {
        binding.cardOpenPnl.visibility = if (show) View.VISIBLE else View.GONE

        // Also adjust the RecyclerView constraints based on PNL card visibility
        val params = binding.rvPositions.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams

        if (show) {
            // When PNL card is visible, position RecyclerView below it
            params.topToBottom = R.id.cardOpenPnl
            params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
        } else {
            // When PNL card is hidden, position RecyclerView below TabLayout
            params.topToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
            params.topToTop = R.id.tabLayoutPositions
        }

        binding.rvPositions.layoutParams = params
    }

    private fun setupExitAllButton() {
        binding.btnExitAll.setOnClickListener {
            // Show confirmation dialog first
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Exit All Positions?")
                .setMessage("Are you sure you want to squareoff all positions?")
                .setPositiveButton("Exit All") { _, _ ->
                    exitAllPositions()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun exitAllPositions() {
        // Loop through all positions and place market sell orders
        Log.d("PositionsFragment", "Exit all positions clicked")
    }

    private fun observeData() {
        viewModel.positions.observe(viewLifecycleOwner) { positions ->
            adapter.submitList(positions)

            // Subscribe to WebSocket for all positions
            subscribeWebSocket(positions.map { it.instrumentToken })

            // Update total PNL only if in Open tab
            if (currentTab == "OPEN") {
                updateTotalPnL()
            }
        }

        viewModel.totalPnl.observe(viewLifecycleOwner) { pnl ->
            binding.tvOpenPnl.text = String.format("%.2f", pnl)
            binding.tvOpenPnl.setTextColor(
                if (pnl >= 0) Color.parseColor("#2E7D32")
                else Color.parseColor("#C62828")
            )
        }
    }

    private fun onPositionClicked(position: com.example.bullback.data.model.positions.PositionsItem) {
        // Open bottom sheet with position details
        val bottomSheet = PositionDetailsBottomSheet(position)
        bottomSheet.show(parentFragmentManager, "PositionDetailsBottomSheet")
    }

    private fun subscribeWebSocket(tokens: List<String>) {
        if (tokens.isEmpty() || tokens == currentTokens) return
        currentTokens = tokens

        val message = JSONObject().apply {
            put("type", "subscribe")
            put("tokens", JSONArray(tokens))
        }
        AppWebSocketManager.sendMessage(message.toString())
        Log.d("PositionsFragment", "Subscribed to ${tokens.size} tokens")
    }

    override fun onStart() {
        super.onStart()
        val token = authRepository.getTokenSync()
        token?.let { AppWebSocketManager.connect(it) }

        // Set up WebSocket listener for real-time position updates
        AppWebSocketManager.setMarketListener { json ->
            if (json.optString("type") != "market_data") return@setMarketListener
            val data = json.optJSONObject("data") ?: return@setMarketListener

            // Extract token/identifier based on available fields
            val token: String?
            val symbol: String?

            if (data.has("Token")) {
                // For index/MCX - use numeric token
                token = data.optLong("Token").toString()
                symbol = null
            } else if (data.has("instrument_token")) {
                // For crypto/comex - use instrument_token as symbol
                token = null
                symbol = data.optString("instrument_token")
            } else {
                return@setMarketListener
            }

            // Extract price data
            val ltp: Double = when {
                data.has("LTP") -> data.optDouble("LTP")
                data.has("ltp") -> data.optDouble("ltp")
                data.has("last_price") -> data.optDouble("last_price")
                else -> 0.0
            }

            if (ltp == 0.0) return@setMarketListener

            // Extract change for PNL calculation
            val change: Double
            val changePercent: Double
            val closePrice: Double

            if (data.has("C")) {
                // Index/MCX format
                closePrice = data.optDouble("C")
                change = ltp - closePrice
                changePercent = if (closePrice != 0.0) (change / closePrice) * 100 else 0.0
            } else {
                // Crypto/comex format
                change = data.optDouble("change")
                changePercent = data.optDouble("change_percent")
                closePrice = data.optDouble("close")
            }

            requireActivity().runOnUiThread {
                // Update adapter with live price
                if (token != null) {
                    adapter.updateLivePrice(token, ltp, change, changePercent)
                } else if (symbol != null) {
                    adapter.updateLivePriceBySymbol(symbol, ltp, change, changePercent)
                }

                // Recalculate and update total open PNL only if in Open tab
                if (currentTab == "OPEN") {
                    updateTotalPnL()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        AppWebSocketManager.setMarketListener(null)
    }

    private fun updateTotalPnL() {
        // Recalculate total PNL from all positions
        var totalPnl = 0.0

        adapter.getCurrentItems().forEach { position ->
            totalPnl += calculatePnlForPosition(position)
        }

        binding.tvOpenPnl.text = String.format("%.2f", totalPnl)
        binding.tvOpenPnl.setTextColor(
            if (totalPnl >= 0) Color.parseColor("#2E7D32")
            else Color.parseColor("#C62828")
        )
    }

    private fun calculatePnlForPosition(position: PositionsItem): Double {
        // Parse values
        val netQty = position.netQuantity.toDoubleOrNull() ?: 0.0
        val avgPrice = position.averagePrice.toDoubleOrNull() ?: 0.0
        val realizedPnl = position.realizedPnl.toDoubleOrNull() ?: 0.0

        // 1️ Closed position → backend truth wins
        if (netQty == 0.0) {
            return realizedPnl
        }

        // 2️Open position → calculate unrealized
        val currentLtp = adapter.getLivePriceForPosition(position.instrumentToken) ?: avgPrice

        if (currentLtp == 0.0) {
            return realizedPnl
        }

        val openQty = Math.abs(netQty)
        val entryPrice = avgPrice
        var unrealized = 0.0

        // Calculate P&L based on side
        if (position.side == "BUY") {
            unrealized = (currentLtp - entryPrice) * openQty
        } else if (position.side == "SELL") {
            unrealized = (entryPrice - currentLtp) * openQty
        } else {
            // Default calculation if side is unknown
            unrealized = (currentLtp - entryPrice) * netQty
        }

        return unrealized
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
