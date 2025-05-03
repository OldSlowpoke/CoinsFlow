package com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.categories.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    navAddSubCategoriesScreen: () -> Unit // Теперь передаём выбранную категорию
) {
    val expenseCategories by vm.expenseCategories.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navAddCategoriesScreen() },
        ) {
            Text("Добавить категорию расходов")
        }
        // Кнопка добавления подкатегории
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            onClick = { navAddSubCategoriesScreen() }
        ) {
            Text("Добавить подкатегорию")
        }
        expenseCategories.forEachIndexed { index, category ->
            CategoryItem(
                category = category,
                mv = vm,
                onDeleteSubcategory = { subCategory -> vm.deleteSubExpenseCategory(category, subCategory) }
            )
            if (index < expenseCategories.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    thickness = 1.dp
                )
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: ExpenseCategories,
    mv: FireViewModel,
    onDeleteSubcategory: (String) -> Unit
) {
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

        // Отображение подкатегорий
        if (category.subExpenseCategories.isNotEmpty()) {
            Text(
                text = "Подкатегории:",
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 16.sp
            )
            category.subExpenseCategories.forEach { subCategory ->
                SubCategoryItem(
                    subCategory = subCategory,
                    onDelete = { onDeleteSubcategory(subCategory) }
                )
            }
        }
    }
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