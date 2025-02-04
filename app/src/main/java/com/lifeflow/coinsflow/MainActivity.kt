/*
CoinsFlow VERSION: 0.0.1
*/

package com.lifeflow.coinsflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lifeflow.coinsflow.ui.theme.CoinsFlowTheme
import com.lifeflow.coinsflow.ui.view.ExpensesScreen
import com.lifeflow.coinsflow.ui.view.HomeScreen
import com.lifeflow.coinsflow.ui.view.IncomesScreen
import com.lifeflow.coinsflow.ui.view.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoinsFlowTheme {
                //IncomesScreen()
                //HomeScreen()
                //ExpensesScreen()
                ProfileScreen()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CoinsFlowTheme {
        IncomesScreen()
    }
}