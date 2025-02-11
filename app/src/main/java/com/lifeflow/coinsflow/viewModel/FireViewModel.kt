package com.lifeflow.coinsflow.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.coinsflow.model.Transactions
import com.lifeflow.coinsflow.model.repository.FireRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FireViewModel @Inject constructor(private val fireRepository: FireRepository) : ViewModel() {
    val transactions =
        fireRepository.getTransactions().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addTransactions(transactions: Transactions, path: String) = viewModelScope.launch {
        fireRepository.addTransactions(transactions,path)
    }

    fun deleteTransactions(transactions: Transactions) = viewModelScope.launch {
        fireRepository.deleteTransactions(transactions)
    }

    fun getLinkOnFirePath(path: String) = fireRepository.getLinkOnFirePath(path)
}

