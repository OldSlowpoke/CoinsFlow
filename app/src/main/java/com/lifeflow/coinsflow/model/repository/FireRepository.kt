package com.lifeflow.coinsflow.model.repository

import android.util.Log
import androidx.compose.runtime.currentComposer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.lifeflow.coinsflow.model.Account
import com.lifeflow.coinsflow.model.Check
import com.lifeflow.coinsflow.model.CheckEntity
import com.lifeflow.coinsflow.model.ExpenseCategories
import com.lifeflow.coinsflow.model.IncomesCategories
import com.lifeflow.coinsflow.model.Market
import com.lifeflow.coinsflow.model.Product
import com.lifeflow.coinsflow.model.Transaction
import com.lifeflow.coinsflow.model.UnitType
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

    // Метод для остановки всех слушателей
    fun stopAllListeners() {
        transactionListener?.remove()
        productListener?.remove()
        expenseCategoriesListener?.remove()
        accountsListener?.remove()
        marketsListener?.remove()
        incomesCategoriesListener?.remove()

        transactionListener = null
        productListener = null
        expenseCategoriesListener = null
        accountsListener = null
        marketsListener = null
        incomesCategoriesListener = null
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

    /*suspend fun saveChecksAndTransaction(
        checkEntities: MutableList<CheckEntity>,
        transaction: Transaction,
        path: String
    ): Result<Unit> {
        return try {
            // Проверка на пустой список чеков
            if (checkEntities.isEmpty()) {
                Log.w("FireViewModel", "Попытка сохранить транзакцию без чеков")
                return Result.failure(IllegalStateException("Нет чеков для сохранения"))
            }

            // Сохранение чеков и получение их ссылок
            val checksLinks = addChecks(checkEntities)
            transaction.checkLinks = checksLinks

            // Сохранение транзакции
            addTransaction(transaction, path)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FireRepository", "Ошибка сохранения чеков и транзакции", e)
            Result.failure(e)
        }
    }*/

    suspend fun saveChecksAndTransaction(
        checkEntities: MutableList<CheckEntity>,
        transaction: Transaction,
        path: String
    ): Result<Unit> {
        return try {
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

            Result.success(Unit)
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

    suspend fun addTransaction(transaction: Transaction, id: String) {
        try {
            firestore
                .collection("users")
                .document(currentUserId())
                .collection("transaction")
                .document(id)
                .set(transaction)
                .await()
        } catch (e: Exception) {
            Log.e("FireRepository", "Ошибка при сохранении транзакции", e)
            throw e
        }
    }

    suspend fun deleteTransactions(transaction: Transaction) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("transaction")
            .document(transaction.id)
            .delete()
            .await()
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

    suspend fun addSubExpenseCategory(expenseCategories: ExpenseCategories, subCategory: String) {
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
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
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
        date = this.date
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
        date = this.date
    )
}




