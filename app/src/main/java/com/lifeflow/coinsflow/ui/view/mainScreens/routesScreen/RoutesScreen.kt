package com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.lifeflow.coinsflow.R

@Composable
fun RoutesScreen(
    navOnProductsScreen: () -> Unit,
    navOnExpenseCategoriesScreen: () -> Unit,
    navOnIncomeCategoriesScreen: () -> Unit,
    navOnMarketsScreen: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navOnExpenseCategoriesScreen()
                }
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Категории расходов",
                    modifier = Modifier.padding(8.dp)
                )
                Icon(
                    imageVector = ImageVector
                        .vectorResource(R.drawable.baseline_keyboard_arrow_right_24),
                    contentDescription = null
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navOnIncomeCategoriesScreen()
                }
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Категории доходов",
                    modifier = Modifier.padding(8.dp)
                )
                Icon(
                    imageVector = ImageVector
                        .vectorResource(R.drawable.baseline_keyboard_arrow_right_24),
                    contentDescription = null
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navOnProductsScreen()
                }
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Товары",
                    modifier = Modifier.padding(8.dp)
                )
                Icon(
                    imageVector = ImageVector
                        .vectorResource(R.drawable.baseline_keyboard_arrow_right_24),
                    contentDescription = null
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navOnMarketsScreen()
                }
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Магазины",
                    modifier = Modifier.padding(8.dp)
                )
                Icon(
                    imageVector = ImageVector
                        .vectorResource(R.drawable.baseline_keyboard_arrow_right_24),
                    contentDescription = null
                )
            }
        }
    }
}