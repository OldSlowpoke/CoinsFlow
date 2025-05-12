package com.lifeflow.coinsflow.ui.view.statisticsScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.GroupBarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarPlotData
import co.yml.charts.ui.barchart.models.GroupBar
import co.yml.charts.ui.barchart.models.GroupBarChartData
import com.lifeflow.coinsflow.R
import com.lifeflow.coinsflow.model.CategoryStat
import com.lifeflow.coinsflow.model.MonthlyStat
import com.lifeflow.coinsflow.model.PriceHistory
import com.lifeflow.coinsflow.model.Product
import com.lifeflow.coinsflow.model.ProductStat
import com.lifeflow.coinsflow.viewModel.FireViewModel

@Composable
fun StatisticsScreen(vm: FireViewModel, navToBudgets: () -> Unit) {

    val transactions by vm.transactions.collectAsState()
    val checks by vm.checks.collectAsState()
    val productsList by vm.products.collectAsState()

    //val statsMap = vm.calculateMonthlyStats(transactions)
    //val statsList = statsMap.values.sortedBy { it.month }

    val customItems = listOf<@Composable () -> Unit>(
        // Card 1: Бюджеты
        {
            Box(
                modifier = Modifier.clickable(
                    onClick = {
                        navToBudgets()
                    }
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Бюджеты", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Посмотреть информацию о ваших бюджетах.")
                    }
                }
            }
        },
        {
            // Card 2: Баланс по месяцам
            StatsCard(title = "Баланс по месяцам") { selectProduct, filter ->
                IncomeExpenseBarChart(
                    modifier = Modifier.fillMaxWidth(),
                    statsMap = vm.calculateMonthlyStats(transactions, filter),
                )
            }
        },
        {
            // Card 3: Топ товаров
            StatsCard(title = "Топ 20 товаров") { selectProduct, filter ->
                TopProductChart(
                    sortedStats = vm.getTopProducts(checks, filter),
                )
            }
        },
        {
            // Card 4: Топ категорий
            StatsCard(title = "Топ 20 категорий") { selectProduct, filter ->
                TopCategoryChart(
                    sortedStats = vm.getTopCategoriesFromTransactions(transactions, filter),
                )
            }
        },
        {
            // Card 5: Изменение цен
            StatsCard(
                products = productsList,
                product = true,
                title = "Изменение цен"
            ) { selectProduct, filter ->
                ChangePriceProductChart(
                    priceHistory = vm.getPriceChange(
                        checkEntities = checks,
                        productName = selectProduct,
                        timeRange = filter
                    ),
                )
            }
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(customItems) {
            it()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsCard(
    products: List<Product> = emptyList(),
    title: String,
    product: Boolean = false,
    content: @Composable (String, String) -> Unit
) {

    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf(Product()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            var selectedIndex by remember { mutableIntStateOf(-1) }
            val options = listOf("All", "Years", "Months")
            SingleChoiceSegmentedButtonRow {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick = { selectedIndex = index },
                        selected = index == selectedIndex,
                        label = { Text(label) }
                    )
                }
            }
            if (product) {
                ProductField(
                    products = products,
                    selectedProduct = selectedProduct,
                    onProductChange = { newValue -> selectedProduct = newValue }
                )
                Button(
                    onClick = {
                        showBottomSheet = !showBottomSheet
                    },
                    enabled = selectedIndex >= 0,
                ) {
                    Text("Показать детали")
                }
                // Модальное окно с прокручиваемым списком
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = rememberModalBottomSheetState()
                    ) {
                        content(selectedProduct.name, options[selectedIndex])
                    }
                }
            } else {
                Button(
                    onClick = {
                        showBottomSheet = !showBottomSheet
                    },
                    enabled = selectedIndex >= 0,
                ) {
                    Text("Показать детали")
                }
                // Модальное окно с прокручиваемым списком
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = rememberModalBottomSheetState()
                    ) {
                        content(selectedProduct.name, options[selectedIndex])
                    }
                }
            }
        }
    }
}
/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    statsList: List<MonthlyStat>,
    trend: List<Double>
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    Box {
        // Основная кнопка для открытия графиков
        Button(
            onClick = { showBottomSheet = !showBottomSheet },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("График", modifier = Modifier.padding(end = 8.dp))
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
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Гистограмма доходов/расходов
                    //IncomeExpenseBarChart(statsList)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Метрики
                    Metrics(statsList)
                }
            }
        }
    }
}*/


