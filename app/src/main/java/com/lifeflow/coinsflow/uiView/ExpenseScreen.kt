package com.lifeflow.coinsflow.uiView

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeflow.coinsflow.model.Expenses

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen() {
    var date = remember { mutableStateOf("Дата") }
    var activity = remember { mutableStateOf("Актив") }
    var account = remember { mutableStateOf("Счет") }
    var amount = remember { mutableStateOf("Сумма") }
    var isCheckOpen = remember { mutableStateOf(false) }
    var isActivityDropdownOpen = remember { mutableStateOf(false) }
    var isAccountDropdownOpen = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расход", textAlign = TextAlign.Center) },
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
                // Поле Дата
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Логика изменения даты */ }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = date.value,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Divider()

                // Поле Актив
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isActivityDropdownOpen.value = true }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activity.value,
                            modifier = Modifier.padding(8.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = isActivityDropdownOpen.value,
                        onDismissRequest = { isActivityDropdownOpen.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать") },
                            onClick = {}
                        )
                    }
                }
                Divider()

                // Поле Счет
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAccountDropdownOpen.value = true }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = account.value,
                            modifier = Modifier.padding(8.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = isAccountDropdownOpen.value,
                        onDismissRequest = { isAccountDropdownOpen.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать") },
                            onClick = { account.value = "Редактировать" }
                        )
                    }
                }
                Divider()

                // Поле Чек
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCheckOpen.value = true }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Чек",
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Divider()

                // Поле Сумма
                BasicTextField(
                    value = amount.value,
                    onValueChange = { amount.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                Divider()

                // Кнопка Сохранить
                Button(
                    onClick = { /* Логика сохранения */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Сохранить")
                }
            }
        }
    )
}
