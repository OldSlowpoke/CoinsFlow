package com.lifeflow.coinsflow.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.coinsflow.model.Expenses
import com.lifeflow.coinsflow.model.repository.FireRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FireViewModel @Inject constructor(private val fireRepository: FireRepository) : ViewModel() {
    val expenses =
        fireRepository.getExpenses().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addExpenses(expenses: Expenses) = viewModelScope.launch {
        fireRepository.addExpenses(expenses)
    }
}

