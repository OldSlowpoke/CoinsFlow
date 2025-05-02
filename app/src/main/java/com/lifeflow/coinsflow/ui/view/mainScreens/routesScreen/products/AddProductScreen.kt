package com.lifeflow.coinsflow.ui.view.mainScreens.routesScreen.products

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeflow.coinsflow.model.Product
import com.lifeflow.coinsflow.viewModel.FireViewModel

@Composable
fun AddProductScreen(
    vm: FireViewModel,
    backUp: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var id: String

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Название") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 16.dp),
            onClick = {
                id = vm.getLinkOnFirePath("products")
                vm.addProduct(
                    Product(
                        name = name,
                        description = description,
                        id = id
                    ),
                    path = id
                )
                backUp()
            },
            enabled = name.isNotBlank()
        ) {
            Text("Сохранить")
        }
    }
}