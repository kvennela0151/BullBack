package com.example.bullback.ui.positions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullback.data.model.positions.PositionsItem
import com.example.bullback.data.model.positions.exitall.ExitAllResponse
import com.example.bullback.data.repository.PositionsRepository
import kotlinx.coroutines.launch

// PositionsViewModel.kt
class PositionsViewModel(
    private val repository: PositionsRepository = PositionsRepository()
) : ViewModel() {

    private val _positions = MutableLiveData<List<PositionsItem>>()
    val positions: LiveData<List<PositionsItem>> = _positions

    private val _openPnl = MutableLiveData<Double>()
    val openPnl: LiveData<Double> = _openPnl

    private var currentStatus = "OPEN"

    private val _exitAllResponse = MutableLiveData<ExitAllResponse?>()
    val exitAllResponse: LiveData<ExitAllResponse?> = _exitAllResponse


    fun setStatus(status: String) {
        currentStatus = status
        fetchPositions()
    }

    fun fetchPositions() {
        viewModelScope.launch {
            try {
                val list = repository.getPositions(currentStatus)
                _positions.value = list
                _openPnl.value = list.sumOf { it.realizedPnl.toDoubleOrNull() ?: 0.0 }
            } catch (e: Exception) {
                _positions.value = emptyList()
            }
        }
    }

    fun exitAllPositions(symbols: List<String>) {
        viewModelScope.launch {
            try {
                val response = repository.exitAllPositions(symbols)
                if (response.isSuccessful) {
                    _exitAllResponse.postValue(response.body())
                } else {
                    _exitAllResponse.postValue(null)
                }
            } catch (e: Exception) {
                _exitAllResponse.postValue(null)
            }
        }
    }



}

