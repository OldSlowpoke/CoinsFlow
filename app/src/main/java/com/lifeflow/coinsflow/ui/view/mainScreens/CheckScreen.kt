package com.lifeflow.coinsflow.ui.view.mainScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.lifeflow.coinsflow.R
import com.lifeflow.coinsflow.model.Check
import com.lifeflow.coinsflow.model.Product
import com.lifeflow.coinsflow.viewModel.FireViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckScreen(
    vm: FireViewModel
) {
    val checkItems by vm.checkItems.collectAsState()
    val products by vm.products.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                vm.addItem(Check())
            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                "Добавить",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(checkItems) { item ->
                ExpenseItemRow(checkItem = item, products = products)
            }
        }
        // Кнопка Сохранить
        Button(
            onClick = { /* Логика сохранения */ },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text("Сохранить")
        }
    }
}

@Composable
fun ExpenseItemRow(checkItem: Check, products: List<Product>) {

    var productName by remember { mutableStateOf(Product()) }
    var amount by remember { mutableStateOf("") }
    var count by remember { mutableStateOf(0) }
    var discount by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxSize().padding(end = 8.dp, top = 4.dp)
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            ProductBox(
                product = productName,
                onAssetChange = { newItem -> productName = newItem },
                products = products
            )
            CheckboxWithText(
                checkedState = discount,
                onCheckedChange = { discount = !discount }
            )
            // Поле Сумма
            ExpensesBox(
                total = amount,
                onTotalChange = { newValue -> amount = newValue }
            )
        }
    }
}

@Composable
fun ProductBox(
    product: Product,
    onAssetChange: (Product) -> Unit,
    products: List<Product>
) {
    var isActivityDropdownOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isActivityDropdownOpen = true }
            .padding(vertical = 8.dp)
    ) {
        TextField(
            value = product.name,
            placeholder = { Text("Выберите товар или услугу") },
            onValueChange = { },
            label = { Text("Товар") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { isActivityDropdownOpen = !isActivityDropdownOpen }) {
                    Icon(
                        imageVector = ImageVector
                            .vectorResource(R.drawable.baseline_keyboard_arrow_down_24),
                        contentDescription = "Выбор товара"
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
            products.forEach { market ->
                DropdownMenuItem(
                    text = { Text(market.name) },
                    onClick = {
                        onAssetChange(market)
                        isActivityDropdownOpen = false
                    }
                )
            }
        }
    }
}

@Composable
fun CheckboxWithText(
    checkedState: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checkedState,
            onCheckedChange = { onCheckedChange() }
        )
        Text(text = "Скидка")
    }
}

@Composable
fun ExpensesBox(
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
                        "-\\d*(\\.\\d{0,2})?"
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