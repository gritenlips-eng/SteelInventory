package com.example.steelinventory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.steelinventory.data.InventoryDao

@Composable
fun ProductReportScreen(dao: InventoryDao) {
    val allItems by dao.getAllItems().collectAsState(initial = emptyList())
    val grouped = allItems.groupBy { Triple(it.productType, it.size, it.declaredWeight) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("گزارش کالایی", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            grouped.forEach { (key, items) ->
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${key.first} | سایز: ${key.second} | وزن: ${key.third}kg",
                                style = MaterialTheme.typography.titleMedium)
                            val byFactory = items.groupBy { it.factoryName }
                            byFactory.forEach { (factory, fItems) ->
                                Text("  کارخانه: $factory")
                                fItems.forEach { Text("    بندیل: ${it.bundleWeight}kg | تاریخ: ${it.receiptDate}") }
                            }
                        }
                    }
                }
            }
        }
    }
}
