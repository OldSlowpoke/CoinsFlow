/*
CoinsFlow VERSION: 0.0.1
*/

package com.lifeflow.coinsflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lifeflow.coinsflow.ui.theme.CoinsFlowTheme
import com.lifeflow.coinsflow.ui.view.ExpensesScreen
import com.lifeflow.coinsflow.ui.view.HomeScreen
import com.lifeflow.coinsflow.ui.view.IncomesScreen
import com.lifeflow.coinsflow.ui.view.ProductScreen
import com.lifeflow.coinsflow.ui.view.ProductsScreen
import com.lifeflow.coinsflow.ui.view.ProfileScreen
import com.lifeflow.coinsflow.ui.view.RoutesScreen
import com.lifeflow.coinsflow.ui.view.StatisticsScreen
import com.lifeflow.coinsflow.viewModel.FireViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoinsFlowTheme {
                Surface{
                    //IncomesScreen()
                    //HomeScreen(db)
                    //ExpensesScreen()
                    //ProfileScreen()
                    //StatisticsScreen()
                    //CheckScreen()
                    MainScreen()
                    //RoutesScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val vm: FireViewModel = hiltViewModel()

    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()

    // Determine the current screen based on the route
    /*val currentScreen = when (currentRoute) {
        NavRoutes.Home.route -> NavRoutes.Home.title
        NavRoutes.Contacts.route -> NavRoutes.Contacts.title
        NavRoutes.About.route -> NavRoutes.About.title
        NavRoutes.Expenses.route -> NavRoutes.Expenses.title
        NavRoutes.Profile.route -> NavRoutes.Profile.title
        NavRoutes.Statistics.route -> NavRoutes.Statistics.title
        NavRoutes.Check.route -> NavRoutes.Check.title
        NavRoutes.Incomes.route -> NavRoutes.Incomes.title
        NavRoutes.Routes.route -> NavRoutes.Routes.title
        else -> NavRoutes.Home.title // Default case if route doesn't match any
    }*/
    val currentScreen = NavRoutes.valueOf(
        backStackEntry?.destination?.route ?: NavRoutes.Transactions.name
    )

    Scaffold(
        topBar = {
            //val canNavigateBack = navController.previousBackStackEntry != null
            if (currentScreen.name !in listOf(
                    "Transactions",
                    "Profile",
                    "Statistics",
                )
            ) {
                FlowAppBar(
                    stringResource(currentScreen.route),
                    navigateUp = { navController.navigateUp() }
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = NavRoutes.Transactions.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Transactions.name) {
                HomeScreen(
                    vm,
                    navOnExpenseScreen = {
                        navController.navigate(NavRoutes.Expenses.name)
                    },
                    navOnIncomeScreen = {
                        navController.navigate(NavRoutes.Incomes.name)
                    },
                    navOnRoutes = {
                        navController.navigate(NavRoutes.Routes.name)
                    },
                )
            }
            composable(NavRoutes.Statistics.name) { StatisticsScreen() }
            composable(NavRoutes.Profile.name) { ProfileScreen() }
            composable(NavRoutes.Expenses.name) {
                ExpensesScreen(
                    backUp = { navController.popBackStack() },
                    vm
                )
            }
            composable(NavRoutes.Incomes.name) {
                IncomesScreen(
                    backUp = { navController.popBackStack() },
                    vm
                )
            }
            composable(NavRoutes.Routes.name) { RoutesScreen() }
            composable(NavRoutes.Products.name) { ProductsScreen() }
            composable(NavRoutes.Product.name) { ProductScreen() }
            composable(NavRoutes.Check.name) {}
            composable(NavRoutes.Categories.name) {}

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowAppBar(
    currentScreen: String,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(currentScreen) },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = ImageVector
                        .vectorResource(R.drawable.baseline_arrow_back_ios_new_24),
                    contentDescription = ""
                )
            }
        }
    )
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        NavBarItems.BarItems.forEach { navItem ->
            NavigationBarItem(
                selected = currentRoute == navItem.route,
                onClick = {
                    navController.navigate(navItem.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(navItem.image),
                        contentDescription = navItem.title
                    )
                },
                label = {
                    Text(text = navItem.title)
                }
            )
        }
    }
}

object NavBarItems {
    val BarItems = listOf(
        BarItem(
            title = "Transactions",
            image = R.drawable.baseline_compare_arrows_24,
            route = "Transactions"
        ),
        BarItem(
            title = "Statistics",
            image = R.drawable.baseline_bar_chart_24,
            route = "Statistics"
        ),
        BarItem(
            title = "Profile",
            image = R.drawable.baseline_credit_card_24,
            route = "Profile"
        )
    )
}

data class BarItem(
    val title: String,
    val image: Int,
    val route: String
)

/*sealed class NavRoutes(val route: String, val title: String) {
    data object Home : NavRoutes("home", "Home")
    data object Contacts : NavRoutes("contacts", "Contacts")
    data object About : NavRoutes("about", "About")
    data object Expenses : NavRoutes("expenses", "Expenses")
    data object Profile : NavRoutes("profile", "Profile")
    data object Statistics : NavRoutes("statistics", "Statistics")
    data object Check : NavRoutes("check", "Check")
    data object Incomes : NavRoutes("incomes", "Incomes")
    data object Routes : NavRoutes("routes", "Routes")
}*/

enum class NavRoutes(@StringRes val route: Int) {
    Transactions(route = R.string.app_name),
    Statistics(route = R.string.statistics),
    Profile(route = R.string.profile),
    Expenses(route = R.string.expenses),
    Check(route = R.string.check),
    Incomes(route = R.string.incomes),
    Routes(route = R.string.routes),
    Products(route = R.string.products),
    Product(route = R.string.product),
    Categories(route = R.string.categories),
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CoinsFlowTheme {
        RoutesScreen()
    }
}