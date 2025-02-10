package com.lifeflow.coinsflow.ui.view

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(nv: NavHostController) {
    var date by remember { mutableStateOf("Дата") }
    var activity by remember { mutableStateOf("Актив") }
    var account by remember { mutableStateOf("Счет") }
    var amount by remember { mutableStateOf("") }
    var isCheckOpen by remember { mutableStateOf(false) }
    var isActivityDropdownOpen by remember { mutableStateOf(false) }
    var isAccountDropdownOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расход", textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = { nv.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    DatePickerDocked()
                }
                HorizontalDivider()

                // Поле Актив
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isActivityDropdownOpen = true }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activity,
                            modifier = Modifier.padding(8.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = isActivityDropdownOpen,
                        onDismissRequest = { isActivityDropdownOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать") },
                            onClick = {}
                        )
                    }
                }
                HorizontalDivider()

                // Поле Счет
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAccountDropdownOpen = true }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = account,
                            modifier = Modifier.padding(8.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = isAccountDropdownOpen,
                        onDismissRequest = { isAccountDropdownOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать") },
                            onClick = { account = "Редактировать" }
                        )
                    }
                }
                HorizontalDivider()

                // Поле Чек
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCheckOpen = true }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Чек",
                            modifier = Modifier.padding(8.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
                HorizontalDivider()

                // Поле Сумма
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isBlank() || newValue.matches("-?\\d*(\\.\\d*)?".toRegex())) {
                            amount = newValue
                        }
                    },
                    label = { Text("Сумма") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                HorizontalDivider()

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
