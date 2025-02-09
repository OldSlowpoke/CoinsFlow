package com.lifeflow.coinsflow.model.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lifeflow.coinsflow.model.Expenses
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FireRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    fun getExpenses(): Flow<List<Expenses>> = callbackFlow {
        val snapshotListener = firestore.collection("Expenses")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val expenses = snapshot.toObjects(Expenses::class.java)
                    trySend(expenses)
                }
            }
        awaitClose { snapshotListener.remove() }
    }

    suspend fun addExpenses(expenses: Expenses) {
        firestore.collection("Expenses").add(expenses).await()
    }
}

