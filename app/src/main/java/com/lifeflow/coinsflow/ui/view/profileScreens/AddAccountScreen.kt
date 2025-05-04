package com.lifeflow.coinsflow.ui.view.profileScreens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lifeflow.coinsflow.model.Account
import com.lifeflow.coinsflow.viewModel.FireViewModel


@Composable
fun AddAccountScreen(
    vm: FireViewModel,
    backUp: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var id: String
    var total by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = name,
            placeholder = { Text("Введите название счета") },
            onValueChange = { name = it },
            label = { Text("Название") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = description,
            placeholder = { Text("Введите описание") },
            onValueChange = { description = it },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = total,
            placeholder = { Text("Введите баланс счета") },
            onValueChange = { newValue ->
                if (
                    newValue.isBlank() || newValue
                        .matches(
                            "\\d*(\\.\\d{0,2})?"
                                .toRegex()
                        )
                ) {
                    total = newValue
                }
            },
            label = { Text("Сумма") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                id = vm.getLinkOnFirePath("accounts")
                vm.addAccount(
                    Account(
                        accountName = name,
                        description = description,
                        id = id,
                        initialAmount = total.toDouble()
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