// Гистограмма доходов/расходов
@Composable
fun IncomeExpenseBarChart(
    modifier: Modifier = Modifier,
    statsMap: Map<String, MonthlyStat>
) {
    val stats = statsMap.values.sortedBy { it.month }

    val groupBars = stats.map { stat ->
        GroupBar(
            label = stat.month,
            barList = listOf(
                BarData(Point(0f, stat.income.toFloat()), Color.Green),
                BarData(Point(1f, -(stat.expense.toFloat())), Color.Red)
            )
        )
    }

    val barPlotData = BarPlotData(
        groupBarList = groupBars,
        barColorPaletteList = listOf(Color.Blue, Color.Red)
    )

    val xAxisData = AxisData.Builder()
        .steps(stats.size) // Количество шагов = количество месяцев
        .labelData { index ->
            stats.getOrNull(index)?.month ?: ""
        } // Подписи по индексу
        .axisStepSize(30.dp)
        .axisLineColor(Color.Black)
        .axisLabelColor(Color.Black)
        .axisLabelFontSize(12.sp)
        .build()

    val maxIncome = stats.maxOfOrNull { it.income } ?: 0.0
    val maxExpense = stats.maxOfOrNull { it.expense } ?: 0.0
    val maxValue = maxOf(maxIncome, maxExpense)

    val yStepSize = 20000.0 // Шаг сетки на оси Y (можно динамически рассчитать)
    val yAxisSteps = (maxValue / yStepSize).toInt().coerceAtLeast(1)

    val yAxisData = AxisData.Builder()
        .steps(yAxisSteps) // Количество шагов на оси Y
        .labelData { index ->
            (index * yStepSize).toInt().toString()
        } // Подписи на оси Y
        .startDrawPadding(30.dp)
        .axisOffset(20.dp)
        .labelAndAxisLinePadding(20.dp)
        .axisLineColor(Color.Black)
        .axisLabelColor(Color.Black)
        .axisLabelFontSize(12.sp)
        .build()

    val groupBarChartData = GroupBarChartData(
        barPlotData = barPlotData,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        horizontalExtraSpace = 30.dp
    )

    val customItems = listOf<@Composable () -> Unit>(
        {
            GroupBarChart(
                modifier = Modifier.height(700.dp),
                groupBarChartData = groupBarChartData,
            )
        },
        { Metrics(stats) }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(customItems) {
            it()
        }
    }
}

/*@Composable
fun BalanceLineChart(stats: List<MonthlyStat>, trend: List<Double>) {
    // Преобразуем данные в точки графика
    val balancePoints = stats.mapIndexed { index, stat ->
        Point(index.toFloat(), stat.balance.toFloat())
    }

    val trendPoints = trend.mapIndexed { index, value ->
        Point(index.toFloat(), value.toFloat())
    }

    /*val pointsData: List<Point> =
        listOf(Point(0f, 40f), Point(1f, 100f), Point(2f, 0f), Point(3f, 60f), Point(4f, 10f))*/

    // Определяем линии графика
    val lines = listOf(
        Line(
            dataPoints = balancePoints,
            lineStyle = LineStyle(
                lineType = LineType.Straight(isDotted = false), // Сплошная линия
                color = Color.Green,
                width = 4f
            ),
            intersectionPoint = IntersectionPoint(),
            selectionHighlightPoint = SelectionHighlightPoint(),
            shadowUnderLine = ShadowUnderLine(),
            selectionHighlightPopUp = SelectionHighlightPopUp()
        ),
        Line(
            dataPoints = trendPoints,
            lineStyle = LineStyle(
                lineType = LineType.Straight(
                    isDotted = true,
                    intervals = floatArrayOf(10f, 10f)
                ), // Пунктирная линия
                color = Color.Blue,
                width = 4f
            ),
            intersectionPoint = IntersectionPoint(),
            selectionHighlightPoint = SelectionHighlightPoint(),
            selectionHighlightPopUp = SelectionHighlightPopUp()
        )
    )

    // Подготавливаем ось X
    val xAxisData = AxisData.Builder()
        .axisStepSize(100.dp)
        .steps(balancePoints.size - 1)
        .labelData { i -> stats[i].month }
        .labelAndAxisLinePadding(15.dp)
        .build()

    // Подготавливаем ось Y
    val yAxisMaxValue = balancePoints.maxOfOrNull { it.y.toDouble() } ?: 0.0
    val yAxisSteps = 50
    val yAxisData = AxisData.Builder()
        .steps(yAxisSteps)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i ->
            val stepValue = yAxisMaxValue / yAxisSteps
            (i * stepValue).formatToSinglePrecision()
        }
        .build()

    // Создаем данные графика
    val lineChartData = LineChartData(
        linePlotData = LinePlotData(lines = lines),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(),
        backgroundColor = Color.White
    )

    // Отрисовываем график
    LineChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        lineChartData = lineChartData
    )
}

