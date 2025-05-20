package com.lifeflow.coinsflow.viewModel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflow.coinsflow.model.Account
import com.lifeflow.coinsflow.model.Budget
import com.lifeflow.coinsflow.model.CategoryStat
import com.lifeflow.coinsflow.model.ExpenseCategories
import com.lifeflow.coinsflow.model.IncomesCategories
import com.lifeflow.coinsflow.model.Market
import com.lifeflow.coinsflow.model.Product
import com.lifeflow.coinsflow.model.Transaction
import com.lifeflow.coinsflow.model.UnitType
import com.lifeflow.coinsflow.model.CheckEntity
import com.lifeflow.coinsflow.model.MonthlyStat
import com.lifeflow.coinsflow.model.PriceHistory
import com.lifeflow.coinsflow.model.ProductStat
import com.lifeflow.coinsflow.model.repository.FireRepository
import com.lifeflow.coinsflow.model.repository.toCheckEntity
import com.lifeflow.coinsflow.model.uiState.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.absoluteValue

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
    val checks =
        fireRepository.getChecks().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val budgets = fireRepository.getBudgets().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _checkItems = MutableStateFlow<MutableList<CheckEntity>>(mutableListOf())
    val checkItems: StateFlow<MutableList<CheckEntity>> = _checkItems

    val totalSum: StateFlow<Double> = _checkItems.map { items ->
        if (items.isEmpty()) {
            BigDecimal.ZERO.toDouble()
        } else {
            // Суммируем только поле `amount` каждого чека
            val totalBigDecimal = items.map { item ->
                item.amount // Используем напрямую `amount` (уже в формате BigDecimal)
            }.reduce { acc, current -> acc.add(current) }
                .setScale(2, RoundingMode.HALF_UP) // Округляем до 2 знаков
            totalBigDecimal.toDouble()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    //Budgets
    fun addBudget(budget: Budget, path: String) = viewModelScope.launch {
        fireRepository.addBudget(budget, path)
    }

    fun deleteBudget(budget: Budget) = viewModelScope.launch {
        fireRepository.deleteBudget(budget)
    }

    // Получение транзакций с вложенными чеками
    fun getTransactionsWithChecks(): Flow<List<Transaction>> = flow {
        fireRepository.getTransactions().collect { transactions ->
            val updatedTransactions = transactions.map { transaction ->
                val mutableTransaction =
                    transaction.copy() // Создаем копию для избежания мутаций оригинала
                if (mutableTransaction.checkLinks.isNotEmpty()) {
                    val checks =
                        fireRepository.getChecksForTransaction(mutableTransaction.checkLinks)
                    mutableTransaction.check = checks.map { it.toCheckEntity() }.toMutableList()
                }
                mutableTransaction
            }
            emit(updatedTransactions)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBalance: Flow<Double> = accounts.map { accounts ->
        accounts.fold(BigDecimal.ZERO) { acc, account ->
            acc + account.initialAmount.toBigDecimal()
        }.setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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

    private fun calculateUnitPrice(
        amount: BigDecimal,
        quantity: BigDecimal,
        unit: UnitType
    ): BigDecimal {
        if (quantity.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO

        return when (unit) {
            UnitType.PIECE -> amount.divide(quantity, 2, RoundingMode.HALF_UP)
            UnitType.KILOGRAM, UnitType.LITER -> amount.divide(quantity, 2, RoundingMode.HALF_UP)
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
        _checkItems.value = _checkItems.value.map { item ->
            if (item.id == id) {
                val updatedItem = item.copy(count = newQuantity)
                updatedItem.copy(
                    unitPrice = calculateUnitPrice(
                        updatedItem.amount,
                        newQuantity,
                        updatedItem.unit
                    )
                )
            } else {
                item
            }
        }.toMutableList()
    }

    // Обновление цены за единицу
    fun updatePrice(id: String, newPrice: BigDecimal) {
        _checkItems.value = _checkItems.value.map { item ->
            if (item.id == id) {
                val updatedItem = item.copy(amount = newPrice)
                updatedItem.copy(
                    unitPrice = calculateUnitPrice(
                        newPrice,
                        updatedItem.count,
                        updatedItem.unit
                    )
                )
            } else {
                item
            }
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
                // 1. Получите пользователя из результата
                val user = result.getOrNull()
                if (user != null) {
                    try {
                        // 2. Инициализируйте базовые коллекции
                        fireRepository.initializeBaseCollections(user.uid)
                        _uiState.update { it.copy(isAuthenticated = true) }
                    } catch (e: Exception) {
                        // 3. Обработка ошибок при инициализации данных
                        _uiState.update { it.copy(error = "Ошибка инициализации данных: ${e.message}") }
                        Log.e("AuthViewModel", "Ошибка при инициализации данных", e)
                    }
                }
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


    //Statistics
    fun calculateTrend(stats: List<MonthlyStat>, period: Int = 3): List<Double> {
        return stats.windowed(period, 1, partialWindows = true) { window ->
            window.map { it.balance }.average()
        }
    }

    @SuppressLint("DefaultLocale")
    fun calculateMonthlyStats(
        transactions: List<Transaction>,
        timeRange: String = "Month"
    ): Map<String, MonthlyStat> {
        val currentYear = LocalDate.now().year // Получаем текущий год

        val grouped = transactions.groupBy { transaction ->
            try {
                val date: LocalDate = try {
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    LocalDate.parse(transaction.date, formatter)
                } catch (e: Exception) {
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    LocalDate.parse(transaction.date, formatter)
                }

                when (timeRange) {
                    "Months" -> {
                        if (date.year != currentYear) {
                            "invalid" // Исключаем месяцы не текущего года
                        } else {
                            "${date.year}-${String.format("%02d", date.monthValue)}"
                        }
                    }

                    "Years" -> "${date.year}"
                    "All" -> "total"
                    else -> "invalid"
                }
            } catch (e: Exception) {
                Log.e("DateParsing", "Ошибка парсинга даты: ${transaction.date}", e)
                "invalid"
            }
        }

        val validGrouped = grouped.filterKeys { it != "invalid" }

        // Сортировка по возрастанию (в зависимости от timeRange)
        val sortedKeys = when (timeRange) {
            "Month" -> validGrouped.keys.sortedWith(compareBy {
                LocalDate.parse("$it-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            })

            "Years" -> validGrouped.keys.sorted()
            "All" -> validGrouped.keys.toList()
            else -> validGrouped.keys.toList()
        }

        val result = mutableMapOf<String, MonthlyStat>()

        for (key in sortedKeys) {
            val txs = validGrouped[key] ?: continue

            val income = txs.filter { it.type == "income" }.sumOf { it.total }
            val expense = txs.filter { it.type == "expense" }.sumOf { it.total }

            result[key] = MonthlyStat(
                month = key,
                income = income,
                expense = expense,
                balance = income + expense
            )
        }

        return result
    }

    // Получение топ-20 товаров
    fun getTopProducts(checksList: List<CheckEntity>, timeRange: String): List<ProductStat> {
        val filteredChecks = checksList.filter { isWithinTimeRange(it.date, timeRange) }
        return filteredChecks.groupBy { it.productName }
            .map { (productName, checks) ->
                ProductStat(productName, checks.sumOf { it.amount.toDouble() })
            }
            .sortedBy { it.amount }
            .take(20)
    }

    //Получение изменения цена товара
    fun getPriceChange(
        checkEntities: List<CheckEntity>,
        productName: String,
        timeRange: String
    ): List<PriceHistory> {
        val checksFirst = checkEntities.filter { it.productName == productName }.filter { isWithinTimeRange(it.date, timeRange) }
        return checksFirst.map { check ->
            PriceHistory(
                name = check.productName,
                date = check.date,
                price = check.amount.toDouble(),
                discount = check.discount
            )
        }
    }

    // Проверка даты
    private fun isWithinTimeRange(date: String, timeRange: String): Boolean {
        val parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        val now = LocalDate.now()
        return when (timeRange) {
            "Year" -> parsedDate.year == now.year
            "Month" -> parsedDate.year == now.year && parsedDate.monthValue == now.monthValue
            else -> true // "all"
        }
    }

    fun getTopCategoriesFromTransactions(
        transactions: List<Transaction>,
        timeRange: String
    ): List<CategoryStat> {
        val filtered = transactions.filter { isWithinTimeRange(it.date, timeRange) }

        return filtered.groupBy { it.category }
            .map { (category, txs) ->
                val amount = txs.sumOf { tx ->
                    if (tx.type == "expense") -tx.total else tx.total
                }
                CategoryStat(category, amount)
            }
            .sortedBy { it.amount }
            .take(20)
    }

    fun getTopSubCategories(
        transactions: List<Transaction>,
        timeRange: String
    ): List<CategoryStat> {
        val filtered = transactions.filter { isWithinTimeRange(it.date, timeRange) }

        return filtered.groupBy { it.subCategory }
            .map { (subCategory, txs) ->
                val amount = txs.sumOf { tx ->
                    if (tx.type == "expense") -tx.total else tx.total
                }
                CategoryStat(subCategory ?: "Без подкатегории", amount)
            }
            .sortedBy { it.amount }
            .take(20)
    }

    // В FireViewModel.calculateBudgetProgress()
    fun calculateBudgetProgress(budget: Budget): Flow<Pair<Double, Double>> = flow {
        val actualExpenses = fireRepository.getExpensesByBudget(budget).absoluteValue
        val percentage = (actualExpenses / budget.amount) * 100
        emit(Pair(actualExpenses, percentage))
    }
}

