package com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.categories.incomes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.lifeflow.coinsflow.R
import com.lifeflow.coinsflow.model.ExpenseCategories
import com.lifeflow.coinsflow.model.IncomesCategories
import com.lifeflow.coinsflow.viewModel.FireViewModel

@Composable
fun AddSubIncomesCategoriesCategoryScreen(
    vm: FireViewModel,
    backUp: () -> Unit,
) {
    var subCategory by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(IncomesCategories()) }
    val categories by vm.incomesCategories.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        CategoryBox(
            category = category,
            onCategoryChange = { newValue -> category = newValue },
            categories = categories
        )
        TextField(
            value = subCategory,
            onValueChange = { subCategory = it },
            placeholder = { Text("Введите название подкатегории") },
            label = { Text("Название подкатегории") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 16.dp),
            onClick = {
                vm.addSubIncomesCategory(category, subCategory)
                backUp()
            },
            enabled = subCategory.isNotBlank() || category.name.isNotBlank(),
        ) {
            Text("Сохранить")
        }
    }
}

@Composable
fun CategoryBox(
    category: IncomesCategories,
    onCategoryChange: (IncomesCategories) -> Unit,
    categories: List<IncomesCategories>
) {
    var isActivityDropdownOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isActivityDropdownOpen = true }
            .padding(vertical = 8.dp)
    ) {
        TextField(
            value = category.name,
            onValueChange = { },
            placeholder = { Text("Выберите категорию") },
            label = { Text("Категория") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { isActivityDropdownOpen = !isActivityDropdownOpen }) {
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
            expanded = isActivityDropdownOpen,
            onDismissRequest = { isActivityDropdownOpen = false },
            offset = DpOffset(x = 250.dp, y = 5.dp)
        ) {
            categories.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = { onCategoryChange(option) }
                )
            }
        }
    }
}