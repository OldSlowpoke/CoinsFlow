package com.lifeflow.coinsflow.model.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lifeflow.coinsflow.model.Transactions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FireRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    fun getTransactions(): Flow<List<Transactions>> = callbackFlow {
        val snapshotListener = firestore.collection("transaction")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val transactions = snapshot.toObjects(Transactions::class.java)
                    trySend(transactions)
                }
            }
        awaitClose { snapshotListener.remove() }
    }

    suspend fun addTransactions(transactions: Transactions, id: String) {
        firestore.collection("transaction").document(id).set(transactions).await()
    }

    suspend fun deleteTransactions(transactions: Transactions) {
        firestore.collection("transaction").document(transactions.id).delete().await()
    }

    fun getLinkOnFirePath(path: String): String {
        return firestore.collection(path).document().id
    }

    /*suspend fun updateTransactions(transactions: Transactions) {
        firestore.collection("transaction")
            .document(transactions.id)
            .update(transactions)
            .await()
    }*/
}

