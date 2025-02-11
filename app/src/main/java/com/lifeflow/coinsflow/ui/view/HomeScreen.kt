package com.lifeflow.coinsflow.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeflow.coinsflow.model.Account
import com.lifeflow.coinsflow.model.Transactions
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.lifeflow.coinsflow.ui.theme.GrayDark
import com.lifeflow.coinsflow.ui.theme.GrayLight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.lifeflow.coinsflow.viewModel.FireViewModel

@Composable
fun HomeScreen(
    nv: NavHostController,
    mv: FireViewModel,
    onButtonClick: () -> Unit,
) {
    val transactions by mv.transactions.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MainBar(nv, onButtonClick)
        Transactions(transactions, mv)
        //Incom()
    }
}

@Composable
fun Incom() {
    TODO("Not yet implemented")
}

@Composable
fun TransactionItem(transaction: Transactions, mv: FireViewModel) {
    var value by rememberSaveable { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp, end = 5.dp, top = 1.dp, bottom = 5.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(GrayLight)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(15.dp)
            ) {
                Text(
                    text = transaction.type,
                    modifier = Modifier.weight(4f),
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = transaction.total.toString(),
                    modifier = Modifier.weight(2f)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { value = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            DropdownMenu(
                expanded = value,
                onDismissRequest = { value = false },
                offset = DpOffset(x = 270.dp, y = 5.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Редактировать") },
                    onClick = {
                        value = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Удалить") },
                    onClick = {
                        mv.deleteTransactions(transaction)
                        value = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Transactions(expens: List<Transactions>, mv: FireViewModel) {
    val groups = expens.groupBy { it.date }
    LazyColumn {
        groups.forEach { (date, incomes) ->
            stickyHeader {
                Text(
                    text = date,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 10.dp, start = 5.dp, end = 5.dp, bottom = 3.dp)
                        .background(GrayDark, RoundedCornerShape(20.dp))
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            items(incomes.size) { index ->
                TransactionItem(transaction = incomes[index], mv)
            }
        }
    }
}

@Composable
fun MainBar(nv: NavHostController, onButtonClick: () -> Unit) {
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
                        text = "Баланс (Руб):",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "100 000",
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
                    onClick = {
                        onButtonClick()
                    },
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
