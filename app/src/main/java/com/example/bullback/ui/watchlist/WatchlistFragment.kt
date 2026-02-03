package com.example.bullback.ui.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bullback.R
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.WatchlistApiService
import com.example.bullback.data.repository.WatchlistRepository
import com.example.bullback.databinding.FragmentWatchlistBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import com.example.bullback.data.model.websocket.AppWebSocketManager
import com.example.bullback.data.repository.AuthRepository
import org.json.JSONArray
import org.json.JSONObject
import android.widget.Toast

class WatchlistFragment : Fragment() {

    private var _binding: FragmentWatchlistBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WatchlistAdapter
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: WatchlistViewModel

    private var currentSegment = "INDEX-FUT"
    private var currentTokens: List<String> = emptyList()
    private var watchlistId: Int? = null

    private var isDeleteMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository.getInstance(requireContext())
        setupViewModel()
        setupRecyclerView()
        setupTabs()
        setupClicks()
        observeWatchlist()
        observeDeleteResult()

        // Load default segment
        viewModel.loadWatchlist(currentSegment)
    }

    private fun setupViewModel() {
        val api = RetrofitClient.createService(WatchlistApiService::class.java)
        val repository = WatchlistRepository(api)
        val factory = WatchlistViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[WatchlistViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = WatchlistAdapter { symbol, segment, token, ltp ->
            val bottomSheet = WatchlistSymbolBottomSheet(symbol, segment, token, ltp)
            bottomSheet.show(parentFragmentManager, "WatchlistSymbolBottomSheet")
        }

        binding.recyclerWatchlist.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerWatchlist.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentSegment = tab.text.toString()
                viewModel.loadWatchlist(currentSegment)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupClicks() {
        binding.etSearch.setOnClickListener {
            val fragment = AddInstruments().apply {
                arguments = Bundle().apply { putString("segment", currentSegment) }
            }
            navigateToFragment(fragment)
        }

        binding.btnDelete.setOnClickListener {
            isDeleteMode = !isDeleteMode
            adapter.isDeleteMode = isDeleteMode

            binding.cbSelectAll.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
            binding.tvDeleteCount.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
            binding.btnDelete.setImageResource(
                if (isDeleteMode) R.drawable.ic_close else R.drawable.ic_delete
            )

            if (!isDeleteMode) {
                adapter.clearSelection()
                binding.cbSelectAll.isChecked = false
            }
        }

        binding.cbSelectAll.setOnCheckedChangeListener { _, checked ->
            if (checked) adapter.selectAll() else adapter.clearSelection()
        }

        binding.tvDeleteCount.setOnClickListener {
            val tokens = adapter.getSelectedTokens()
            val id = watchlistId

            if (tokens.isEmpty() || id == null) {
                Toast.makeText(requireContext(), "Select items to delete", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.deleteSelectedSymbols(id, tokens)
        }
    }

    private fun observeWatchlist() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.symbols.collect { pair ->
                pair ?: return@collect

                watchlistId = pair.first
                val symbols = pair.second

                adapter.submitList(symbols)
                subscribeWebSocket(symbols.map { it.token })
            }
        }
    }

    private fun observeDeleteResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteResult.collect { response ->
                response ?: return@collect

                // Get updated symbols for current watchlist
                val updatedSymbols = response.data
                    .firstOrNull { it.id == watchlistId }?.symbols ?: emptyList()

                adapter.submitList(updatedSymbols)
                subscribeWebSocket(updatedSymbols.map { it.token })
                exitDeleteMode()
            }
        }
    }



    private fun exitDeleteMode() {
        isDeleteMode = false
        adapter.isDeleteMode = false
        adapter.clearSelection()

        binding.cbSelectAll.visibility = View.GONE
        binding.tvDeleteCount.visibility = View.GONE
        binding.cbSelectAll.isChecked = false
        binding.btnDelete.setImageResource(R.drawable.ic_delete)
    }

    private fun subscribeWebSocket(tokens: List<String>) {
        if (tokens.isEmpty() || tokens == currentTokens) return
        currentTokens = tokens

        val message = JSONObject().apply {
            put("type", "subscribe")
            put("tokens", JSONArray(tokens))
        }
        AppWebSocketManager.sendMessage(message.toString())
    }

    // Update the onStart() method
    override fun onStart() {
        super.onStart()
        val token = authRepository.getTokenSync()
        token?.let { AppWebSocketManager.connect(it) }

        // Update the WatchlistFragment's WebSocket listener
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
            val ltp: Double = data.optDouble("LTP")
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
                // Try matching by token first, then by symbol
                if (token != null) {
                    adapter.updateLivePrice(token, ltp, change, changePercent, closePrice)
                } else if (symbol != null) {
                    adapter.updateLivePriceBySymbol(symbol, ltp, change, changePercent, closePrice)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        AppWebSocketManager.setMarketListener(null)
    }

    private fun navigateToFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