// Расширение для форматирования чисел
fun Double.formatToSinglePrecision(): String {
    return String.format("%.1f", this)
}*/

// Отображение метрик
@Composable
fun Metrics(stats: List<MonthlyStat>) {
    val lastMonth = stats.last()
    val prevMonth = stats.getOrNull(stats.size - 2)

    println("Last Month: ${lastMonth.balance}")
    println("Prev Month: ${prevMonth?.balance}")
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Column {
            Text(
                "Баланс за ${lastMonth.month}: ${lastMonth.balance} руб",
                modifier = Modifier.padding(8.dp),
                fontSize = 18.sp, fontWeight = FontWeight.Bold
            )
            prevMonth?.let {
                val change = ((lastMonth.balance - it.balance) / it.balance * 100).toInt()
                Text(
                    "Изменение: ${if (change > 0) "+" else ""}$change%",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 18.sp, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/*
@Composable
fun BalanceBarChart(stats: List<MonthlyStat>) {
    // Подготовка данных для графика
    val barChartData = stats.map { stat ->
        BarData(
            value = stat.balance.toFloat(),
            label = stat.month,
            color = if (stat.balance >= 0) Color.Green else Color.Red
        )
    }

    // Настройка осей
    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp)
        .steps(barChartData.size - 1)
        .bottomPadding(40.dp)
        .axisLabelAngle(20f)
        .labelData { index -> barChartData[index].label }
        .build()

    val maxBalance = stats.maxOfOrNull { it.balance }?.toFloat() ?: 0f
    val yStepSize = 5
    val yAxisData = AxisData.Builder()
        .steps(yStepSize)
        .labelAndAxisLinePadding(20.dp)
        .axisOffset(20.dp)
        .labelData { index ->
            val stepValue = maxBalance / yStepSize
            (index * stepValue).toInt().toString()
        }
        .build()

    // Инициализация графика
    val barChartConfig = BarChartData(
        chartData = barChartData,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        paddingBetweenBars = 20.dp,
        barWidth = 25.dp
    )

    // Отрисовка
    BarChart(
        modifier = Modifier.height(350.dp),
        barChartData = barChartConfig
    )
}

@Composable
fun HorizontalBarChart() {
    // Подготовка данных
    val barChartData = DataUtils.getHorizontalBarChartData(data.take(20))

    // Настройка осей
    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp)
        .steps(barChartData.size - 1)
        .bottomPadding(40.dp)
        .axisLabelAngle(20f)
        .labelData { index -> barChartData[index].label }
        .build()

    val maxAmount = data.maxOfOrNull { it.amount }?.toFloat() ?: 0f
    val yStepSize = 5
    val yAxisData = AxisData.Builder()
        .steps(yStepSize)
        .labelAndAxisLinePadding(20.dp)
        .axisOffset(20.dp)
        .labelData { index ->
            val stepValue = maxAmount / yStepSize
            (index * stepValue).toInt().toString()
        }
        .build()

    // Инициализация графика
    val barChartConfig = BarChartData(
        chartData = barChartData,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        paddingBetweenBars = 20.dp,
        barWidth = 25.dp
    )

    // Отрисовка
    HorizontalBarChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        barChartData = barChartConfig
    )
}

@Composable
fun ProductPriceChart(priceHistory: List<PriceHistory>) {
    // Разделение на графики со скидкой и без
    val withoutDiscount = priceHistory.filter { !it.discount }
    val withDiscount = priceHistory.filter { it.discount }

    // Подготовка данных
    val barChartDataWithoutDiscount = withoutDiscount.map { item ->
        BarData(
            value = item.price.toFloat(),
            label = item.date,
            color = Color.Blue
        )
    }

    val barChartDataWithDiscount = withDiscount.map { item ->
        BarData(
            value = item.price.toFloat(),
            label = item.date,
            color = Color.Purple
        )
    }

    // Настройка осей
    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp)
        .steps(barChartDataWithoutDiscount.size - 1)
        .bottomPadding(40.dp)
        .axisLabelAngle(20f)
        .labelData { index ->
            if (index < barChartDataWithoutDiscount.size) {
                barChartDataWithoutDiscount[index].label
            } else {
                ""
            }
        }
        .build()

    val maxPrice = priceHistory.maxOfOrNull { it.price.toDouble() }?.toFloat() ?: 0f
    val yStepSize = 5
    val yAxisData = AxisData.Builder()
        .steps(yStepSize)
        .labelAndAxisLinePadding(20.dp)
        .axisOffset(20.dp)
        .labelData { index ->
            val stepValue = maxPrice / yStepSize
            (index * stepValue).toInt().toString()
        }
        .build()

    // Инициализация графиков
    val barChartWithoutDiscount = BarChartData(
        chartData = barChartDataWithoutDiscount,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        paddingBetweenBars = 20.dp,
        barWidth = 25.dp
    )

    val barChartWithDiscount = BarChartData(
        chartData = barChartDataWithDiscount,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        paddingBetweenBars = 20.dp,
        barWidth = 25.dp
    )

    // Отрисовка
    Column {
        Text("Цена без скидки")
        BarChart(
            modifier = Modifier.height(300.dp),
            barChartData = barChartWithoutDiscount
        )
        Text("Цена со скидкой")
        BarChart(
            modifier = Modifier.height(300.dp),
            barChartData = barChartWithDiscount
        )
    }
}

@Composable
fun ProductSelectionCard(vm: FireViewModel) {
    var selectedProduct by remember { mutableStateOf(Product()) }
    val products by vm.products.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Выберите продукт:")
        ProductField(
            products = products,
            selectedProduct = selectedProduct,
            onProductChange = { newValue -> selectedProduct = newValue }
        )
    }
}
*/
@Composable
fun ProductField(
    selectedProduct: Product,
    onProductChange: (Product) -> Unit,
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
            value = selectedProduct.name,
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
                        onProductChange(prod)
                        isDropdownOpen = false
                    }
                )
            }
        }
    }
}


