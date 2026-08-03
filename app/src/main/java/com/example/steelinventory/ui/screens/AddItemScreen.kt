package com.example.steelinventory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.steelinventory.data.InventoryDao
import com.example.steelinventory.data.InventoryItem
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun AddItemScreen(dao: InventoryDao, nav: NavController) {
    val scope = rememberCoroutineScope()
    var productType by remember { mutableStateOf("نبشی") }
    var size by remember { mutableStateOf("") }
    var declaredWeight by remember { mutableStateOf("") }
    var factoryName by remember { mutableStateOf("") }
    var bundleCount by remember { mutableStateOf("") }
    var weightPerPiece by remember { mutableStateOf("") }

    val productTypes = listOf("نبشی", "ناودانی")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ثبت کالای جدید", style = MaterialTheme.typography.titleLarge)

        Text("نوع کالا:")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            productTypes.forEach { type ->
                FilterChip(selected = productType == type, onClick = { productType = type }, label = { Text(type) })
            }
        }

        listOf(
            "سایز" to size,
            "وزن اعلامی (کیلوگرم)" to declaredWeight,
            "نام کارخانه" to factoryName,
            "تعداد شاخه در بندیل" to bundleCount,
            "وزن هر شاخه (کیلوگرم)" to weightPerPiece
        ).forEachIndexed { i, (label, value) ->
            OutlinedTextField(
                value = value,
                onValueChange = { v ->
                    when (i) {
                        0 -> size = v
                        1 -> declaredWeight = v
                        2 -> factoryName = v
                        3 -> bundleCount = v
                        4 -> weightPerPiece = v
                    }
                },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        val count = bundleCount.toIntOrNull() ?: 0
        val wpp = weightPerPiece.toDoubleOrNull() ?: 0.0
        Text("وزن بندیل (محاسباتی): ${count * wpp} کیلوگرم")

        Button(
            onClick = {
                val dw = declaredWeight.toDoubleOrNull() ?: return@Button
                val bc = bundleCount.toIntOrNull() ?: return@Button
                val wppVal = weightPerPiece.toDoubleOrNull() ?: return@Button
                scope.launch {
                    dao.insertItem(
                        InventoryItem(
                            productType = productType,
                            size = size,
                            declaredWeight = dw,
                            factoryName = factoryName,
                            bundleCount = bc,
                            weightPerPiece = wppVal,
                            bundleWeight = bc * wppVal,
                            receiptDate = LocalDate.now().toString()
                        )
                    )
                    nav.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("ذخیره") }
    }
}
