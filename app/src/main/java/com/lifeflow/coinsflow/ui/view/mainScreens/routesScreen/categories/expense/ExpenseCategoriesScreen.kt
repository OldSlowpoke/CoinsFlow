package com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.categories.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeflow.coinsflow.model.ExpenseCategories
import com.lifeflow.coinsflow.viewModel.FireViewModel

@Composable
fun ExpenseCategoriesScreen(
    vm: FireViewModel,
    navAddCategoriesScreen: () -> Unit,
    navAddSubCategoriesScreen: () -> Unit
) {
    val expenseCategories by vm.expenseCategories.collectAsState()

    LazyColumn( // Замените Column на LazyColumn
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item { // Кнопки как отдельный элемент списка
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navAddCategoriesScreen() },
            ) {
                Text("Добавить категорию расходов")
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                onClick = { navAddSubCategoriesScreen() }
            ) {
                Text("Добавить подкатегорию")
            }
        }

        items(expenseCategories) { category -> // Используйте items для категорий
            CategoryItem(
                category = category,
                mv = vm,
                onDeleteSubcategory = { subCategory ->
                    vm.deleteSubExpenseCategory(category, subCategory)
                }
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: ExpenseCategories,
    mv: FireViewModel,
    onDeleteSubcategory: (String) -> Unit
) {
    var hasSubCategories by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = category.name,
            fontSize = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = category.description,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(6f)
            )
            IconButton(
                onClick = { mv.deleteExpenseCategories(category) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Удалить категорию")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    hasSubCategories = !hasSubCategories
                }
        ) {
            Text(
                text = "Подкатегории:",
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 16.sp
            )
        }
        // Отображение подкатегорий
        if (category.subExpenseCategories.isNotEmpty() && hasSubCategories) {
            category.subExpenseCategories.forEach { subCategory ->
                SubCategoryItem(
                    subCategory = subCategory,
                    onDelete = { onDeleteSubcategory(subCategory) }
                )
            }
        }
    }
    HorizontalDivider()
}

@Composable
fun SubCategoryItem(
    subCategory: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = subCategory, fontSize = 14.sp)
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Удалить подкатегорию")
        }
    }
}