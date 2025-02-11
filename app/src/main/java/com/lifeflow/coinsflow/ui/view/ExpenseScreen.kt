package com.lifeflow.coinsflow.ui.view

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavHostController
import com.lifeflow.coinsflow.model.Asset
import com.lifeflow.coinsflow.viewModel.FireViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(nv: NavHostController, mv: FireViewModel) {
    var assets by remember { mutableStateOf("Актив") }
    var accounts by remember { mutableStateOf("Счет") }
    var total by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Категория") }

    var isCheckOpen by remember { mutableStateOf(false) }
    var isActivityDropdownOpen by remember { mutableStateOf(false) }
    var isAccountDropdownOpen by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

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
                DateBox(
                    datePickerState = datePickerState,
                    selectedDate = selectedDate,
                )

                HorizontalDivider()

                // Поле Актив
                AssetBox(
                    assets = assets,
                    onAssetChange = { newValue -> assets = newValue }
                )
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
                            text = accounts,
                            modifier = Modifier.padding(8.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = isAccountDropdownOpen,
                        onDismissRequest = { isAccountDropdownOpen = false },
                        offset = DpOffset(x = 250.dp, y = 5.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать") },
                            onClick = {
                                accounts = "Редактировать"
                                isAccountDropdownOpen = false
                            }
                        )
                    }
                }
                HorizontalDivider()

                // Поле Категория
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
                            text = category,
                            modifier = Modifier.padding(8.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = isAccountDropdownOpen,
                        onDismissRequest = { isAccountDropdownOpen = false },
                        offset = DpOffset(x = 250.dp, y = 5.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать") },
                            onClick = {
                                category = "Редактировать"
                                isAccountDropdownOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить") },
                            onClick = {
                                category = "Удалить"
                                isAccountDropdownOpen = false
                            }
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
                    value = total,
                    onValueChange = { newValue ->
                        if (
                            newValue.isBlank() || newValue
                                .matches(
                                    "-\\d*(\\.\\d{0,2})?"
                                        .toRegex()
                                )
                        ) {
                            total = newValue
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
                    onClick = {
                        mv.addTransactions(
                            com.lifeflow.coinsflow.model.Transactions(
                                date = selectedDate,
                                total = total.toDouble(),
                                type = "расход",

                                )
                        )
                    },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateBox(
    selectedDate: String,
    datePickerState: DatePickerState,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { },
            label = { Text("Дата") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        if (showDatePicker) {
            Popup(
                onDismissRequest = { showDatePicker = false },
                alignment = Alignment.TopStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 64.dp)
                        .shadow(elevation = 4.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
                }
            }
        }
    }
}

@Composable
fun AssetBox(
    assets: String,
    onAssetChange: (String) -> Unit
){
    var isActivityDropdownOpen by remember { mutableStateOf(false) }
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
                text = assets,
                modifier = Modifier.padding(8.dp)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }
        DropdownMenu(
            expanded = isActivityDropdownOpen,
            onDismissRequest = { isActivityDropdownOpen = false },
            offset = DpOffset(x = 250.dp, y = 5.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Редактировать") },
                onClick = {
                    onAssetChange("Редактировать")
                    isActivityDropdownOpen = false
                }
            )
        }
    }
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}