package com.example.bullback.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullback.data.model.auth.watchlist.addwatchlist.RemoveWatchlistResponse
import com.example.bullback.data.model.auth.watchlist.deletewatchlist.RemoveWatchlistRequest
import com.example.bullback.data.model.watchlist.WatchlistSymbol
import com.example.bullback.data.repository.WatchlistRepository
import com.example.bullback.utlis.Resource
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
class WatchlistViewModel(
    private val repository: WatchlistRepository
) : ViewModel() {

    private val _symbols =
        MutableStateFlow<Pair<Int, List<WatchlistSymbol>>?>(null)

    val symbols: StateFlow<Pair<Int, List<WatchlistSymbol>>?> = _symbols


    fun loadWatchlist(selectedSegment: String) {
        viewModelScope.launch {
            when (val result = repository.getWatchlist()) {
                is Resource.Success -> {
                    val watchlist = result.data
                        .firstOrNull { it.name == selectedSegment }

                    watchlist?.let {
                        _symbols.value = it.id to it.symbols
                    }
                }
                else -> _symbols.value = null
            }
        }
    }

    private val _deleteResult = MutableStateFlow<RemoveWatchlistResponse?>(null)
    val deleteResult: StateFlow<RemoveWatchlistResponse?> get() = _deleteResult

    fun deleteSelectedSymbols(id: Int, symbols: List<String>) {
        viewModelScope.launch {
            try {
                val request = RemoveWatchlistRequest(id, symbols)
                val result: Resource<RemoveWatchlistResponse> = repository.removeSymbols(id, symbols)

                when (result) {
                    is Resource.Success -> _deleteResult.value = result.data
                    is Resource.Error -> {
                        // handle error if needed
                        _deleteResult.value = null
                    }
                    is Resource.Loading -> {
                        // optional: show loading state
                    }
                }
            } catch (e: Exception) {
                _deleteResult.value = null
            }
        }
    }
}

