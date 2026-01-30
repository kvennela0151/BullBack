package com.example.bullback.ui.watchlist.searchinstrument

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullback.data.model.auth.watchlist.addwatchlist.AddWatchlistRequest
import com.example.bullback.data.model.auth.watchlist.instrumentsearch.SearchInstrument
import com.example.bullback.data.repository.InstrumentSearchRepository
import com.example.bullback.data.repository.WatchlistRepository
import com.example.bullback.utlis.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InstrumentSearchViewModel(
    private val repository: InstrumentSearchRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    /* ---------------- SEARCH RESULTS ---------------- */
    private val _results = MutableStateFlow<List<SearchInstrument>>(emptyList())
    val results: StateFlow<List<SearchInstrument>> = _results

    /* ---------------- ADD STATE ---------------- */
    private val _addState = MutableStateFlow<Resource<Unit>?>(null)
    val addState: StateFlow<Resource<Unit>?> = _addState


    /* ---------------- SEARCH ---------------- */
    fun search(query: String, segment: String) {
        if (query.length < 2) {
            _results.value = emptyList()
            return
        }

        viewModelScope.launch {
            when (val result = repository.search(query, segment)) {
                is Resource.Success -> {
                    _results.value = result.data ?: emptyList()
                }
                is Resource.Error -> {
                    _results.value = emptyList()
                }
                else -> Unit
            }
        }
    }

    /* ---------------- ADD INSTRUMENT ---------------- */
    fun addInstrument(request: AddWatchlistRequest) {
        viewModelScope.launch {
            _addState.value = Resource.Loading()

            try {
                println("DEBUG: Adding instrument with request: $request")

                val result = watchlistRepository.addInstrument(request)
                _addState.value = result

                // If successful, update the search results to mark as added
                if (result is Resource.Success) {
                    println("DEBUG: Add successful, updating UI")
                    _results.value = _results.value.map { item ->
                        // Match by trading symbol
                        if (item.tradingsymbol == request.symbol) {
                            item.copy(isAdded = true)
                        } else {
                            item
                        }
                    }
                } else if (result is Resource.Error) {
                    println("DEBUG: Add failed: ${result.message}")
                }
            } catch (e: Exception) {
                println("DEBUG: Exception during add: ${e.message}")
                e.printStackTrace()
                _addState.value = Resource.Error(e.message ?: "Failed to add instrument")
            }
        }
    }

    fun resetAddState() {
        _addState.value = null
    }
}
