package com.example.bullback.ui.profile.wallet

import androidx.lifecycle.*
import com.example.bullback.data.model.auth.ApiResponse
import com.example.bullback.data.model.wallet.WalletTransaction
import com.example.bullback.data.model.wallet.depositwithdrawal.BankAccountData
import com.example.bullback.data.model.wallet.walletbalance.WalletBalanceData
import com.example.bullback.data.repository.WalletRepository
import kotlinx.coroutines.launch
import com.example.bullback.data.model.positions.PositionsItem
import com.example.bullback.data.model.wallet.deposit.DepositResponse
import com.example.bullback.data.model.wallet.withdrawal.WithdrawResponse
import com.example.bullback.data.repository.PositionsRepository
import com.example.bullback.utlis.FinanceCalculator
import java.io.File
import java.io.FileOutputStream

class WalletViewModel(
    private val walletRepo: WalletRepository,
    private val positionsRepo: PositionsRepository = PositionsRepository()
) : ViewModel() {

    // -------------------------------------------------------------------
    // WALLET LIVE DATA
    // -------------------------------------------------------------------
    private val _transactions = MutableLiveData<List<WalletTransaction>>()
    val transactions: LiveData<List<WalletTransaction>> = _transactions

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _bankDetails = MutableLiveData<BankAccountData>()
    val bankDetails: LiveData<BankAccountData> = _bankDetails

    private val _walletBalance = MutableLiveData<WalletBalanceData?>()
    val walletBalance: LiveData<WalletBalanceData?> = _walletBalance

    private val _withdrawResult = MutableLiveData<ApiResponse<WithdrawResponse>?>()
    val withdrawResult: LiveData<ApiResponse<WithdrawResponse>?> = _withdrawResult

    fun withdrawFunds(amount: String, name: String, acc: String, ifsc: String, upi: String?) {
        viewModelScope.launch {
            try {
                val res = walletRepo.withdrawFunds(amount, name, acc, ifsc, upi)
                if (res.isSuccessful) {
                    _withdrawResult.postValue(res.body())
                } else {
                    _withdrawResult.postValue(null)
                }
            } catch (e: Exception) {
                _withdrawResult.postValue(null)
            }
        }
    }


    // -------------------------------------------------------------------
    // POSITIONS (needed for utilised funds / PNL)
    // -------------------------------------------------------------------
    private val _positions = MutableLiveData<List<PositionsItem>>()
    val positions: LiveData<List<PositionsItem>> = _positions

    val livePrices = mutableMapOf<String, Double>()

    val utilisedFunds = MutableLiveData<Double>()
    val totalPnl = MutableLiveData<Double>()

    private val _depositResult = MutableLiveData<ApiResponse<DepositResponse>?>()
    val depositResult: LiveData<ApiResponse<DepositResponse>?> get() = _depositResult

    fun depositFunds(amount: String, bankId: Int, screenshot: File?) {
        viewModelScope.launch {
            try {
                val response = walletRepo.depositFunds(amount, bankId, screenshot)
                if (response.isSuccessful) {
                    _depositResult.postValue(response.body())
                } else {
                    _depositResult.postValue(null)
                }
            } catch (e: Exception) {
                _depositResult.postValue(null)
            }
        }
    }


    // -------------------------------------------------------------------
    // LOAD WALLET BALANCE
    // -------------------------------------------------------------------
    fun loadWalletBalance() {
        viewModelScope.launch {
            try {
                val balanceData = walletRepo.getWalletBalance()
                _walletBalance.postValue(balanceData)
            } catch (e: Exception) {
                e.printStackTrace()
                _walletBalance.postValue(null)
            }
        }
    }

    // -------------------------------------------------------------------
    // LOAD TRANSACTIONS
    // -------------------------------------------------------------------
    fun loadTransactions(type: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _transactions.value = walletRepo.getTransactions(type)
            } catch (e: Exception) {
                _transactions.value = emptyList()
            }
            _loading.value = false
        }
    }

    // -------------------------------------------------------------------
    // LOAD ACTIVE BANK DETAILS
    // -------------------------------------------------------------------
    fun loadBankDetails() {
        viewModelScope.launch {
            try {
                val response = walletRepo.getActiveBankAccount()
                if (response.isSuccessful) {
                    response.body()?.data?.let { bankData ->
                        _bankDetails.postValue(bankData)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // -------------------------------------------------------------------
    // LOAD POSITIONS → Required for utilised funds + total pnl
    // -------------------------------------------------------------------
    fun loadPositions() {
        viewModelScope.launch {
            try {
                val list = positionsRepo.getPositions("OPEN")
                _positions.postValue(list)

                recalcAll() // calculate pnl + used funds
            } catch (e: Exception) {
                _positions.postValue(emptyList())
            }
        }
    }

    // -------------------------------------------------------------------
    // Recalculate PNL + Utilised Funds
    // -------------------------------------------------------------------
    fun recalcAll() {
        val list = positions.value ?: return

        // TOTAL PNL
        val pnl = FinanceCalculator.calculateTotalPnl(list, livePrices)
        totalPnl.postValue(pnl)

        // UTILISED FUNDS
        val used = FinanceCalculator.calculateUtilisedFunds(list)
        utilisedFunds.postValue(used)
    }
}
