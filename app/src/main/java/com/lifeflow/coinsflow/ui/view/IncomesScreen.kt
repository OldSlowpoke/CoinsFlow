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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.lifeflow.coinsflow.R
import com.lifeflow.coinsflow.model.Transaction
import com.lifeflow.coinsflow.viewModel.FireViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomesScreen(
    backUp: () -> Unit,
    vm: FireViewModel
) {
    var assets by remember { mutableStateOf("Актив") }
    var accounts by remember { mutableStateOf("Счет") }
    var total by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Категория") }
    var id: String

    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Поле Дата
        IncomesDateBox(
            datePickerState = datePickerState,
            selectedDate = selectedDate,
        )

        HorizontalDivider()

        // Поле Актив
        IncomesAssetBox(
            assets = assets,
            onAssetChange = { newValue -> assets = newValue }
        )
        HorizontalDivider()

        // Поле Счет
        IncomesAccountBox(
            accounts = accounts,
            onAccountChange = { newValue -> accounts = newValue }
        )

        HorizontalDivider()

        // Поле Категория
        IncomesCategoryBox(
            category = category,
            onCategoryChange = { newValue -> category = newValue }
        )

        HorizontalDivider()

        // Поле Сумма
        IncomesTotalBox(
            total = total,
            onTotalChange = { newValue -> total = newValue }
        )


        HorizontalDivider()

        // Кнопка Сохранить
        Button(
            onClick = {
                id = vm.getLinkOnFirePath("transactions")
                vm.addTransactions(
                    Transaction(
                        date = selectedDate,
                        total = total.toDouble(),
                        type = "доход",
                        category = category,
                        id = id,
                    ),
                    path = id
                )
                backUp()
                /*.isCompleted.let { answer ->
                    when (answer) {
                        true -> {
                            scope.launch {
                                snackBarHostState.showSnackbar("Транзакция удалена")
                            }
                        }
                        false -> {
                            scope.launch {
                                snackBarHostState.showSnackbar("Транзакция не удалена")
                            }
                        }
                    }
                }*/
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Сохранить")
        }
    }
}

@Composable
fun IncomesTotalBox(
    total: String,
    onTotalChange: (String) -> Unit
) {
    OutlinedTextField(
        value = total,
        onValueChange = { newValue ->
            if (
                newValue.isBlank() || newValue
                    .matches(
                        "\\d*(\\.\\d{0,2})?"
                            .toRegex()
                    )
            ) {
                onTotalChange(newValue)
            }
        },
        label = { Text("Сумма") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomesDateBox(
    selectedDate: String,
    datePickerState: DatePickerState,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selectedDate,
            onValueChange = { },
            label = { Text("Дата") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = ImageVector
                            .vectorResource(R.drawable.baseline_date_range_24),
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
fun IncomesAssetBox(
    assets: String,
    onAssetChange: (String) -> Unit
) {
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
                imageVector = ImageVector
                    .vectorResource(R.drawable.baseline_keyboard_arrow_down_24),
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

@Composable
fun IncomesAccountBox(
    accounts: String,
    onAccountChange: (String) -> Unit
) {
    var isAccountDropdownOpen by remember { mutableStateOf(false) }
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
                imageVector = ImageVector
                    .vectorResource(R.drawable.baseline_keyboard_arrow_down_24),
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
                    onAccountChange("Редактировать")
                    isAccountDropdownOpen = false
                }
            )
        }
    }
}

@Composable
fun IncomesCategoryBox(
    category: String,
    onCategoryChange: (String) -> Unit
) {
    var isCategoryDropdownOpen by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isCategoryDropdownOpen = true }
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
                imageVector = ImageVector
                    .vectorResource(R.drawable.baseline_keyboard_arrow_down_24),
                contentDescription = null
            )
        }
        DropdownMenu(
            expanded = isCategoryDropdownOpen,
            onDismissRequest = { isCategoryDropdownOpen = false },
            offset = DpOffset(x = 250.dp, y = 5.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Редактировать") },
                onClick = {
                    onCategoryChange("Редактировать")
                    isCategoryDropdownOpen = false
                }
            )
            DropdownMenuItem(
                text = { Text("Удалить") },
                onClick = {
                    onCategoryChange("Удалить")
                    isCategoryDropdownOpen = false
                }
            )
        }
    }
}
