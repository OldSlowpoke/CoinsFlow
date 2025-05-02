package com.lifeflow.coinsflow.ui.view.mainScreens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lifeflow.coinsflow.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckScreen(

) {
    var expenses by remember { mutableStateOf(listOf<Product>()) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Чек",
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        expenses = expenses + Product(name = "Новый элемент", amount = 0.0)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        "Добавить",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(expenses) { item ->
                        ExpenseItemRow(item = item)
                    }
                }
                // Кнопка Сохранить
                Button(
                    onClick = { /* Логика сохранения */ },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text("Сохранить")
                }
            }
        }
    )
}

@Composable
fun ExpenseItemRow(item: Product) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = item.name, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Сумма: ${item.amount}", style = MaterialTheme.typography.bodySmall)
    }
}