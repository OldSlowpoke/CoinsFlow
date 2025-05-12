package com.lifeflow.coinsflow.model

data class MonthlyStat(
    val month: String,
    val income: Double,
    val expense: Double,
    val balance: Double
)