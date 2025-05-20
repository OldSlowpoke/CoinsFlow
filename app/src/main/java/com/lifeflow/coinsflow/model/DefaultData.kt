package com.lifeflow.coinsflow.model

// Пример шаблонных данных
val defaultAccounts = listOf(
    Account(accountName = "Основной счет", description = "Для повседневных расходов", initialAmount = 0.0),
    Account(accountName = "Накопления", description = "Для долгосрочных целей", initialAmount = 0.0)
)

val defaultIncomeCategories = listOf(
    IncomesCategories(name = "Зарплата", subIncomesCategories = mutableListOf()),
    IncomesCategories(name = "Инвестиции", subIncomesCategories = mutableListOf())
)

val defaultExpenseCategories = listOf(
    ExpenseCategories(name = "Продукты", subExpenseCategories = mutableListOf()),
    ExpenseCategories(name = "Транспорт", subExpenseCategories = mutableListOf())
)

val defaultMarkets = listOf(
    Market(name = "Магнит", description = "Супермаркет"),
    Market(name = "Перекресток", description = "Супермаркет")
)