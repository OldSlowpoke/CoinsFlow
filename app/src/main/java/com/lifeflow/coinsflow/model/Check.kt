package com.lifeflow.coinsflow.model

data class Check (
    var productName: String = "",
    val amount: Double = 0.0,
    val count: Int = 0,
    val discount: Boolean = false,
    val productId: String,
)
