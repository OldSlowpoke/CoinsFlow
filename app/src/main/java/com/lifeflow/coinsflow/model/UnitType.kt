package com.lifeflow.coinsflow.model

enum class UnitType(name: String) {
    PIECE("Единица"),   // Штуки (целые числа)
    KILOGRAM("Килограмм"), // Килограммы (дробные до 3 знаков)
    LITER("Литр")     // Литры (дробные до 3 знаков)
}