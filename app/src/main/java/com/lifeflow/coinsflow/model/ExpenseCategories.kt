package com.lifeflow.coinsflow.model

data class ExpenseCategories (
    var name: String = "",
    val description: String = "",
    val id: String = "",
    val subExpenseCategories: MutableList<String> = mutableListOf(),
)