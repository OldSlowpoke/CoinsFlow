package com.lifeflow.coinsflow.model

data class Expenses(
    val expenseId: Int = 0,
    val accountId: Int = 0,
    val date: String = "",
    val type: String = "",
    val total: Double = 0.0
)