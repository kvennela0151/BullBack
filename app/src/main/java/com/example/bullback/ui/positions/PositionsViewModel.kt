package com.example.bullback.ui.positions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullback.data.model.positions.PositionsItem
import com.example.bullback.data.model.positions.exitall.ExitAllResponse
import com.example.bullback.data.model.positions.squareoff.SquareOffResponse
import com.example.bullback.data.repository.PositionsRepository
import com.example.bullback.utlis.FinanceCalculator
import com.example.bullback.utlis.Resource
import kotlinx.coroutines.launch

// PositionsViewModel.kt
open class PositionsViewModel(
    private val repository: PositionsRepository = PositionsRepository()
) : ViewModel() {

    private val _positions = MutableLiveData<List<PositionsItem>>()
    val positions: LiveData<List<PositionsItem>> = _positions

    private val _openPnl = MutableLiveData<Double>()
    val openPnl: LiveData<Double> = _openPnl

    private var currentStatus = "OPEN"

    private val _exitAllResponse = MutableLiveData<ExitAllResponse?>()
    val exitAllResponse: LiveData<ExitAllResponse?> = _exitAllResponse

    private val _squareOffResult = MutableLiveData<Resource<SquareOffResponse>>()
    val squareOffResult: LiveData<Resource<SquareOffResponse>> = _squareOffResult

    val livePrices = mutableMapOf<String, Double>()

    val totalPnl = MutableLiveData<Double>()
    val utilisedFunds = MutableLiveData<Double>()


    fun setStatus(status: String) {
        currentStatus = status
        fetchPositions(currentStatus)
    }

    fun fetchPositions(currentStatus: String) {
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

    fun squareOffPosition(symbol: String) {
        viewModelScope.launch {
            _squareOffResult.value = Resource.Loading()
            val result = repository.squareOffPositions(listOf(symbol))
            _squareOffResult.value = result

            // Reload positions after successful square off
            if (result is Resource.Success && result.data.status) {
                fetchPositions(currentStatus)
            }
        }
    }

    fun squareOffAllPositions() {
        viewModelScope.launch {
            _squareOffResult.value = Resource.Loading()
            val result = repository.squareOffAllPositions()
            _squareOffResult.value = result

            // Reload positions after successful square off
            if (result is Resource.Success && result.data.status) {
                fetchPositions(currentStatus)
            }
        }
    }

    fun recalcAll() {
        val list = positions.value ?: return

        // TOTAL PNL
        val pnl = FinanceCalculator.calculateTotalPnl(list, livePrices)
        totalPnl.postValue(pnl)

        // UTILISED FUNDS
        val utilised = FinanceCalculator.calculateUtilisedFunds(list)
        utilisedFunds.postValue(utilised)
    }

    fun recalcTotalPnl() {
        val list = positions.value ?: return
        val pnl = FinanceCalculator.calculateTotalPnl(list, livePrices)
        totalPnl.postValue(pnl)
    }

    fun refreshPositions() {
        fetchPositions(currentStatus)
    }
}

