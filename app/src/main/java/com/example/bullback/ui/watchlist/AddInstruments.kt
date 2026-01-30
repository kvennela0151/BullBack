package com.example.bullback.ui.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.WatchlistApiService
import com.example.bullback.data.repository.InstrumentSearchRepository
import com.example.bullback.data.repository.WatchlistRepository
import com.example.bullback.databinding.FragmentAddInstrumentsBinding
import com.example.bullback.ui.watchlist.searchinstrument.InstrumentSearchAdapter
import com.example.bullback.ui.watchlist.searchinstrument.InstrumentSearchViewModel
import com.example.bullback.ui.watchlist.searchinstrument.InstrumentSearchViewModelFactory
import com.example.bullback.utlis.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddInstruments : Fragment() {

    private lateinit var binding: FragmentAddInstrumentsBinding
    private lateinit var viewModel: InstrumentSearchViewModel
    private lateinit var adapter: InstrumentSearchAdapter

    private var segment: String = "INDEX-FUT"
    private var searchJob: Job? = null // for debounced search

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddInstrumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        segment = arguments?.getString("segment") ?: "INDEX-FUT"

        setupViewModel()
        setupRecyclerView()
        observeResults()
        observeAddState()
        setupSearch()
        setupBack()
    }

    private fun setupViewModel() {
        val api = RetrofitClient.createService(WatchlistApiService::class.java)
        val searchRepo = InstrumentSearchRepository(api)
        val watchlistRepo = WatchlistRepository(api)

        val factory = InstrumentSearchViewModelFactory(searchRepo, watchlistRepo)
        viewModel = ViewModelProvider(this, factory)[InstrumentSearchViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = InstrumentSearchAdapter { instrument ->
            viewModel.addInstrument(instrument) // call ViewModel to add
        }

        binding.rvInstruments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInstruments.adapter = adapter
    }

    private fun observeResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.results.collectLatest { list ->
                adapter.submitList(list)
                binding.layoutEmpty.visibility =
                    if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun observeAddState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                    }
                    is Resource.Success -> {
                        viewModel.resetAddState()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetAddState()
                    }
                    null -> Unit
                }
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(300) // debounce 300ms
                viewModel.search(query, segment)
            }
        }
    }

    private fun setupBack() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
