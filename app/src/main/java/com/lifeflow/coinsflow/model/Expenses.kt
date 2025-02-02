package com.lifeflow.coinsflow.model

data class Expenses(
    val expenseId: Int,
    val accountId: Int,
    val date: String,
    val type: String,
    val total: Double
)