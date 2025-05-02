package com.lifeflow.coinsflow.model

data class Transaction(
    val id: String = "",
    val type: String = "",
    val date: String = "",
    val account: String = "",
    val market: String = "",
    val category: String = "",
    val subCategory: String = "",
    val total: Double = 0.0,
    val checkLinks: MutableList<String> = mutableListOf(),
    val check: MutableList<Check> = mutableListOf()
)