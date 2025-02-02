package com.lifeflow.coinsflow.uiView

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeflow.coinsflow.model.Accounts
import com.lifeflow.coinsflow.model.Expenses
import com.lifeflow.coinsflow.model.Incomes
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.lifeflow.coinsflow.ui.theme.GrayDark
import com.lifeflow.coinsflow.ui.theme.GrayLight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    // Инициализация счетов
    val accounts = listOf(
        Accounts(
            accountId = 1,
            assetId = 101,
            accountsName = "Счет 1",
            initialAmount = 1000.0,
            userid = 1
        )
    )

// Инициализация трат
    val expenses = listOf(
        Expenses(
            expenseId = 1,
            accountId = 1,
            date = "2023-10-01",
            type = "Продукты",
            total = 150.0
        ),
        Expenses(
            expenseId = 2,
            accountId = 1,
            date = "2023-10-01",
            type = "Транспорт",
            total = 50.0
        ),
        Expenses(
            expenseId = 3,
            accountId = 1,
            date = "2023-10-03",
            type = "Развлечения",
            total = 200.0
        ),
        Expenses(
            expenseId = 4,
            accountId = 1,
            date = "2023-10-04",
            type = "Образование",
            total = 300.0
        ),
        Expenses(
            expenseId = 5,
            accountId = 1,
            date = "2023-10-04",
            type = "Здоровье",
            total = 100.0
        ),
        Expenses(
            expenseId = 6,
            accountId = 1,
            date = "2023-10-04",
            type = "Коммунальные услуги",
            total = 120.0
        ),
        Expenses(
            expenseId = 7,
            accountId = 1,
            date = "2023-10-07",
            type = "Интернет",
            total = 50.0
        ),
        Expenses(
            expenseId = 8,
            accountId = 1,
            date = "2023-10-08",
            type = "Мобильная связь",
            total = 30.0
        ),
        Expenses(
            expenseId = 9,
            accountId = 1,
            date = "2023-10-08",
            type = "Одежда",
            total = 250.0
        ),
        Expenses(
            expenseId = 10,
            accountId = 1,
            date = "2023-10-10",
            type = "Рестораны",
            total = 180.0
        ),
        Expenses(
            expenseId = 11,
            accountId = 1,
            date = "2023-10-11",
            type = "Путешествия",
            total = 500.0
        ),
        Expenses(
            expenseId = 12,
            accountId = 1,
            date = "2023-10-12",
            type = "Подарки",
            total = 80.0
        ),
        Expenses(
            expenseId = 13,
            accountId = 1,
            date = "2023-10-13",
            type = "Спорт",
            total = 70.0
        ),
        Expenses(
            expenseId = 14,
            accountId = 1,
            date = "2023-10-14",
            type = "Книги",
            total = 40.0
        ),
        Expenses(
            expenseId = 15,
            accountId = 1,
            date = "2023-10-15",
            type = "Подписки",
            total = 20.0
        ),
        Expenses(
            expenseId = 16,
            accountId = 1,
            date = "2023-10-16",
            type = "Благотворительность",
            total = 50.0
        ),
        Expenses(
            expenseId = 17,
            accountId = 1,
            date = "2023-10-17",
            type = "Ремонт",
            total = 300.0
        ),
        Expenses(
            expenseId = 18,
            accountId = 1,
            date = "2023-10-18",
            type = "Мебель",
            total = 400.0
        )
    )


    val incomes = listOf(
        Incomes(
            incomeId = 1,
            accountId = 1,
            type = "Зарплата",
            date = "2023-10-01",
            total = 5000.0
        ),
        Incomes(
            incomeId = 2,
            accountId = 1,
            type = "Фриланс",
            date = "2023-10-01",
            total = 1500.0
        ),
        Incomes(
            incomeId = 3,
            accountId = 1,
            type = "Дивиденды",
            date = "2023-10-03",
            total = 300.0
        ),
        Incomes(
            incomeId = 4,
            accountId = 1,
            type = "Аренда",
            date = "2023-10-04",
            total = 800.0
        ),
        Incomes(
            incomeId = 5,
            accountId = 1,
            type = "Продажа имущества",
            date = "2023-10-04",
            total = 2000.0
        ),
        Incomes(
            incomeId = 6,
            accountId = 1,
            type = "Подарок",
            date = "2023-10-04",
            total = 500.0
        ),
        Incomes(
            incomeId = 7,
            accountId = 1,
            type = "Премия",
            date = "2023-10-07",
            total = 1000.0
        ),
        Incomes(
            incomeId = 8,
            accountId = 1,
            type = "Консультации",
            date = "2023-10-08",
            total = 700.0
        ),
        Incomes(
            incomeId = 9,
            accountId = 1,
            type = "Инвестиции",
            date = "2023-10-09",
            total = 1200.0
        ),
        Incomes(
            incomeId = 10,
            accountId = 1,
            type = "Роялти",
            date = "2023-10-10",
            total = 400.0
        ),
        Incomes(
            incomeId = 11,
            accountId = 1,
            type = "Фриланс",
            date = "2023-10-11",
            total = 1500.0
        ),
        Incomes(
            incomeId = 12,
            accountId = 1,
            type = "Зарплата",
            date = "2023-10-12",
            total = 5000.0
        ),
        Incomes(
            incomeId = 13,
            accountId = 1,
            type = "Дивиденды",
            date = "2023-10-13",
            total = 300.0
        ),
        Incomes(
            incomeId = 14,
            accountId = 1,
            type = "Аренда",
            date = "2023-10-15",
            total = 800.0
        ),
        Incomes(
            incomeId = 15,
            accountId = 1,
            type = "Продажа имущества",
            date = "2023-10-15",
            total = 2000.0
        ),
        Incomes(
            incomeId = 16,
            accountId = 1,
            type = "Подарок",
            date = "2023-10-17",
            total = 500.0
        ),
        Incomes(
            incomeId = 17,
            accountId = 1,
            type = "Премия",
            date = "2023-10-17",
            total = 1000.0
        ),
        Incomes(
            incomeId = 18,
            accountId = 1,
            type = "Консультации",
            date = "2023-10-18",
            total = 700.0
        )
    )


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar()
        Expence(expenses)
        //Incom()
    }
}

