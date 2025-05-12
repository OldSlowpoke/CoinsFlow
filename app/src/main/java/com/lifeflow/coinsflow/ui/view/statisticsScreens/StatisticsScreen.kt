package com.lifeflow.coinsflow.ui.view.statisticsScreens

import android.util.Printer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.GroupBarChart
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarPlotData
import co.yml.charts.ui.barchart.models.GroupBar
import co.yml.charts.ui.barchart.models.GroupBarChartData
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.LineType
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.lifeflow.coinsflow.R
import com.lifeflow.coinsflow.model.MonthlyStat
import com.lifeflow.coinsflow.viewModel.FireViewModel

@Composable
fun StatisticsScreen(vm: FireViewModel) {
    val transactions by vm.transactions.collectAsState()

    val statsMap = vm.calculateMonthlyStats(transactions)
    val statsList = statsMap.values.sortedBy { it.month }

    if (statsList.isEmpty()) {
        Text("Нет данных", modifier = Modifier.padding(16.dp))
        return
    }

    val trend = vm.calculateTrend(statsList)

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        TestScreen(
            statsList = statsList,
            trend = trend
        )
    }


}

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
            onClick = { showBottomSheet = true },
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
                    IncomeExpenseBarChart(statsList)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Линейный график баланса
                    BalanceLineChart(statsList, trend)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Метрики
                    Metrics(statsList)
                }
            }
        }
    }
}


// Гистограмма доходов/расходов
@Composable
fun IncomeExpenseBarChart(stats: List<MonthlyStat>) {
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

    val yStepSize = 500.0 // Шаг сетки на оси Y (можно динамически рассчитать)
    val yAxisSteps = (maxValue / yStepSize).toInt().coerceAtLeast(1)

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

    val groupBarChartData = GroupBarChartData(
        barPlotData = barPlotData,
        xAxisData = xAxisData,
        yAxisData = yAxisData
    )

    GroupBarChart(
        modifier = Modifier.height(300.dp),
        groupBarChartData = groupBarChartData
    )
}

/*@Composable
fun BalanceLineChart(){
    val pointsData: List<Point> =
        listOf(Point(0f, 40f), Point(1f, 1000f), Point(2f, 0f), Point(3f, 60f), Point(4f, 10f))
    val xAxisData = AxisData.Builder()
        .axisStepSize(100.dp)
        .backgroundColor(Color.Blue)
        .steps(pointsData.size - 1)
        .labelData { i -> i.toString() }
        .labelAndAxisLinePadding(15.dp)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(10)
        .backgroundColor(Color.Red)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i ->
            val yScale = 1000.0 / 10.0
            (i * yScale).formatToSinglePrecision()
        }.build()
    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData,
                    LineStyle(),
                    IntersectionPoint(),
                    SelectionHighlightPoint(),
                    ShadowUnderLine(),
                    SelectionHighlightPopUp()
                )
            ),
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(),
        backgroundColor = Color.White
    )
    LineChart(
        modifier = Modifier
            .fillMaxSize(),
        lineChartData = lineChartData
    )
}*/

@Composable
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
}

// Отображение метрик
@Composable
fun Metrics(stats: List<MonthlyStat>) {
    val lastMonth = stats.last()
    val prevMonth = stats.getOrNull(stats.size - 2)

    println("Last Month: ${lastMonth.balance}")
    println("Prev Month: ${prevMonth?.balance}")

    Column {
        Text("Баланс за ${lastMonth.month}: ${lastMonth.balance} руб")
        prevMonth?.let {
            val change = ((lastMonth.balance - it.balance) / it.balance * 100).toInt()
            Text("Изменение: ${if (change > 0) "+" else ""}$change%")
        }
    }
}


