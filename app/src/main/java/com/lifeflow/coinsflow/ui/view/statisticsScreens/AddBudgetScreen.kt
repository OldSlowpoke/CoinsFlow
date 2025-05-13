package com.lifeflow.coinsflow.ui.view.statisticsScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.lifeflow.coinsflow.model.Budget
import com.lifeflow.coinsflow.model.ExpenseCategories
import com.lifeflow.coinsflow.ui.view.convertMillisToDate
import com.lifeflow.coinsflow.ui.view.convertMillisToDateBudget
import com.lifeflow.coinsflow.ui.view.mainScreens.ExpensesDateBox
import com.lifeflow.coinsflow.ui.view.mainScreens.ExpensesTotalBox
import com.lifeflow.coinsflow.viewModel.FireViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreen(
    vm: FireViewModel,
    backUp: () -> Unit,
) {
    val categories by vm.expenseCategories.collectAsState()

    var id: String
    var categoryState by remember { mutableStateOf(ExpenseCategories()) }
    var subCategory by remember { mutableStateOf("") }
    var totalState by remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDateBudget(it)
    } ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Поле Дата
        BudgetDateBox(
            datePickerState = datePickerState,
            selectedDate = selectedDate,
        )

        // Поле Категория
        BudgetCategoryBox(
            category = categoryState,
            onCategoryChange = { newValue -> categoryState = newValue },
            categories = categories,
        )

        if (categoryState.subExpenseCategories.isNotEmpty()) {
            // Показываем подкатегорию только если у категории есть подкатегории
            HorizontalDivider()

            BudgetSubCategoryBox(
                subCategories = categoryState.subExpenseCategories,
                selectedSubCategory = subCategory,
                onSubCategoryChange = { newValue -> subCategory = newValue }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        BudgetTotalBox(
            total = totalState,
            onTotalChange = { newValue -> totalState = newValue }
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, top = 16.dp),
            onClick = {
                id = vm.getLinkOnFirePath("products")
                vm.addBudget(
                    Budget(
                        data = selectedDate,
                        category = categoryState.name,
                        subCategory = subCategory,
                        amount = totalState.toDouble(),
                        id = id
                    ),
                    path = id
                )
                backUp()
            },
            enabled = selectedDate.isNotBlank()
                    && categoryState.name.isNotBlank()
                    && totalState.isNotBlank()
        ) {
            Text("Сохранить")
        }
    }
}

@Composable
fun BudgetCategoryBox(
    category: ExpenseCategories,
    onCategoryChange: (ExpenseCategories) -> Unit,
    categories: List<ExpenseCategories>,
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
fun BudgetSubCategoryBox(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDateBox(
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
fun BudgetTotalBox(
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