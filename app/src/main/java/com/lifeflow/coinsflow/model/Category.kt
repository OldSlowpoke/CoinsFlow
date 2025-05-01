package com.lifeflow.coinsflow.model

data class Category (
    var name: String = "",
    val description: String = "",
    val id: String = "",
    val subcategories: MutableList<String> = mutableListOf(),
)