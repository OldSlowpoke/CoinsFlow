package com.lifeflow.coinsflow.model

data class Check (
    var productName: String = "",
    val amount: Long = 0,
    var count: Long = 0,
    val discount: Boolean = false,
    val id: String = "",
    var unit: String = UnitType.PIECE.name,
    val date: String = "",
    var unitPrice: Long = 0L // Цена за единицу измерения (в Long, хранится как копейки/центы)
)
