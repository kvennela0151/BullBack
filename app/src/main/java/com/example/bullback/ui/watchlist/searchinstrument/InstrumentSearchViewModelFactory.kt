package com.example.bullback.ui.watchlist.searchinstrument

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bullback.data.repository.InstrumentSearchRepository
import com.example.bullback.data.repository.WatchlistRepository

class InstrumentSearchViewModelFactory(
    private val searchRepository: InstrumentSearchRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InstrumentSearchViewModel(searchRepository, watchlistRepository) as T
    }
}