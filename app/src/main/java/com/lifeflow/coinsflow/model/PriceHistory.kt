package com.lifeflow.coinsflow.model

import java.math.BigDecimal

data class PriceHistory(
    val name: String,
    val date: String,
    val price: Double,
    val discount: Boolean
)