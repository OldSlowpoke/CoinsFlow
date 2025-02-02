package com.lifeflow.coinsflow.model

data class Incomes(
    val incomeId: Int,
    val accountId: Int,
    val type: String,
    val date: String,
    val total: Double
)