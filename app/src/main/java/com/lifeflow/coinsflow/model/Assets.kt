package com.lifeflow.coinsflow.model

import android.accounts.Account

data class Assets(
    val assetType: String,
    val accounts: List<Accounts>
)