package com.lifeflow.coinsflow.model

data class Transaction(
    val id: String = "",
    val date: String = "",
    val type: String = "",
    val category: String = "",
    val total: Double = 0.0,
    val check: List<Check> = listOf()
)