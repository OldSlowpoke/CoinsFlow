package com.lifeflow.coinsflow.ui.view.profileScreens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeflow.coinsflow.model.Account
import com.lifeflow.coinsflow.viewModel.FireViewModel

@Composable
fun ProfileScreen(
    vm: FireViewModel,
    navAddAccountScreen: () -> Unit,
    navOnLogout: () -> Unit
) {
    val accounts by vm.accounts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                vm.logout()
                navOnLogout()
            }
        ) {
            Text("Logout")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navAddAccountScreen() }
        ) {
            Text("Добавить категорию транзакции")
        }
        accounts.forEachIndexed { index, account ->
            AccountItem(
                account = account,
                vm = vm,
            )
            if (index < accounts.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    thickness = 1.dp
                )
            }
        }
    }
}

@Composable
fun AccountItem(
    account: Account,
    vm: FireViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = account.accountName,
            fontSize = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = account.description,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(6f)
            )
            Text(
                text = account.initialAmount.toString(),
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(6f)
            )
            IconButton(
                onClick = { vm.deleteAccount(account) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Удалить категорию")
            }
        }
    }
}