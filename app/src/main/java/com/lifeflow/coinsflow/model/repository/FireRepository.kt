package com.lifeflow.coinsflow.model.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.lifeflow.coinsflow.model.Account
import com.lifeflow.coinsflow.model.Budget
import com.lifeflow.coinsflow.model.Check
import com.lifeflow.coinsflow.model.CheckEntity
import com.lifeflow.coinsflow.model.ExpenseCategories
import com.lifeflow.coinsflow.model.IncomesCategories
import com.lifeflow.coinsflow.model.Market
import com.lifeflow.coinsflow.model.Product
import com.lifeflow.coinsflow.model.Transaction
import com.lifeflow.coinsflow.model.UnitType
import com.lifeflow.coinsflow.model.defaultAccounts
import com.lifeflow.coinsflow.model.defaultExpenseCategories
import com.lifeflow.coinsflow.model.defaultIncomeCategories
import com.lifeflow.coinsflow.model.defaultMarkets
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class FireRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    // Хранение ссылок на слушатели
    private var transactionListener: ListenerRegistration? = null
    private var productListener: ListenerRegistration? = null
    private var expenseCategoriesListener: ListenerRegistration? = null
    private var incomesCategoriesListener: ListenerRegistration? = null
    private var accountsListener: ListenerRegistration? = null
    private var marketsListener: ListenerRegistration? = null
    private var checksListener: ListenerRegistration? = null
    private var budgetListener: ListenerRegistration? = null

    // Метод для остановки всех слушателей
    fun stopAllListeners() {
        transactionListener?.remove()
        productListener?.remove()
        expenseCategoriesListener?.remove()
        accountsListener?.remove()
        marketsListener?.remove()
        incomesCategoriesListener?.remove()
        checksListener?.remove()
        budgetListener?.remove()

        transactionListener = null
        productListener = null
        expenseCategoriesListener = null
        accountsListener = null
        marketsListener = null
        incomesCategoriesListener = null
        checksListener = null
        budgetListener = null
    }

    //Budget
    fun getBudgets(): Flow<List<Budget>> = callbackFlow {
        val snapshotListener = firestore
            .collection("users")
            .document(currentUserId())
            .collection("budget")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val budget = snapshot.toObjects(Budget::class.java)
                    trySend(budget)
                }
            }
        awaitClose { budgetListener?.remove() }
    }

    suspend fun addBudget(budget: Budget, id: String) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("budget")
            .document(id)
            .set(budget)
            .await()
    }

    suspend fun deleteBudget(budget: Budget) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("budget")
            .document(budget.id)
            .delete()
            .await()
    }


    //Transactions
    fun getTransactions(): Flow<List<Transaction>> = callbackFlow {
        val snapshotListener = firestore
            .collection("users")
            .document(currentUserId())
            .collection("transaction")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val transaction = snapshot.toObjects(Transaction::class.java)
                    trySend(transaction)
                }
            }
        awaitClose { transactionListener?.remove() }
    }

    // Функция для получения чеков по ID транзакции
    suspend fun getChecksForTransaction(checkIds: List<String>): List<Check> {
        val userId = currentUserId()
        val checks = mutableListOf<Check>()
        for (id in checkIds) {
            val doc = firestore.collection("users").document(userId)
                .collection("checks").document(id).get().await()
            if (doc.exists()) {
                checks.add(doc.toObject(Check::class.java)!!)
            }
        }
        return checks
    }

    //Checks
    fun getChecks(): Flow<List<CheckEntity>> = callbackFlow {
        val snapshotListener = firestore
            .collection("users")
            .document(currentUserId())
            .collection("checks")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val checks = snapshot.toObjects(Check::class.java)
                    val checkEntity = checks.map { check ->
                        check.toCheckEntity()
                    }
                    trySend(checkEntity)

                }
            }
        awaitClose { checksListener?.remove() }
    }

    suspend fun saveChecksAndTransaction(
        checkEntities: MutableList<CheckEntity> = mutableListOf(),
        transaction: Transaction,
        path: String
    ): Result<Unit> {
        return try {
            // Проверка на пустой список чеков
            if (checkEntities.isEmpty()) {
                val userId = currentUserId()
                val transactionRef = firestore.collection("users")
                    .document(userId)
                    .collection("transaction")
                    .document(path)
                firestore.runBatch { batch ->
                    batch.set(transactionRef, transaction) // Использование transactionRef

                }.await()
                updateAccountBalance(
                    transaction.account,
                    transaction.total.toBigDecimal()
                )
                Result.success(Unit)
            } else {
                val userId = currentUserId()

                // Добавьте дату транзакции в каждый чек
                val checksWithDate = checkEntities.map { entity ->
                    entity.copy(date = transaction.date) // Устанавливаем дату транзакции
                }

                // Получение ссылки на документ транзакции
                val transactionRef = firestore.collection("users")
                    .document(userId)
                    .collection("transaction")
                    .document(path)

                firestore.runBatch { batch ->
                    // 1. Добавление чеков в батч
                    for (entity in checksWithDate) {
                        val checkRef = firestore.collection("users")
                            .document(userId)
                            .collection("checks")
                            .document(entity.id)
                        val check = entity.toCheck()
                        batch.set(checkRef, check)
                    }

                    // 2. Обновление транзакции с ссылками на чеки
                    val checkLinks = checksWithDate.map { it.id }.toMutableList()
                    transaction.checkLinks = checkLinks
                    batch.set(transactionRef, transaction) // Использование transactionRef
                }.await()
                updateAccountBalance(
                    transaction.account,
                    transaction.total.toBigDecimal()
                )
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e("FireRepository", "Ошибка пакетного сохранения чеков и транзакции", e)
            Result.failure(e)
        }
    }

    suspend fun addChecks(checks: MutableList<CheckEntity>): MutableList<String> {
        val checksLinks = mutableListOf<String>()
        try {
            for (entity in checks) {
                val check = entity.toCheck()
                firestore.collection("users")
                    .document(currentUserId())
                    .collection("checks")
                    .document(entity.id)
                    .set(check)
                    .await() // Ожидаем завершения операции
                checksLinks.add(entity.id)
            }
        } catch (e: Exception) {
            Log.e("FireRepository", "Ошибка при сохранении чеков", e)
            throw e
        }
        return checksLinks
    }

    suspend fun deleteTransactions(transaction: Transaction) {
        try {
            val userId = currentUserId()
            val transactionRef = firestore.collection("users").document(userId)
                .collection("transaction").document(transaction.id)

            // Удаляем связанные чеки
            val checkRefs = transaction.checkLinks.map { checkId ->
                firestore.collection("users").document(userId).collection("checks")
                    .document(checkId)
            }

            firestore.runBatch { batch ->
                checkRefs.forEach { batch.delete(it) }
                batch.delete(transactionRef)
            }.await()
            updateAccountBalance(
                transaction.account,
                transaction.total.toBigDecimal(),
                deleteTransaction = true
            )
        } catch (e: Exception) {
            Log.e("FireRepository", "Ошибка при удалении транзакции", e)
            throw e
        }
    }

    fun getLinkOnFirePath(path: String): String {
        return firestore
            .collection("users")
            .document(currentUserId())
            .collection(path)
            .document().id
    }

    //Products
    fun getProducts(): Flow<List<Product>> = callbackFlow {
        val snapshotListener = firestore
            .collection("users")
            .document(currentUserId())
            .collection("products")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val products = snapshot.toObjects(Product::class.java)
                    trySend(products)
                }
            }
        awaitClose { productListener?.remove() }
    }

    suspend fun addProduct(product: Product, id: String) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("products")
            .document(id)
            .set(product)
            .await()
    }

    suspend fun deleteProduct(product: Product) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("products")
            .document(product.id)
            .delete()
            .await()
    }

    //ExpenseCategories
    fun getExpenseCategories(): Flow<List<ExpenseCategories>> = callbackFlow {
        val snapshotListener = firestore
            .collection("users")
            .document(currentUserId())
            .collection("expenseCategories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val category = snapshot.toObjects(ExpenseCategories::class.java)
                    trySend(category)
                }
            }
        awaitClose { expenseCategoriesListener?.remove() }
    }

    suspend fun addExpenseCategory(expenseCategories: ExpenseCategories, id: String) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("expenseCategories")
            .document(id)
            .set(expenseCategories)
            .await()
    }

    suspend fun addSubExpenseCategory(
        expenseCategories: ExpenseCategories,
        subCategory: String
    ) {
        try {
            firestore
                .collection("users")
                .document(currentUserId())
                .collection("expenseCategories")
                .document(expenseCategories.id)
                .update("subExpenseCategories", FieldValue.arrayUnion(subCategory))
                .await()
        } catch (e: Exception) {
            // Обработка ошибок (например, логирование)
            Log.e("FireRepository", "Error adding subcategory", e)
            throw e
        }
    }

    suspend fun deleteSubExpenseCategory(
        expenseCategories: ExpenseCategories,
        subCategory: String
    ) {
        try {
            firestore
                .collection("users")
                .document(currentUserId())
                .collection("expenseCategories")
                .document(expenseCategories.id)
                .update("subExpenseCategories", FieldValue.arrayRemove(subCategory))
                .await()
        } catch (e: Exception) {
            // Логируем ошибку
            Log.e("FireRepository", "Error deleting subcategory", e)
            throw e
        }
    }

    suspend fun deleteExpenseCategory(expenseCategories: ExpenseCategories) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("expenseCategories")
            .document(expenseCategories.id)
            .delete()
            .await()
    }

    //IncomeCategories
    fun getIncomesCategories(): Flow<List<IncomesCategories>> = callbackFlow {
        val snapshotListener = firestore
            .collection("users")
            .document(currentUserId())
            .collection("incomesCategories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val category = snapshot.toObjects(IncomesCategories::class.java)
                    trySend(category)
                }
            }
        awaitClose { incomesCategoriesListener?.remove() }
    }

    suspend fun addIncomesCategory(category: IncomesCategories, id: String) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("incomesCategories")
            .document(id)
            .set(category)
            .await()
    }

    suspend fun addSubIncomesCategory(category: IncomesCategories, subCategory: String) {
        try {
            firestore
                .collection("users")
                .document(currentUserId())
                .collection("incomesCategories")
                .document(category.id)
                .update("subIncomesCategories", FieldValue.arrayUnion(subCategory))
                .await()
        } catch (e: Exception) {
            // Обработка ошибок (например, логирование)
            Log.e("FireRepository", "Error adding subcategory", e)
            throw e
        }
    }

    suspend fun deleteSubIncomesCategory(category: IncomesCategories, subCategory: String) {
        try {
            firestore
                .collection("users")
                .document(currentUserId())
                .collection("incomesCategories")
                .document(category.id)
                .update("subIncomesCategories", FieldValue.arrayRemove(subCategory))
                .await()
        } catch (e: Exception) {
            // Логируем ошибку
            Log.e("FireRepository", "Error deleting subcategory", e)
            throw e
        }
    }

    suspend fun deleteIncomesCategory(category: IncomesCategories) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("incomesCategories")
            .document(category.id)
            .delete()
            .await()
    }

    /*suspend fun updateTransactions(transactions: Transactions) {
        firestore.collection("transaction")
            .document(transactions.id)
            .update(transactions)
            .await()
    }*/

    suspend fun login(email: String, password: String): Result<FirebaseUser> =
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun register(email: String, password: String): Result<FirebaseUser> =
        try {
            // Шаг 1: Регистрация пользователя в Firebase Auth
            val authResult =
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("User creation failed")

            // Шаг 2: Создание документа пользователя в Firestore
            val newUserDoc = hashMapOf(
                "email" to email,
                "uid" to user.uid
            )

            firestore.collection("users")
                .document(user.uid)
                .set(newUserDoc) // Создаем документ пользователя
                .await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    private fun currentUserId(): String =
        firebaseAuth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

    fun logout() {
        stopAllListeners() // Остановка всех Firestore-слушателей
        firebaseAuth.signOut()
    }

    //Accounts
    fun getAccounts(): Flow<List<Account>> = callbackFlow {
        val snapshotListener = firestore
            .collection("users")
            .document(currentUserId())
            .collection("accounts")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val accounts = snapshot.toObjects(Account::class.java)
                    trySend(accounts)
                }
            }
        awaitClose { accountsListener?.remove() }
    }

    suspend fun addAccount(account: Account, idAccount: String) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("accounts")
            .document(idAccount)
            .set(account)
            .await()
    }

    suspend fun deleteAccount(account: Account) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("accounts")
            .document(account.id)
            .delete()
            .await()
    }

    //Markets
    fun getMarkets(): Flow<List<Market>> = callbackFlow {
        val snapshotListener = firestore
            .collection("users")
            .document(currentUserId())
            .collection("markets")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val markets = snapshot.toObjects(Market::class.java)
                    trySend(markets)
                }
            }
        awaitClose { marketsListener?.remove() }
    }

    suspend fun addMarket(market: Market, id: String) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("markets")
            .document(id)
            .set(market)
            .await()
    }

    suspend fun deleteMarket(market: Market) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("markets")
            .document(market.id)
            .delete()
            .await()
    }

    private suspend fun updateAccountBalance(
        accountName: String,
        amount: BigDecimal,
        deleteTransaction: Boolean = false
    ) {
        try {
            val userId = currentUserId()
            val accountRef = firestore
                .collection("users")
                .document(userId)
                .collection("accounts")
                .whereEqualTo("accountName", accountName)
                .get()
                .await()
                .firstOrNull() ?: return

            val currentBalance =
                accountRef.toObject(Account::class.java).initialAmount.toBigDecimal()
                    ?: BigDecimal.ZERO

            // Основная формула
            val newBalance = if (deleteTransaction) {
                currentBalance.subtract(amount)
            } else {
                currentBalance.add(amount)
            }

            // Проверка на отрицательный баланс
            if (newBalance.signum() == -1) {
                throw IllegalStateException("Баланс не может быть отрицательным")
            }


            firestore.collection("users")
                .document(userId)
                .collection("accounts")
                .document(accountRef.id)
                .update("initialAmount", newBalance.toDouble())
                .await()

        } catch (e: Exception) {
            Log.e("FireRepository", "Ошибка обновления баланса счета", e)
        }
    }

    suspend fun getExpensesByBudget(budget: Budget): Double {
        val userId = currentUserId()
        val budgetMonthYear = budget.data // Уже в формате "MM-yyyy"

        val query = firestore.collection("users")
            .document(userId)
            .collection("transaction")
            .whereEqualTo("type", "expense")
            .whereEqualTo("category", budget.category)

        // Добавляем условие для подкатегории, если указана
        if (budget.subCategory.isNotBlank()) {
            query.whereEqualTo("subCategory", budget.subCategory)
        }

        val snapshot = query.get().await()
        val transactions = snapshot.toObjects(Transaction::class.java)

        // Фильтруем транзакции по месяцу и году
        val filteredTransactions = transactions.filter { transaction ->
            val transactionMonthYear = extractMonthYear(transaction.date)
            transactionMonthYear == budgetMonthYear
        }

        return filteredTransactions.sumOf { it.total }
    }

    private suspend fun checkIfBaseCollectionsExist(userId: String): Boolean {
        try {
            // Проверяем наличие документов во всех ключевых коллекциях
            val accountsSnapshot = firestore.collection("users")
                .document(userId)
                .collection("accounts")
                .get()
                .await()

            val incomesCategoriesSnapshot = firestore.collection("users")
                .document(userId)
                .collection("incomesCategories")
                .get()
                .await()

            val expenseCategoriesSnapshot = firestore.collection("users")
                .document(userId)
                .collection("expenseCategories")
                .get()
                .await()

            val marketsSnapshot = firestore.collection("users")
                .document(userId)
                .collection("markets")
                .get()
                .await()

            // Если хотя бы одна коллекция не пуста - данные уже инициализированы
            return accountsSnapshot.isEmpty ||
                    incomesCategoriesSnapshot.isEmpty ||
                    expenseCategoriesSnapshot.isEmpty ||
                    marketsSnapshot.isEmpty

        } catch (e: Exception) {
            Log.e("FireRepository", "Ошибка проверки коллекций", e)
            throw e
        }
    }

    // FireRepository.kt
    suspend fun initializeBaseCollections(userId: String) {
        try {

            if (checkIfBaseCollectionsExist(userId)) {
                // 1. Добавление счетов
                for (account in defaultAccounts) {
                    val accountId =
                        firestore
                            .collection("users")
                            .document(userId)
                            .collection("accounts")
                            .document().id
                    firestore
                        .collection("users")
                        .document(userId)
                        .collection("accounts")
                        .document(accountId)
                        .set(account.copy(id = accountId))
                        .await()
                }

                // 2. Добавление категорий доходов
                for (category in defaultIncomeCategories) {
                    val categoryId =
                        firestore
                            .collection("users")
                            .document(userId)
                            .collection("incomesCategories")
                            .document().id
                    firestore
                        .collection("users")
                        .document(userId)
                        .collection("incomesCategories")
                        .document(categoryId)
                        .set(category.copy(id = categoryId))
                        .await()
                }

                // 3. Добавление категорий расходов
                for (category in defaultExpenseCategories) {
                    val categoryId =
                        firestore.collection("users")
                            .document(userId)
                            .collection("expenseCategories")
                            .document().id
                    firestore
                        .collection("users")
                        .document(userId)
                        .collection("expenseCategories")
                        .document(categoryId)
                        .set(category.copy(id = categoryId))
                        .await()
                }

                // 4. Добавление магазинов
                for (market in defaultMarkets) {
                    val marketId = firestore
                        .collection("users")
                        .document(userId)
                        .collection("markets")
                        .document().id
                    firestore
                        .collection("users")
                        .document(userId).collection("markets")
                        .document(marketId)
                        .set(market.copy(id = marketId))
                        .await()
                }
            }
        } catch (e: Exception) {
            Log.e("FireRepository", "Ошибка при инициализации данных", e)
            throw e
        }
    }

}


