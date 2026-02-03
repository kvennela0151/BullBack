package com.example.bullback.ui.profile.wallet

import androidx.lifecycle.*
import com.example.bullback.data.model.wallet.WalletTransaction
import com.example.bullback.data.model.wallet.depositwithdrawal.BankAccountData
import com.example.bullback.data.repository.WalletRepository
import kotlinx.coroutines.launch

class WalletViewModel(
    private val repository: WalletRepository
) : ViewModel() {

    private val _transactions = MutableLiveData<List<WalletTransaction>>()
    val transactions: LiveData<List<WalletTransaction>> = _transactions

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading


    private val _bankDetails = MutableLiveData<BankAccountData>()
    val bankDetails: LiveData<BankAccountData> = _bankDetails

    fun loadTransactions(type: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _transactions.value = repository.getTransactions(type)
            } catch (e: Exception) {
                _transactions.value = emptyList()
            }
            _loading.value = false
        }
    }

    fun loadBankDetails() {
        viewModelScope.launch {
            try {
                val resp = repository.getActiveBankAccount()
                if (resp.isSuccessful) {
                    resp.body()?.data?.let {
                        _bankDetails.postValue(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}