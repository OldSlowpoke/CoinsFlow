package com.lifeflow.coinsflow.ui.view.mainScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import com.lifeflow.coinsflow.model.Product
import com.lifeflow.coinsflow.model.UnitType
import com.lifeflow.coinsflow.model.CheckEntity
import com.lifeflow.coinsflow.viewModel.FireViewModel
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckScreen(vm: FireViewModel) {
    val checkItems by vm.checkItems.collectAsState()
    val products by vm.products.collectAsState()
    val totalSum by vm.totalSum.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = { vm.addItem(vm) }, // Добавляем новый пустой элемент
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Добавить",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        Button(
            onClick = { vm.addChecks(vm.checkItems.value) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }

        Text("Сумма чека: ${totalSum}")

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(items = checkItems, key = { it.id }) { item ->
                CheckItemRow(
                    checkItem = item,
                    products = products,
                    onProductSelected = { product ->
                        vm.updateProduct(item.id, product)
                    },
                    onQuantityChange = { qty ->
                        vm.updateQuantity(item.id, qty)
                    },
                    onPriceChange = { price ->
                        vm.updatePrice(item.id, price)
                    },
                    onDiscountToggle = {
                        vm.toggleDiscount(item.id)
                    },
                    onUnitChange = { unit ->
                        vm.updateUnit(item.id, unit)
                    },
                    onRemoveClick = {
                        vm.removeItem(item.id)
                    }
                )
            }
        }
    }
}

@Composable
fun CheckItemRow(
    checkItem: CheckEntity,
    products: List<Product>,
    onProductSelected: (Product) -> Unit,
    onQuantityChange: (BigDecimal) -> Unit,
    onPriceChange: (BigDecimal) -> Unit,
    onDiscountToggle: () -> Unit,
    onUnitChange: (UnitType) -> Unit,
    onRemoveClick: () -> Unit,
) {
    var quantity by remember(checkItem.count) {
        mutableStateOf(
            if (checkItem.count.compareTo(BigDecimal.ZERO) == 0) ""
            else checkItem.count.toString()
        )
    }
    var price by remember(checkItem.amount) {
        mutableStateOf(
            if (checkItem.amount.compareTo(BigDecimal.ZERO) == 0) ""
            else checkItem.amount.toString()
        )
    }
    var selectedProduct by remember(checkItem.productName) {
        mutableStateOf(
            products.find { it.name == checkItem.productName } ?: Product("", "")
        )
    }
    var selectedUnit by remember(checkItem.unit) {
        mutableStateOf(checkItem.unit)
    }

    var isDropdownOpen by remember { mutableStateOf(false) }

    // Синхронизация при изменении checkItem
    LaunchedEffect(checkItem) {
        quantity = if (checkItem.count.compareTo(BigDecimal.ZERO) == 0) ""
        else checkItem.count.toString()
        price = if (checkItem.amount.compareTo(BigDecimal.ZERO) == 0) ""
        else checkItem.amount.toString()
        selectedProduct = products.find { it.name == checkItem.productName } ?: Product("", "")
        selectedUnit = checkItem.unit
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Поле выбора товара
            ProductBox(
                product = selectedProduct,
                onAssetChange = onProductSelected,
                products = products
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Единица измерения:")
                Box {
                    Text(
                        text = selectedUnit.name,
                        modifier = Modifier
                            .clickable { isDropdownOpen = true }
                            .padding(8.dp)
                    )
                    DropdownMenu(
                        expanded = isDropdownOpen,
                        onDismissRequest = { isDropdownOpen = false }
                    ) {
                        UnitType.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.name) },
                                onClick = {
                                    selectedUnit = unit
                                    onUnitChange(unit)
                                    isDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }

            // Поле количества
            OutlinedTextField(
                value = quantity,
                label = { Text("Количество:") },
                placeholder = { Text("Введите количество товара") },
                onValueChange = { newValue ->
                    quantity = newValue
                    val parsedQty = newValue.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    onQuantityChange(parsedQty)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )


            // Поле цены
            OutlinedTextField(
                value = price,
                label = { Text("Цена товара") },
                placeholder = { Text("Введите цену за товар") },
                onValueChange = { newValue ->
                    if (
                        newValue.isBlank() || newValue
                            .matches(
                                "\\d*(\\.\\d{0,2})?"
                                    .toRegex()
                            )
                    ) {
                        price = newValue
                        val parsedPrice = newValue.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        onPriceChange(parsedPrice)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Флаг скидки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Скидка:")
                Checkbox(
                    checked = checkItem.discount,
                    onCheckedChange = { onDiscountToggle() }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Общая сумма
                val total = remember(quantity, price, selectedUnit) {
                    derivedStateOf {
                        val qty = quantity.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        val unitPrice = price.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        val subtotal = unitPrice.multiply(qty)
                        val total = subtotal.setScale(2, RoundingMode.HALF_UP)
                        total.toDouble()
                    }
                    /*when (selectedUnit) {
                        UnitType.PIECE -> derivedStateOf {
                            val qty = quantity.toDoubleOrNull() ?: 0.0
                            val unitPrice = price.toDoubleOrNull() ?: 0.0
                            val total = qty * unitPrice
                            total
                        }
                        else -> derivedStateOf {
                            val priceForKg = price.toDoubleOrNull() ?: 0.0
                            val priceForGram = if (priceForKg >= 0) priceForKg / 1000 else 0.0
                            val qty = quantity.toDoubleOrNull() ?: 0.0
                            val total = qty * priceForGram
                            total
                        }
                    }*/
                }
                Text("Итого: ${total.value}")
                IconButton(
                    onClick = onRemoveClick,
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Удалить категорию")
                }
            }
        }
    }
}

@Composable
fun ProductBox(
    product: Product,
    onAssetChange: (Product) -> Unit,
    products: List<Product>
) {
    var isDropdownOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isDropdownOpen = true }
            .padding(vertical = 8.dp)
    ) {
        TextField(
            value = product.name,
            placeholder = { Text("Выберите товар") },
            onValueChange = { },
            label = { Text("Товар") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { isDropdownOpen = !isDropdownOpen }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.baseline_keyboard_arrow_down_24),
                        contentDescription = "Выбор товара"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        DropdownMenu(
            expanded = isDropdownOpen,
            onDismissRequest = { isDropdownOpen = false },
            offset = DpOffset(x = 250.dp, y = 5.dp)
        ) {
            products.forEach { prod ->
                DropdownMenuItem(
                    text = { Text(prod.name) },
                    onClick = {
                        onAssetChange(prod)
                        isDropdownOpen = false
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

/*private fun formatNumber(value: Double, unit: UnitType): String {
    return when (unit) {
        UnitType.PIECE -> value.toInt().toString() // Например: 2.0 → "2"
        else -> "%.3f".format(value) // Например: 1.234 → "1.234"
    }
}

private fun parseQuantity(value: String, unit: UnitType): Double {
    return when (unit) {
        UnitType.PIECE -> {
            val intValue = value.toIntOrNull()
            // Для штук: значение должно быть ≥ 1.0
            intValue?.coerceAtLeast(1)?.toDouble() ?: 1.0
        }

        else -> {
            // Проверяем, что ввод соответствует формату: цифры, точка, до 3 знаков после точки
            if (value.matches(Regex("^\\d*\\.?\\d{0,3}\$"))) {
                value.toDoubleOrNull() ?: 0.0
            } else {
                // Если формат неверен, удаляем последний символ и рекурсивно продолжаем
                if (value.isNotEmpty()) {
                    parseQuantity(value.dropLast(1), unit)
                } else {
                    0.0
                }
            }
        }
    }
}*/