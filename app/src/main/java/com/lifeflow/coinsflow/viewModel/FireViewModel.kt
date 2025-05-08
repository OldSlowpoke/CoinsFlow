package com.lifeflow.coinsflow.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.coinsflow.model.Account
import com.lifeflow.coinsflow.model.ExpenseCategories
import com.lifeflow.coinsflow.model.IncomesCategories
import com.lifeflow.coinsflow.model.Market
import com.lifeflow.coinsflow.model.Product
import com.lifeflow.coinsflow.model.Transaction
import com.lifeflow.coinsflow.model.UnitType
import com.lifeflow.coinsflow.model.CheckEntity
import com.lifeflow.coinsflow.model.repository.FireRepository
import com.lifeflow.coinsflow.model.uiState.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
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

    private val _checkItems = MutableStateFlow<MutableList<CheckEntity>>(mutableListOf())
    val checkItems: StateFlow<MutableList<CheckEntity>> = _checkItems

    val totalSum: StateFlow<Double> = _checkItems.map { items ->
        if (items.isEmpty()) {
            BigDecimal.ZERO.toDouble() // Возвращаем 0, если список пустой
        } else {
            val totalBigDecimal = items
                .map { item ->
                    val qty = item.count.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val unitPrice = item.amount.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val subtotal = unitPrice.multiply(qty)
                    subtotal
                }
                .reduce { acc, current -> acc.add(current) } // Суммируем все BigDecimal
                .setScale(2, RoundingMode.HALF_UP) // Округляем до 2 знаков

            totalBigDecimal.toDouble() // Конвертируем в Double только для отображения
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalBalance: Flow<Double> = accounts.map { accounts ->
        accounts.fold(BigDecimal.ZERO) { acc, account ->
            acc + account.initialAmount.toBigDecimal()
        }.setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addChecks(checks: MutableList<CheckEntity>) = viewModelScope.launch {
        fireRepository.addChecks(checks)

    }

    // Добавление нового пустого элемента
    fun addItem(vm: FireViewModel) {
        _checkItems.value = _checkItems.value.toMutableList().apply {
            add(
                CheckEntity(
                    id = vm.getLinkOnFirePath("checks")
                )
            )
        }
    }

    // Обновление productId и productName
    fun updateProduct(id: String, product: Product) {
        _checkItems.value = _checkItems.value.map {
            if (it.id == id) {
                it.copy(productName = product.name)
            } else {
                it
            }
        }.toMutableList()
    }

    // Обновление количества
    fun updateQuantity(id: String, newQuantity: BigDecimal) {
        _checkItems.value = _checkItems.value.map {
            if (it.id == id) it.copy(count = newQuantity) else it
        }.toMutableList()
    }
    /*//Обновлление Даты
    fun updateDate(id: String, newDate: String) {
        _checkItems.value = _checkItems.value.map {
            if (it.id == id) it.copy(date = newDate) else it
        }.toMutableList()
    }*/

    // Обновление цены за единицу
    fun updatePrice(id: String, newPrice: BigDecimal) {
        _checkItems.value = _checkItems.value.map {
            if (it.id == id) it.copy(amount = newPrice) else it
        }.toMutableList()
    }

    // Переключение скидки
    fun toggleDiscount(id: String) {
        _checkItems.value = _checkItems.value.map {
            if (it.id == id) it.copy(discount = !it.discount) else it
        }.toMutableList()
    }

    fun updateUnit(id: String, newUnit: UnitType) {
        _checkItems.value = _checkItems.value.map {
            if (it.id == id) it.copy(unit = newUnit) else it
        }.toMutableList()
    }

    fun removeItem(id: String) {
        _checkItems.value = _checkItems.value.filterNot { it.id == id }.toMutableList()
    }

    /*fun calculateTotalSum(): Double {
        return _checkItems.value.sumOf { item ->
            val itemTotal = item.count * item.amount
            itemTotal
        }
    }*/

    // Очистка чека
    fun clearCheck() {
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

    fun deleteTransactions(transaction: Transaction) = viewModelScope.launch {
        fireRepository.deleteTransactions(transaction)
    }

    fun saveChecksAndTransaction(
        checkEntities: MutableList<CheckEntity>,
        transaction: Transaction,
        path: String
    ) = viewModelScope.launch {
        fireRepository.saveChecksAndTransaction(checkEntities, transaction, path)
    }

    fun getLinkOnFirePath(path: String) = fireRepository.getLinkOnFirePath(path)

    //ExpenseCategories
    fun addExpenseCategory(expenseCategories: ExpenseCategories, path: String) =
        viewModelScope.launch {
            fireRepository.addExpenseCategory(expenseCategories, path)
        }

    fun deleteExpenseCategories(expenseCategories: ExpenseCategories) = viewModelScope.launch {
        fireRepository.deleteExpenseCategory(expenseCategories)
    }

    //SubExpenseCategories
    fun addSubExpenseCategory(expenseCategories: ExpenseCategories, subCategory: String) =
        viewModelScope.launch {
            fireRepository.addSubExpenseCategory(expenseCategories, subCategory)
        }

    fun deleteSubExpenseCategory(expenseCategories: ExpenseCategories, subCategory: String) =
        viewModelScope.launch {
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
    fun addSubIncomesCategory(category: IncomesCategories, subCategory: String) =
        viewModelScope.launch {
            fireRepository.addSubIncomesCategory(category, subCategory)
        }

    fun deleteSubIncomesCategory(category: IncomesCategories, subCategory: String) =
        viewModelScope.launch {
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

    fun getLinkOnChecks(checkEntities: MutableList<CheckEntity>, transaction: Transaction) {
        // Преобразуем список CheckEntity в список ID
        val links: MutableList<String> = checkEntities.map { it.id }.toMutableList()

        // Присваиваем транзакции новый список ссылок
        transaction.checkLinks = links
    }
}

