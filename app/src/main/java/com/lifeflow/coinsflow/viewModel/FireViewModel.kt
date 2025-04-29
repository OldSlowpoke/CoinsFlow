package com.lifeflow.coinsflow.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.coinsflow.model.Category
import com.lifeflow.coinsflow.model.Product
import com.lifeflow.coinsflow.model.Transaction
import com.lifeflow.coinsflow.model.repository.FireRepository
import com.lifeflow.coinsflow.model.uistate.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FireViewModel @Inject constructor(
    private val fireRepository: FireRepository
) : ViewModel() {
    val transactions =
        fireRepository.getTransactions().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val categories =
        fireRepository.getCategories().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val products =
        fireRepository.getProducts().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        checkCurrentUser()
    }

    // login/out
    private fun checkCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = fireRepository.getCurrentUser()
            if (user != null) {
                _uiState.update { it.copy(isAuthenticated = true) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = fireRepository.login(uiState.value.email, uiState.value.password)
            if (result.isSuccess) {
                _uiState.update { it.copy(isAuthenticated = true) }
            } else {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun register() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = fireRepository.register(uiState.value.email, uiState.value.password)
            if (result.isSuccess) {
                _uiState.update { it.copy(isAuthenticated = true) }
            } else {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    //Transactions
    fun addTransactions(transaction: Transaction, path: String) = viewModelScope.launch {
        fireRepository.addTransaction(transaction, path)
    }

    fun deleteTransactions(transaction: Transaction) = viewModelScope.launch {
        fireRepository.deleteTransactions(transaction)
    }

    fun getLinkOnFirePath(path: String) = fireRepository.getLinkOnFirePath(path)

    //Categories
    fun addCategory(category: Category, path: String) = viewModelScope.launch {
        fireRepository.addCategory(category, path)
    }

    fun deleteCategories(category: Category) = viewModelScope.launch {
        fireRepository.deleteCategory(category)
    }

    //Products
    fun addProduct(product: Product, path: String) = viewModelScope.launch {
        fireRepository.addProduct(product, path)
    }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        fireRepository.deleteProduct(product)
    }

    fun logout() {
        fireRepository.logout()
        _uiState.update {
            AuthUiState(isAuthenticated = false)
        }
        viewModelScope.coroutineContext.cancelChildren() // Отмена всех корутин
    }
}

