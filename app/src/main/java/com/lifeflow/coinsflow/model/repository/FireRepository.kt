package com.lifeflow.coinsflow.model.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import com.lifeflow.coinsflow.model.Category
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

    // Метод для остановки всех слушателей
    fun stopAllListeners() {
        transactionListener?.remove()
        productListener?.remove()
        categoryListener?.remove()

        transactionListener = null
        productListener = null
        categoryListener = null
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
}


