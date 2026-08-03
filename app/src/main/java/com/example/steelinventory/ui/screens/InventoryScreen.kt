package com.example.steelinventory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.steelinventory.data.InventoryDao
import com.example.steelinventory.data.InventoryItem
import com.example.steelinventory.util.kg
import com.example.steelinventory.util.kgRange

@Composable
fun InventoryScreen(dao: InventoryDao, nav: NavController) {
    val items by dao.getAllItems().collectAsState(initial = emptyList())
    val grouped = items.groupBy { it.productType }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("موجودی انبار", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            grouped.forEach { (type, typeItems) ->
                item { Text(type, style = MaterialTheme.typography.titleMedium) }
                val bySizeWeight = typeItems.groupBy { "${it.size} - ${kg(it.declaredWeight)}" }
                bySizeWeight.forEach { (sizeWeight, swItems) ->
                    item { Text("  $sizeWeight", style = MaterialTheme.typography.bodyLarge) }
                    val byFactory = swItems.groupBy { it.factoryName }
                    byFactory.forEach { (factory, fItems) ->
                        item { Text("    $factory", style = MaterialTheme.typography.bodyMedium) }
                        items(fItems) { item -> ItemRow(item) }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemRow(item: InventoryItem) {
    Card(modifier = Modifier.fillMaxWidth().padding(start = 16.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                "بندیل: ${item.bundleCount} شاخه | " +
                    "وزن شاخه: ${kgRange(item.weightPerPieceMin, item.weightPerPieceMax)}"
            )
            Text("وزن بندیل: ${kg(item.bundleWeight)} | تاریخ: ${item.receiptDate}")
        }
    }
}
