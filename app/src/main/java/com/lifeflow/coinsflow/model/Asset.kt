package com.lifeflow.coinsflow.model

data class Asset(
    val assetType: String,
    val accounts: List<com.lifeflow.coinsflow.model.Account>
)