package com.lifeflow.coinsflow.model

data class Transactions(
    val id: String = "",
    val date: String = "",
    val type: String = "",
    val category: String = "",
    val total: Double = 0.0
)