@Composable
fun TopProductChart(sortedStats: List<ProductStat>) {

    val bars = sortedStats.mapIndexed { index, stat ->
        BarData(
            point = Point(
                x = index.toFloat(),
                y = stat.amount.toFloat(),
            ),
            label = stat.productName,
            color = Color.Green
        )
    }

    val xAxisData = AxisData.Builder()
        .steps(sortedStats.size) // Количество шагов
        .labelData { index ->
            sortedStats.getOrNull(index)?.productName ?: ""
        } // Подписи по индексу
        .axisStepSize(30.dp)
        .axisLineColor(Color.Black)
        .axisLabelColor(Color.Black)
        .axisLabelFontSize(12.sp)
        .build()

    val maxExpense = sortedStats.maxOfOrNull { it.amount } ?: 0.0

    val yStepSize = 10000.0 // Шаг сетки на оси Y (можно динамически рассчитать)
    val yAxisSteps = (maxExpense / yStepSize).toInt().coerceAtLeast(1)

    val yAxisData = AxisData.Builder()
        .steps(yAxisSteps) // Количество шагов на оси Y
        .labelData { index ->
            (index * yStepSize).toInt().toString()
        } // Подписи на оси Y
        .axisOffset(20.dp)
        .labelAndAxisLinePadding(20.dp)
        .axisLineColor(Color.Black)
        .axisLabelColor(Color.Black)
        .axisLabelFontSize(12.sp)
        .build()


    val barChartData = BarChartData(
        chartData = bars,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        horizontalExtraSpace = 300.dp
    )

    BarChart(modifier = Modifier.height(700.dp), barChartData = barChartData)

}

@Composable
fun TopCategoryChart(sortedStats: List<CategoryStat>) {

    val bars = sortedStats.mapIndexed { index, stat ->
        BarData(
            point = Point(
                x = index.toFloat(),
                y = stat.amount.toFloat(),
            ),
            label = stat.name,
            color = Color.Green
        )
    }

    val xAxisData = AxisData.Builder()
        .steps(sortedStats.size) // Количество шагов
        .labelData { index ->
            sortedStats.getOrNull(index)?.name ?: ""
        } // Подписи по индексу
        .axisStepSize(10000.dp)
        .startPadding(100.dp)
        .axisLineColor(Color.Black)
        .axisLabelColor(Color.Black)
        .axisLabelFontSize(12.sp)
        .build()

    val maxExpense = sortedStats.maxOfOrNull { it.amount } ?: 0.0

    val yStepSize = 10000.0 // Шаг сетки на оси Y (можно динамически рассчитать)
    val yAxisSteps = (maxExpense / yStepSize).toInt().coerceAtLeast(1)

    val yAxisData = AxisData.Builder()
        .steps(yAxisSteps) // Количество шагов на оси Y
        .labelData { index ->
            (index * yStepSize).toInt().toString()
        } // Подписи на оси Y
        .startDrawPadding(50.dp)
        .axisOffset(20.dp)
        .labelAndAxisLinePadding(20.dp)
        .axisLineColor(Color.Black)
        .axisLabelColor(Color.Black)
        .axisLabelFontSize(12.sp)
        .build()


    val barChartData = BarChartData(
        chartData = bars,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        horizontalExtraSpace = 30.dp
    )

    BarChart(modifier = Modifier.height(700.dp), barChartData = barChartData)

}

