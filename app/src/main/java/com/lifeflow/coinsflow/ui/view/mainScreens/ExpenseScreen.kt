package com.lifeflow.coinsflow.ui.view.mainScreens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.lifeflow.coinsflow.R
import com.lifeflow.coinsflow.model.Account
import com.lifeflow.coinsflow.model.ExpenseCategories
import com.lifeflow.coinsflow.model.Market
import com.lifeflow.coinsflow.model.Transaction
import com.lifeflow.coinsflow.model.CheckEntity
import com.lifeflow.coinsflow.model.Product
import com.lifeflow.coinsflow.model.UnitType
import com.lifeflow.coinsflow.ui.view.convertMillisToDate
import com.lifeflow.coinsflow.viewModel.FireViewModel
import java.math.BigDecimal
import java.math.RoundingMode


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    backUp: () -> Unit,
    vm: FireViewModel,
) {
    val accounts by vm.accounts.collectAsState()
    val markets by vm.markets.collectAsState()
    val categories by vm.expenseCategories.collectAsState()
    val checkItems by vm.checkItems.collectAsState()
    val products by vm.products.collectAsState()
    val totalSum by vm.totalSum.collectAsState()

    var accountState by remember { mutableStateOf(Account()) }
    var marketState by remember { mutableStateOf(Market()) }
    var categoryState by remember { mutableStateOf(ExpenseCategories()) }
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
        ExpensesDateBox(
            datePickerState = datePickerState,
            selectedDate = selectedDate,
        )

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp)
        )

        // Поле Счет
        ExpensesAccountBox(
            account = accountState,
            onAccountChange = { newValue -> accountState = newValue },
            accounts = accounts
        )

        HorizontalDivider()

        // Поле Актив
        ExpensesMarketBox(
            market = marketState,
            onAssetChange = { newValue -> marketState = newValue },
            markets = markets

        )

        HorizontalDivider()

        // Поле Категория
        ExpensesCategoryBox(
            category = categoryState,
            onCategoryChange = { newValue -> categoryState = newValue },
            categories = categories,
        )

        if (categoryState.subExpenseCategories.isNotEmpty()) {
            // Показываем подкатегорию только если у категории есть подкатегории
            HorizontalDivider()

            ExpensesSubCategoryBox(
                subCategories = categoryState.subExpenseCategories,
                selectedSubCategory = subCategory,
                onSubCategoryChange = { newValue -> subCategory = newValue }
            )
        }

        HorizontalDivider()

        // Поле Чек
        ExpenseCheckBox(
            products,
            checkItems,
            vm,
            totalSum
        )
        if (checkItems.isEmpty()) {
            HorizontalDivider()
            // Поле Сумма
            ExpensesTotalBox(
                total = totalState,
                onTotalChange = { newValue -> totalState = newValue }
            )
        }

        HorizontalDivider()

        // Кнопка Сохранить
        Button(
            onClick = {
                id = vm.getLinkOnFirePath("transactions")
                vm.saveChecksAndTransaction(
                    checkItems,
                    Transaction(
                        date = selectedDate,
                        total = -(
                                if (totalSum != 0.0) totalSum
                                else totalState.toDouble()
                                ),
                        type = "expense",
                        account = accountState.accountName,
                        category = categoryState.name,
                        market = marketState.name,
                        id = id,
                        subCategory = subCategory
                    ),
                    path = id
                )
                vm.clearCheck()
                backUp()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = accountState.accountName.isNotBlank()
                    && marketState.name.isNotBlank()
                    && categoryState.name.isNotBlank()
                    && selectedDate.isNotBlank()
        ) {
            Text("Сохранить")
        }
    }
}

@Composable
fun ExpensesTotalBox(
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
fun ExpensesDateBox(
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
fun ExpensesMarketBox(
    market: Market,
    onAssetChange: (Market) -> Unit,
    markets: List<Market>
) {
    var isActivityDropdownOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isActivityDropdownOpen = true }
            .padding(vertical = 8.dp)
    ) {
        TextField(
            value = market.name,
            placeholder = { Text("Выберите магазин") },
            onValueChange = { },
            label = { Text("Магазин") },
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
            markets.forEach { market ->
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
fun ExpensesAccountBox(
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
fun ExpensesCategoryBox(
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
fun ExpensesSubCategoryBox(
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
fun ExpenseCheckBox(
    products: List<Product>,
    checkItems: MutableList<CheckEntity>,
    vm: FireViewModel,
    totalSum: Double
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    Box {
        // Основная кнопка для открытия списка чеков
        Button(
            onClick = { showBottomSheet = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Чек", modifier = Modifier.padding(end = 8.dp))
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.baseline_keyboard_arrow_down_24),
                contentDescription = null
            )
        }

        // Модальное окно с прокручиваемым списком
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Список товаров и услуг",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text("Сумма чека: ${totalSum}")

                    Button(
                        onClick = { vm.addItem(vm) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text("Добавить товар")
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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