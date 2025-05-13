package com.lifeflow.coinsflow.ui.view.statisticsScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeflow.coinsflow.model.Budget
import com.lifeflow.coinsflow.viewModel.FireViewModel

@Composable
fun BudgetsScreen(
    vm: FireViewModel,
    navAddBudgetScreen: () -> Unit,
) {
    val budgets by vm.budgets.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navAddBudgetScreen()
                }
            ) {
                Text("Добавить")
            }
        }

        items(budgets) { budget ->
            BudgetItem(budget, vm) { vm.deleteBudget(budget) }
        }
    }
}

@Composable
fun BudgetItem(
    budget: Budget,
    vm: FireViewModel,
    onDeleteBudget: (Budget) -> Unit
) {
    var actualExpenses by remember(budget) { mutableDoubleStateOf(0.0) }
    var percentage by remember(budget) { mutableDoubleStateOf(0.0) }

    // Загрузка данных в background
    LaunchedEffect(budget) {
        vm.calculateBudgetProgress(budget).collect { (expenses, percent) ->
            actualExpenses = expenses
            percentage = percent
        }
    }

    Card (

    ){
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(
                text = "Дата: ${budget.data}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
            )
            Text(text = "Категория: ${budget.category}", fontSize = 18.sp)
            if (budget.subCategory.isNotBlank()) {
                Text(text = "Подкатегория: ${budget.subCategory}", fontSize = 16.sp)
            }
            Text(text = "Бюджет: ${budget.amount} руб", fontSize = 16.sp)
            Text(text = "Фактические траты: $actualExpenses руб", fontSize = 16.sp)
            Text(text = "Процент от бюджета: ${percentage.toInt()}%", fontSize = 16.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { onDeleteBudget(budget) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