@Composable
fun ChangePriceProductChart(
    priceHistory: List<PriceHistory>
) {
    if (priceHistory.isEmpty()) {
        Text("Нет данных для отображения")
        return
    }

    // Разделение на графики со скидкой и без
    val withoutDiscount = priceHistory.filter { !it.discount }
    val withDiscount = priceHistory.filter { it.discount }

    // Подготовка данных
    val barChartDataWithoutDiscount = withoutDiscount.mapIndexed { index, stat ->
        BarData(
            point = Point(
                x = index.toFloat(),
                y = stat.price.toFloat(),
            ),
            label = stat.date,
            color = Color.Green
        )
    }

    val barChartDataWithDiscount = withDiscount.mapIndexed { index, stat ->
        BarData(
            point = Point(
                x = index.toFloat(),
                y = stat.price.toFloat(),
            ),
            label = stat.date,
            color = Color.Blue
        )
    }

    val xAxisData = AxisData.Builder()
        .steps(withoutDiscount.size) // Количество шагов
        .labelData { index ->
            withoutDiscount.getOrNull(index)?.date ?: ""
        } // Подписи по индексу
        .axisStepSize(10000.dp)
        .startPadding(100.dp)
        .axisLineColor(Color.Black)
        .axisLabelColor(Color.Black)
        .axisLabelFontSize(12.sp)
        .build()

    val maxExpense = withoutDiscount.maxOfOrNull { it.price } ?: 0.0

    val yStepSize = 10000.0 // Шаг сетки на оси Y (можно динамически рассчитать)
    val yAxisSteps = (maxExpense / yStepSize).toInt().coerceAtLeast(1)

    val yAxisData = AxisData.Builder()
        .steps(yAxisSteps) // Количество шагов на оси Y
        .labelData { index ->
            (index * yStepSize).toInt().toString()
        } // Подписи на оси Y
        .startDrawPadding(50.dp)
        .axisOffset(20.dp)
        .labelAndAxisLinePadding(20.dp)
        .axisLineColor(Color.Black)
        .axisLabelColor(Color.Black)
        .axisLabelFontSize(12.sp)
        .build()

    val xAxisDataD = AxisData.Builder()
        .steps(withDiscount.size) // Количество шагов
        .labelData { index ->
            withDiscount.getOrNull(index)?.date ?: ""
        } // Подписи по индексу
        .axisStepSize(10000.dp)
        .startPadding(100.dp)
        .axisLineColor(Color.Black)
        .axisLabelColor(Color.Black)
        .axisLabelFontSize(12.sp)
        .build()

    val maxExpenseD = withDiscount.maxOfOrNull { it.price } ?: 0.0

    val yStepSizeD = 10000.0 // Шаг сетки на оси Y (можно динамически рассчитать)
    val yAxisStepsD = (maxExpenseD / yStepSize).toInt().coerceAtLeast(1)

    val yAxisDataD = AxisData.Builder()
        .steps(yAxisStepsD) // Количество шагов на оси Y
        .labelData { index ->
            (index * yStepSizeD).toInt().toString()
        } // Подписи на оси Y
        .startDrawPadding(50.dp)
        .axisOffset(20.dp)
        .labelAndAxisLinePadding(20.dp)
        .axisLineColor(Color.Black)
        .axisLabelColor(Color.Black)
        .axisLabelFontSize(12.sp)
        .build()

    // Инициализация графиков
    val barChartWithoutDiscount = BarChartData(
        chartData = barChartDataWithoutDiscount,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
    )

    val barChartWithDiscountD = BarChartData(
        chartData = barChartDataWithDiscount,
        xAxisData = xAxisDataD,
        yAxisData = yAxisDataD,
    )

    val customItems = listOf<@Composable () -> Unit>(
        { Text("Цена без скидки") },
        {
            BarChart(
                modifier = Modifier.height(300.dp),
                barChartData = barChartWithoutDiscount
            )
        },
        { Text("Цена со скидкой") },
        {
            BarChart(
                modifier = Modifier.height(300.dp),
                barChartData = barChartWithDiscountD
            )
        }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(customItems) {
            it()
        }
    }
}