// Из CheckEntity в Check
fun CheckEntity.toCheck(): Check {
    return Check(
        productName = this.productName,
        count = this.count.multiply(BigDecimal("1000")) // Умножаем на 1000 для перевода в Long
            .setScale(0, RoundingMode.HALF_UP)
            .toBigIntegerExact()
            .toLong(),
        amount = this.amount.multiply(BigDecimal("100")) // Умножаем на 100 для перевода в Long
            .setScale(0, RoundingMode.HALF_UP)
            .toBigIntegerExact()
            .toLong(),
        discount = this.discount,
        unit = this.unit.name, // Преобразуем UnitType в String
        id = this.id,
        date = this.date,
        unitPrice = (this.unitPrice.multiply(BigDecimal("100"))) // Преобразование BigDecimal -> Long
            .setScale(0, RoundingMode.HALF_UP)
            .toBigIntegerExact()
            .toLong()
    )
}

// Из Check в CheckEntity
fun Check.toCheckEntity(): CheckEntity {
    return CheckEntity(
        productName = this.productName,
        count = BigDecimal.valueOf(this.count) // Преобразуем Long в BigDecimal
            .divide(BigDecimal("1000"), 3, RoundingMode.HALF_UP), // Делим на 1000
        amount = BigDecimal.valueOf(this.amount) // Преобразуем Long в BigDecimal
            .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP), // Делим на 100
        discount = this.discount,
        unit = UnitType.valueOf(this.unit), // Преобразуем String в UnitType
        id = this.id,
        date = this.date,
        unitPrice = BigDecimal.valueOf(this.unitPrice).divide(
            BigDecimal("100"),
            2,
            RoundingMode.HALF_UP
        ) // Преобразование Long -> BigDecimal
    )
}

private fun extractMonthYear(dateString: String): String {
    val parts = dateString.split("-")
    if (parts.size == 3) {
        return "${parts[0]}-${parts[1]}"
    }
    throw IllegalArgumentException("Неверный формат даты: $dateString")
}




