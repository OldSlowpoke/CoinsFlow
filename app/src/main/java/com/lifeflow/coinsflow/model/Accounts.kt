package com.lifeflow.coinsflow.model

data class Accounts(
    val accountId: Int,
    val assetId: Int,
    val accountsName: String,
    val initialAmount: Double,
    val userid: Int
)