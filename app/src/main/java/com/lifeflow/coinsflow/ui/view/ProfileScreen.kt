package com.lifeflow.coinsflow.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeflow.coinsflow.model.Accounts
import com.lifeflow.coinsflow.model.Assets
import com.lifeflow.coinsflow.ui.theme.GrayLight

@Composable
fun ProfileScreen() {
    val apartmentAccounts = listOf(
        Accounts(accountName = "Счет 1", initialAmount = 1000.0),
        Accounts(accountName = "Счет 2", initialAmount = 2000.0)
    )

    val bankAccounts = listOf(
        Accounts(accountName = "Счет 1", initialAmount = 3000.0),
        Accounts(accountName = "Счет 2", initialAmount = 4000.0),
        Accounts(accountName = "Счет 3", initialAmount = 5000.0)
    )

    val carAccounts = listOf(
        Accounts(accountName = "Счет 1", initialAmount = 6000.0)
    )

    // Создаем список активов
    val assets = listOf(
        Assets(assetType = "Квартира", accounts = apartmentAccounts),
        Assets(assetType = "Банк", accounts = bankAccounts),
        Assets(assetType = "Автомобиль", accounts = carAccounts)
    )
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ProfileBar()
        AssetsList(assets)
        //Incom()
    }
}

@Composable
fun ProfileBar() {
    Card(
        modifier = Modifier
            .height(300.dp)
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 5.dp),
        colors = CardDefaults.cardColors(GrayLight),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GrayLight)
                    .weight(4f),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    Text(
                        text = "Профиль:",
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Привет, Егор!",
                        fontSize = 50.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(2f)
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.weight(2f)
                ) {
                    Icon(
                        Icons.Filled.Build,
                        contentDescription = "Edit"
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { },
                    modifier = Modifier.weight(2f)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Edit"
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { },
                    modifier = Modifier.weight(2f)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Edit"
                    )
                }
            }
        }
    }
}


@Composable
fun AssetsList(assets: List<Assets>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(assets) { asset ->
            AssetItem(asset = asset)
        }
    }
}

@Composable
fun AssetItem(asset: Assets) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Актив: ${asset.assetType}",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        asset.accounts.forEach { account ->
            AccountItem(account = account)
        }
    }
}

@Composable
fun AccountItem(account: Accounts) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "Счет: ${account.accountName}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Сумма: ${account.initialAmount}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}