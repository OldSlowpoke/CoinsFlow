package com.lifeflow.coinsflow.ui.view

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun convertMillisToDateBudget(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun String.formatDateYyyyMmDdToDdMmYyyy(): String {
    return try {
        val date = LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    } catch (e: Exception) {
        this // Возвращаем исходную строку, если парсинг невозможен
    }
}