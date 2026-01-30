package com.example.bullback.ui.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullback.data.model.auth.Order
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

    init {
        loadOrdersByType("OPEN")
    }

    fun loadOrdersByType(type: String) {
        viewModelScope.launch {
            _orders.value = Resource.Loading()

            when (val result = repository.getOrders(type)) {
                is Resource.Success -> {
                    _orders.value = result as Resource<List<Order>>
                    _filteredOrders.value = result.data // show in adapter
                }
                is Resource.Error -> {
                    _orders.value = result
                    _filteredOrders.value = emptyList()
                }
                is Resource.Loading -> _orders.value = result
            }
        }
    }
}
