package com.lifeflow.coinsflow.model

data class IncomesCategories(
    var name: String = "",
    val description: String = "",
    val id: String = "",
    val subIncomesCategories: MutableList<String> = mutableListOf(),
)