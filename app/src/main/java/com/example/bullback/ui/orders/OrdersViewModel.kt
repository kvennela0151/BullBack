package com.example.bullback.ui.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullback.data.model.auth.Order
import com.example.bullback.data.model.auth.OrderStatus
import com.example.bullback.data.repository.OrdersRepository
import com.example.bullback.utlis.Resource
import kotlinx.coroutines.launch
class OrdersViewModel(
    private val repository: OrdersRepository = OrdersRepository()
) : ViewModel() {

    private val _orders = MutableLiveData<Resource<List<Order>>>()
    val orders: LiveData<Resource<List<Order>>> = _orders

    private val _filteredOrders = MutableLiveData<List<Order>>()
    val filteredOrders: LiveData<List<Order>> = _filteredOrders

    private val _selectedTab = MutableLiveData<OrderStatus>(OrderStatus.OPEN)
    val selectedTab: LiveData<OrderStatus> = _selectedTab

    private var allOrders: List<Order> = emptyList()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _orders.value = Resource.Loading()

            when (val result = repository.getOrders()) {
                is Resource.Success -> {
                    _orders.value = result
                    allOrders = result.data
                    filterOrders(_selectedTab.value ?: OrderStatus.OPEN)
                }
                is Resource.Error -> {
                    _orders.value = result
                    _filteredOrders.value = emptyList()
                }
                is Resource.Loading -> {
                    _orders.value = result
                }
            }
        }
    }

    fun onTabSelected(status: OrderStatus) {
        _selectedTab.value = status
        filterOrders(status)
    }

    private fun filterOrders(status: OrderStatus) {
        val filtered = repository.filterOrdersByStatus(allOrders, status)
        _filteredOrders.value = filtered
    }


    fun refreshOrders() {
        loadOrders()
    }
}