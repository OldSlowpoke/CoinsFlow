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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lifeflow.coinsflow.ui.theme.CoinsFlowTheme
import com.lifeflow.coinsflow.ui.view.CheckScreen
import com.lifeflow.coinsflow.ui.view.ExpensesScreen
import com.lifeflow.coinsflow.ui.view.HomeScreen
import com.lifeflow.coinsflow.ui.view.IncomesScreen
import com.lifeflow.coinsflow.ui.view.ProfileScreen
import com.lifeflow.coinsflow.ui.view.StatisticsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Firebase.firestore
        enableEdgeToEdge()
        setContent {
            CoinsFlowTheme {
                //IncomesScreen()
                HomeScreen(db)
                //ExpensesScreen()
                //ProfileScreen()
                //StatisticsScreen()
                //CheckScreen()
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