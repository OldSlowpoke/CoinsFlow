package com.lifeflow.coinsflow.ui.view.mainScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.lifeflow.coinsflow.model.Account
import com.lifeflow.coinsflow.model.ExpenseCategories
import com.lifeflow.coinsflow.model.IncomesCategories
import com.lifeflow.coinsflow.model.Market
import com.lifeflow.coinsflow.model.Transaction
import com.lifeflow.coinsflow.ui.view.convertMillisToDate
import com.lifeflow.coinsflow.viewModel.FireViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomesScreen(
    backUp: () -> Unit,
    vm: FireViewModel
) {
    val accounts by vm.accounts.collectAsState()
    val categories by vm.incomesCategories.collectAsState()

    var accountState by remember { mutableStateOf(Account()) }
    var categoryState by remember { mutableStateOf(IncomesCategories()) }
    var totalState by remember { mutableStateOf("") }
    var subCategory by remember { mutableStateOf("") }

    var id: String

    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

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

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp)
        )

        // Поле Счет
        IncomesAccountBox(
            account = accountState,
            onAccountChange = { newValue -> accountState = newValue },
            accounts = accounts
        )

        HorizontalDivider()

        // Поле Категория
        IncomesCategoryBox(
            category = categoryState,
            onCategoryChange = { newValue -> categoryState = newValue },
            categories = categories,
        )

        if (categoryState.subIncomesCategories.isNotEmpty()) {
            // Показываем подкатегорию только если у категории есть подкатегории
            HorizontalDivider()

            IncomesSubCategoryBox(
                subCategories = categoryState.subIncomesCategories,
                selectedSubCategory = subCategory,
                onSubCategoryChange = { newValue -> subCategory = newValue }
            )
        }


        HorizontalDivider()

        // Поле Сумма
        IncomesTotalBox(
            total = totalState,
            onTotalChange = { newValue -> totalState = newValue }
        )


        HorizontalDivider()

        // Кнопка Сохранить
        Button(
            onClick = {
                id = vm.getLinkOnFirePath("transactions")
                vm.saveChecksAndTransaction(
                    mutableListOf(),
                    Transaction(
                        date = selectedDate,
                        total = totalState.toDouble(),
                        type = "income",
                        account = accountState.accountName,
                        category = categoryState.name,
                        id = id,
                        subCategory = subCategory
                    ),
                    path = id
                )
                backUp()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = accountState.accountName.isNotBlank()
                    && categoryState.name.isNotBlank()
                    && selectedDate.isNotBlank()
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
        placeholder = { Text("Введите сумму") },
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
            placeholder = { Text("Выберите дату") },
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
fun IncomesAccountBox(
    account: Account,
    onAccountChange: (Account) -> Unit,
    accounts: List<Account>
) {
    var isAccountDropdownOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isAccountDropdownOpen = true }
            .padding(vertical = 8.dp)
    ) {
        TextField(
            value = account.accountName,
            placeholder = { Text("Выберите счет") },
            onValueChange = { },
            label = { Text("Счет") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { isAccountDropdownOpen = !isAccountDropdownOpen }) {
                    Icon(
                        imageVector = ImageVector
                            .vectorResource(R.drawable.baseline_keyboard_arrow_down_24),
                        contentDescription = "Выбор категории"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )
        DropdownMenu(
            expanded = isAccountDropdownOpen,
            onDismissRequest = { isAccountDropdownOpen = false },
            offset = DpOffset(x = 250.dp, y = 5.dp)
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.accountName) },
                    onClick = {
                        onAccountChange(account)
                        isAccountDropdownOpen = false
                    }
                )
            }
        }
    }
}

@Composable
fun IncomesCategoryBox(
    category: IncomesCategories,
    onCategoryChange: (IncomesCategories) -> Unit,
    categories: List<IncomesCategories>,
) {
    var isCategoryDropdownOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isCategoryDropdownOpen = !isCategoryDropdownOpen }
            .padding(vertical = 8.dp)
    ) {
        TextField(
            value = category.name,
            placeholder = { Text("Выберите категорию") },
            onValueChange = { },
            label = { Text("Категория") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { isCategoryDropdownOpen = !isCategoryDropdownOpen }) {
                    Icon(
                        imageVector = ImageVector
                            .vectorResource(R.drawable.baseline_keyboard_arrow_down_24),
                        contentDescription = "Выбор категории"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )
        DropdownMenu(
            expanded = isCategoryDropdownOpen,
            onDismissRequest = { isCategoryDropdownOpen = false },
            offset = DpOffset(x = 250.dp, y = 5.dp)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategoryChange(category)
                        isCategoryDropdownOpen = false
                    }
                )
            }
        }
    }
}

@Composable
fun IncomesSubCategoryBox(
    subCategories: MutableList<String>,
    selectedSubCategory: String,
    onSubCategoryChange: (String) -> Unit
) {
    var isSubCategoryDropdownOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isSubCategoryDropdownOpen = !isSubCategoryDropdownOpen }
            .padding(vertical = 8.dp)
    ) {
        TextField(
            value = selectedSubCategory,
            placeholder = { Text("Выберите подкатегорию") },
            onValueChange = { },
            label = { Text("Подкатегория") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { isSubCategoryDropdownOpen = !isSubCategoryDropdownOpen }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.baseline_keyboard_arrow_down_24),
                        contentDescription = "Выбор подкатегории"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        DropdownMenu(
            expanded = isSubCategoryDropdownOpen,
            onDismissRequest = { isSubCategoryDropdownOpen = false },
            offset = DpOffset(x = 250.dp, y = 5.dp)
        ) {
            subCategories.forEach { subCategory ->
                DropdownMenuItem(
                    text = { Text(subCategory) },
                    onClick = {
                        onSubCategoryChange(subCategory)
                        isSubCategoryDropdownOpen = false
                    }
                )
            }
        }
    }
}