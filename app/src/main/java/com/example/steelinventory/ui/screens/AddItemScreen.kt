package com.example.steelinventory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.steelinventory.data.InventoryDao
import com.example.steelinventory.data.InventoryItem
import com.example.steelinventory.util.kg
import com.example.steelinventory.util.round2
import kotlinx.coroutines.launch
import com.example.steelinventory.util.todayJalali

@Composable
fun AddItemScreen(dao: InventoryDao, onSaved: () -> Unit) {
    val scope = rememberCoroutineScope()

    var productType by remember { mutableStateOf("نبشی") }
    var size by remember { mutableStateOf("") }
    var declaredWeight by remember { mutableStateOf("") }
    var factoryName by remember { mutableStateOf("") }
    var bundleCount by remember { mutableStateOf("") }
    var weightMin by remember { mutableStateOf("") }
    var weightMax by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val minW = weightMin.toDoubleOrNull()
    val maxW = weightMax.toDoubleOrNull()
    val count = bundleCount.toIntOrNull()

    val avgRaw =
        if (minW != null && maxW != null) (minW + maxW) / 2 else null
    val avgWeight = avgRaw?.let { round2(it) }
    val bundleWeight =
        if (avgRaw != null && count != null) round2(count * avgRaw) else null


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ثبت کالای جدید", style = MaterialTheme.typography.titleLarge)

        Text("نوع کالا")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("نبشی", "ناودانی").forEach { type ->
                FilterChip(
                    selected = productType == type,
                    onClick = { productType = type },
                    label = { Text(type) }
                )
            }
        }

        OutlinedTextField(
            value = size,
            onValueChange = { size = it },
            label = { Text("سایز") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = declaredWeight,
            onValueChange = { declaredWeight = it },
            label = { Text("وزن اعلامی در فروش (ک)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = factoryName,
            onValueChange = { factoryName = it },
            label = { Text("نام کارخانه") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bundleCount,
            onValueChange = { bundleCount = it },
            label = { Text("تعداد شاخه در بندیل") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Text("وزن هر شاخه (ک)")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = weightMin,
                onValueChange = { weightMin = it },
                label = { Text("از وزن") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = weightMax,
                onValueChange = { weightMax = it },
                label = { Text("تا وزن") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("میانگین وزن هر شاخه: " + (avgWeight?.let { kg(it) } ?: "-"))
                Text("وزن بندیل: " + (bundleWeight?.let { kg(it) } ?: "-"))
            }
        }

        Button(
            onClick = {
                val dw = declaredWeight.toDoubleOrNull()
                if (size.isBlank() || factoryName.isBlank() ||
                    dw == null || count == null || minW == null ||
                    maxW == null || bundleWeight == null
                ) {
                    message = "همه فیلدها را درست پر کنید"
                    return@Button
                }
                if (minW > maxW) {
                    message = "وزن ابتدای بازه نباید بیشتر از انتهای بازه باشد"
                    return@Button
                }
                scope.launch {
                    dao.insertItem(
                        InventoryItem(
                            productType = productType,
                            size = size.trim(),
                            declaredWeight = round2(dw),
                            factoryName = factoryName.trim(),
                            bundleCount = count,
                            weightPerPieceMin = round2(minW),
                            weightPerPieceMax = round2(maxW),
                            bundleWeight = bundleWeight,
                            receiptDate = todayJalali()

                        )
                    )
                    message = "ذخیره شد"
                    size = ""; declaredWeight = ""; factoryName = ""
                    bundleCount = ""; weightMin = ""; weightMax = ""
                    onSaved()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ذخیره")
        }

        if (message.isNotBlank()) {
            Text(message, color = MaterialTheme.colorScheme.primary)
        }
    }
}
