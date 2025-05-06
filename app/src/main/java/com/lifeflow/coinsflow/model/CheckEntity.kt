package com.lifeflow.coinsflow.model

import java.math.BigDecimal

data class CheckEntity(
    val id: String = "",
    var productId: String = "",
    var productName: String = "",
    var count: BigDecimal = BigDecimal.ZERO,
    var amount: BigDecimal = BigDecimal.ZERO,
    var discount: Boolean = false,
    var unit: UnitType = UnitType.PIECE
)