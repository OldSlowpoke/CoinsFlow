package com.lifeflow.coinsflow.model.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.lifeflow.coinsflow.model.Account
import com.lifeflow.coinsflow.model.Category
import com.lifeflow.coinsflow.model.Market
import com.lifeflow.coinsflow.model.Product
import com.lifeflow.coinsflow.model.Transaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FireRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    // Хранение ссылок на слушатели
    private var transactionListener: ListenerRegistration? = null
    private var productListener: ListenerRegistration? = null
    private var categoryListener: ListenerRegistration? = null
    private var accountsListener: ListenerRegistration? = null
    private var marketsListener: ListenerRegistration? = null

    // Метод для остановки всех слушателей
    fun stopAllListeners() {
        transactionListener?.remove()
        productListener?.remove()
        categoryListener?.remove()
        accountsListener?.remove()
        marketsListener?.remove()

        transactionListener = null
        productListener = null
        categoryListener = null
        accountsListener = null
        marketsListener = null
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

    suspend fun addTransaction(transaction: Transaction, id: String) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("transaction")
            .document(id)
            .set(transaction)
            .await()
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
        return firestore.collection(path).document().id
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

    //Categories
    fun getCategories(): Flow<List<Category>> = callbackFlow {
        val snapshotListener = firestore
            .collection("users")
            .document(currentUserId())
            .collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val category = snapshot.toObjects(Category::class.java)
                    trySend(category)
                }
            }
        awaitClose { categoryListener?.remove() }
    }

    suspend fun addCategory(category: Category, id: String) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("categories")
            .document(id)
            .set(category)
            .await()
    }

    suspend fun addSubCategory(category: Category, subCategory: String) {
        try {
            firestore
                .collection("users")
                .document(currentUserId())
                .collection("categories")
                .document(category.id)
                .update("subCategories", FieldValue.arrayUnion(subCategory))
                .await()
        } catch (e: Exception) {
            // Обработка ошибок (например, логирование)
            Log.e("FireRepository", "Error adding subcategory", e)
            throw e
        }
    }

    suspend fun deleteSubCategory(category: Category, subCategory: String) {
        try {
            firestore
                .collection("users")
                .document(currentUserId())
                .collection("categories")
                .document(category.id)
                .update("subCategories", FieldValue.arrayRemove(subCategory))
                .await()
        } catch (e: Exception) {
            // Логируем ошибку
            Log.e("FireRepository", "Error deleting subcategory", e)
            throw e
        }
    }

    suspend fun deleteCategory(category: Category) {
        firestore
            .collection("users")
            .document(currentUserId())
            .collection("categories")
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


