/*
CoinsFlow VERSION: 0.0.1
*/

package com.lifeflow.coinsflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.lifeflow.coinsflow.ui.view.ProfileScreen
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
                //IncomesScreen()
                //HomeScreen(db)
                //ExpensesScreen()
                //ProfileScreen()
                //StatisticsScreen()
                //CheckScreen()
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val vm: FireViewModel = hiltViewModel()
    Column(Modifier.padding(8.dp)) {
        NavHost(
            navController,
            startDestination = NavRoutes.Home.route,
            modifier = Modifier.weight(1f)
        ) {
            composable(NavRoutes.Home.route) {
                HomeScreen(
                    navController,
                    vm,
                    onButtonClick = {
                        navController.navigate(NavRoutes.Expenses.route)
                    }
                )
            }
            composable(NavRoutes.Contacts.route) { StatisticsScreen() }
            composable(NavRoutes.About.route) { ProfileScreen() }
            composable(NavRoutes.Expenses.route) { ExpensesScreen(navController, vm) }
        }
        BottomNavigationBar(navController = navController)
    }
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
                        imageVector = navItem.image,
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
            title = "Home",
            image = Icons.Filled.Home,
            route = "home"
        ),
        BarItem(
            title = "Contacts",
            image = Icons.Filled.Face,
            route = "contacts"
        ),
        BarItem(
            title = "About",
            image = Icons.Filled.Info,
            route = "about"
        )
    )
}

data class BarItem(
    val title: String,
    val image: ImageVector,
    val route: String
)

sealed class NavRoutes(val route: String) {
    data object Home : NavRoutes("home")
    data object Contacts : NavRoutes("contacts")
    data object About : NavRoutes("about")
    data object Expenses : NavRoutes("expenses")
    data object Profile : NavRoutes("profile")
    data object Statistics : NavRoutes("statistics")
    data object Check : NavRoutes("check")
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CoinsFlowTheme {
        IncomesScreen()
    }
}