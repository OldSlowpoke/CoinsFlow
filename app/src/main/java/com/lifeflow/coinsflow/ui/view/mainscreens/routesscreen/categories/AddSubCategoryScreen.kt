package com.lifeflow.coinsflow.ui.view.mainscreens.routesscreen.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.lifeflow.coinsflow.R
import com.lifeflow.coinsflow.model.Category
import com.lifeflow.coinsflow.model.SubCategory
import com.lifeflow.coinsflow.viewModel.FireViewModel

@Composable
fun AddSubCategoryScreen(
    vm: FireViewModel,
    backUp: () -> Unit,
) {
    var SubCategory by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category()) }
    val categories by vm.categories.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CategoryBox(
            category = category,
            onAssetChange = { newValue -> category = newValue },
            categories = categories
        )
        TextField(
            value = SubCategory,
            onValueChange = { SubCategory = it },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                vm.addSubCategory(category, SubCategory)
                backUp()
            },
            enabled = SubCategory.isNotBlank() || category.name.isNotBlank(),
        ) {
            Text("Сохранить")
        }
    }
}

@Composable
fun CategoryBox(
    category: Category,
    onAssetChange: (Category) -> Unit,
    categories: List<Category>
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
                text = category.name,
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
            categories.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = { onAssetChange(option) }
                )
            }
        }
    }
}