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
    fun addInstrument(item: SearchInstrument) {
        viewModelScope.launch {
            _addState.value = Resource.Loading()

            val request = AddWatchlistRequest(
                exchange = item.exchange,
                expiryDate = item.expiryDate,
                instrumentType = item.instrumentType,
                lotSize = item.lotSize,
                script = item.script,
                segment = item.segment,
                strike = item.strike,
                symbol = item.symbol,
                token = item.instrumentToken?.toString() ?: ""
            )

            _addState.value = watchlistRepository.addInstrument(request)

            if (_addState.value is Resource.Success) {
                // Update the list immutably so adapter sees the change
                _results.value = _results.value.map {
                    if (it.symbol == item.symbol) it.copy(isAdded = true)
                    else it
                }
            }
        }
    }

    fun resetAddState() {
        _addState.value = null
    }
}
