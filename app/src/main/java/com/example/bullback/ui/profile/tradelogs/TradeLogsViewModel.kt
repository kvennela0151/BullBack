package com.example.bullback.ui.profile.tradelogs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullback.data.model.tradelogs.TradeItem
import com.example.bullback.data.repository.TradeRepository
import kotlinx.coroutines.launch

class TradeLogsViewModel(private val repo: TradeRepository) : ViewModel() {

    private val _tradeLogs = MutableLiveData<List<TradeItem>>()
    val tradeLogs: LiveData<List<TradeItem>> get() = _tradeLogs

    fun loadClosedTrades(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            try {
                val data = repo.getClosedTrades(page, limit)
                _tradeLogs.value = data
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
