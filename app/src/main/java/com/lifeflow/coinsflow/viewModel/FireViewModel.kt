package com.lifeflow.coinsflow.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.coinsflow.model.Account
import com.lifeflow.coinsflow.model.ExpenseCategories
import com.lifeflow.coinsflow.model.Check
import com.lifeflow.coinsflow.model.IncomesCategories
import com.lifeflow.coinsflow.model.Market
import com.lifeflow.coinsflow.model.Product
import com.lifeflow.coinsflow.model.Transaction
import com.lifeflow.coinsflow.model.repository.FireRepository
import com.lifeflow.coinsflow.model.uiState.AuthUiState
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
    val expenseCategories =
        fireRepository.getExpenseCategories()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val incomesCategories = fireRepository.getIncomesCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val products =
        fireRepository.getProducts().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val accounts =
        fireRepository.getAccounts().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val markets =
        fireRepository.getMarkets().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    // Инициализируем список
    private val _checkItems = MutableStateFlow<MutableList<Check>>(mutableListOf())
    val checkItems: StateFlow<MutableList<Check>> = _checkItems

    // Метод для добавления элемента
    fun addItem(item: Check) {
        _checkItems.value = _checkItems.value.toMutableList().apply {
            add(item)
        }
    }

    // Метод для обновления элемента по индексу
    fun updateItem(index: Int, newName: String) {
        val updatedList = _checkItems.value.toMutableList().apply {
            this[index] = this[index].copy(productName = newName)
        }
        _checkItems.value = updatedList
    }

    // Метод для очистки списка
    fun clearCheckItems() {
        _checkItems.value = mutableListOf()
    }

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

    //ExpenseCategories
    fun addExpenseCategory(expenseCategories: ExpenseCategories, path: String) = viewModelScope.launch {
        fireRepository.addExpenseCategory(expenseCategories, path)
    }

    fun deleteExpenseCategories(expenseCategories: ExpenseCategories) = viewModelScope.launch {
        fireRepository.deleteExpenseCategory(expenseCategories)
    }

    //SubExpenseCategories
    fun addSubExpenseCategory(expenseCategories: ExpenseCategories, subCategory: String) = viewModelScope.launch {
        fireRepository.addSubExpenseCategory(expenseCategories, subCategory)
    }

    fun deleteSubExpenseCategory(expenseCategories: ExpenseCategories, subCategory: String) = viewModelScope.launch {
        fireRepository.deleteSubExpenseCategory(expenseCategories, subCategory)
    }

    //IncomesCategories
    fun addIncomesCategory(category: IncomesCategories, path: String) = viewModelScope.launch {
        fireRepository.addIncomesCategory(category, path)
    }

    fun deleteIncomesCategories(category: IncomesCategories) = viewModelScope.launch {
        fireRepository.deleteIncomesCategory(category)
    }

    //SubCategories
    fun addSubIncomesCategory(category: IncomesCategories, subCategory: String) = viewModelScope.launch {
        fireRepository.addSubIncomesCategory(category, subCategory)
    }

    fun deleteSubIncomesCategory(category: IncomesCategories, subCategory: String) = viewModelScope.launch {
        fireRepository.deleteSubIncomesCategory(category, subCategory)
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

    //Accounts
    fun addAccount(account: Account, path: String) = viewModelScope.launch {
        fireRepository.addAccount(account, path)
    }

    fun deleteAccount(account: Account) = viewModelScope.launch {
        fireRepository.deleteAccount(account)

    }

    //Markets
    fun addMarket(market: Market, path: String) = viewModelScope.launch {
        fireRepository.addMarket(market, path)
    }

    fun deleteMarket(market: Market) = viewModelScope.launch {
        fireRepository.deleteMarket(market)
    }
}