@Composable
fun Incom() {
    TODO("Not yet implemented")
}

@Composable
fun TransactionItem(transaction: Expenses, onDelete: () -> Unit) {
    var value by rememberSaveable { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp, end = 5.dp, top = 1.dp, bottom = 5.dp)
            .background(GrayLight)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(15.dp)
            ) {
                Text(
                    text = transaction.type,
                    modifier = Modifier.weight(4f),
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = transaction.total.toString(),
                    modifier = Modifier.weight(2f)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { value = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            DropdownMenu(
                expanded = value,
                onDismissRequest = { value = false },
                offset = DpOffset(x = 270.dp, y = 5.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Редактировать") },
                    onClick = {}
                )
                DropdownMenuItem(
                    text = { Text("Удалить") },
                    onClick = {
                        onDelete()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Expence(expenses: List<Expenses>) {
    val groups = expenses.groupBy { it.date }
    LazyColumn {
        groups.forEach { (date, incomes) ->
            stickyHeader {
                Text(
                    text = date,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 10.dp, start = 5.dp, end = 5.dp, bottom = 3.dp)
                        .background(GrayDark)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            items(incomes.size) { index ->
                TransactionItem(transaction = incomes[index], onDelete = {})
            }
        }
    }
}

@Composable
fun TopBar() {
    Card(
        modifier = Modifier
            .height(300.dp)
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 5.dp),
        colors = CardDefaults.cardColors(GrayLight)
    ) {
        Text(
            text = "Hello, world!",
            fontSize = 30.sp
        )
    }
}
