/*
CoinsFlow VERSION: 0.0.1
*/

package com.lifeflow.coinsflow

import android.annotation.SuppressLint
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
import com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.categories.expense.AddExpenseCategoryScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.products.AddProductScreen
import com.lifeflow.coinsflow.ui.view.AuthScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.CheckScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.categories.expense.ExpenseCategoriesScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.ExpensesScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.HomeScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.IncomesScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.products.ProductScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.products.ProductsScreen
import com.lifeflow.coinsflow.ui.view.profileScreens.ProfileScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.RoutesScreen
import com.lifeflow.coinsflow.ui.view.statisticsScreens.StatisticsScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.categories.expense.AddSubExpenseCategoryScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.categories.incomes.AddIncomesCategoryScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.categories.incomes.AddSubIncomesCategoriesCategoryScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.categories.incomes.IncomesCategoriesScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.markets.AddMarketsScreen
import com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.markets.MarketsScreen
import com.lifeflow.coinsflow.ui.view.profileScreens.AddAccountScreen
import com.lifeflow.coinsflow.viewModel.FireViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoinsFlowTheme {
                Surface {
                    //IncomesScreen()
                    //HomeScreen(db)
                    //ExpensesScreen()
                    //ProfileScreen()
                    //StatisticsScreen()
                    //CheckScreen()
                    //RoutesScreen()
                    //ProductsScreen()
                    //AddProductScreen()

                    MainScreen()
                }
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
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
                    "Login"
                )
            ) {
                FlowAppBar(
                    stringResource(currentScreen.route),
                    navigateUp = { navController.navigateUp() }
                )
            }
        },
        bottomBar = {
            if (currentScreen.name !in listOf(
                    "Login"
                )
            ) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = if (vm.uiState.value.isAuthenticated) NavRoutes.Transactions.name else NavRoutes.Login.name,
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
            composable(NavRoutes.Statistics.name) { StatisticsScreen(vm) }
            composable(NavRoutes.Profile.name) {
                ProfileScreen(
                    vm,
                    navOnLogout = {
                        navController.navigate(NavRoutes.Login.name)
                    },
                    navAddAccountScreen = {
                        navController.navigate(NavRoutes.AddAccount.name)
                    }
                )
            }
            composable(NavRoutes.Expenses.name) {
                ExpensesScreen(
                    backUp = { navController.popBackStack() },
                    vm,
                )
            }
            composable(NavRoutes.Incomes.name) {
                IncomesScreen(
                    backUp = { navController.popBackStack() },
                    vm
                )
            }
            composable(NavRoutes.Routes.name) {
                RoutesScreen(
                    navOnProductsScreen = {
                        navController.navigate(NavRoutes.Products.name)
                    },
                    navOnExpenseCategoriesScreen = {
                        navController.navigate(NavRoutes.ExpenseCategories.name)
                    },
                    navOnIncomeCategoriesScreen = {
                        navController.navigate(NavRoutes.IncomesCategories.name)
                    },
                    navOnMarketsScreen = {
                        navController.navigate(NavRoutes.Markets.name)
                    }
                )
            }
            composable(NavRoutes.Products.name) {
                ProductsScreen(
                    vm,
                    navAddProductScreen = {
                        navController.navigate(NavRoutes.AddProduct.name)
                    }
                )
            }
            composable(NavRoutes.Product.name) { ProductScreen() }
            composable(NavRoutes.Check.name) {
                CheckScreen(
                    vm,
                )
            }
            composable(NavRoutes.AddProduct.name) {
                AddProductScreen(
                    vm,
                    backUp = { navController.popBackStack() })
            }
            composable(NavRoutes.ExpenseCategories.name) {
                ExpenseCategoriesScreen(
                    vm,
                    navAddCategoriesScreen = {
                        navController.navigate(NavRoutes.AddExpenseCategory.name)
                    },
                    navAddSubCategoriesScreen = {
                        navController.navigate(NavRoutes.AddSubExpenseCategory.name)
                    }
                )
            }
            composable(NavRoutes.AddExpenseCategory.name) {
                AddExpenseCategoryScreen(
                    vm,
                    backUp = { navController.popBackStack() })
            }
            composable(NavRoutes.Login.name) {
                AuthScreen(
                    vm = vm,
                    onNavigateToHome = {
                        navController.navigate(NavRoutes.Transactions.name)
                    }
                )
            }
            composable(NavRoutes.AddSubExpenseCategory.name) {
                AddSubExpenseCategoryScreen(
                    vm,
                    backUp = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.AddAccount.name) {
                AddAccountScreen(
                    vm,
                    backUp = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.Markets.name) {
                MarketsScreen(
                    vm,
                    navAddMarket = {
                        navController.navigate(NavRoutes.AddMarket.name)
                    }
                )
            }
            composable(NavRoutes.AddMarket.name) {
                AddMarketsScreen(
                    vm,
                    backUp = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.IncomesCategories.name) {
                IncomesCategoriesScreen(
                    vm,
                    navAddIncomesCategoriesScreen = {
                        navController.navigate(NavRoutes.AddIncomesCategory.name)
                    },
                    navAddSubIncomesCategoriesScreen = {
                        navController.navigate(NavRoutes.AddSubIncomesCategory.name)
                    }
                )
            }
            composable(NavRoutes.AddIncomesCategory.name) {
                AddIncomesCategoryScreen(
                    vm,
                    backUp = { navController.popBackStack() })
            }
            composable(NavRoutes.AddSubIncomesCategory.name) {
                AddSubIncomesCategoriesCategoryScreen(
                    vm,
                    backUp = { navController.popBackStack() })
            }
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
    IncomesCategories(route = R.string.incomes_categories),
    AddIncomesCategory(route = R.string.add_incomes_categories),
    AddSubIncomesCategory(route = R.string.add_sub_incomes_categories),
    AddProduct(route = R.string.add_product),
    ExpenseCategories(route = R.string.expense_categories),
    AddExpenseCategory(route = R.string.add_expense_categories),
    AddSubExpenseCategory(route = R.string.add_sub_expense_categories),
    AddAccount(route = R.string.add_account),
    Login(route = R.string.login),
    Markets(route = R.string.markets),
    AddMarket(route = R.string.add_market),
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CoinsFlowTheme {

    }
}