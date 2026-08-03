package com.example.steelinventory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.steelinventory.data.InventoryDao

@Composable
fun FactoryProductsScreen(dao: InventoryDao) {
    val allItems by dao.getAllItems().collectAsState(initial = emptyList())
    val factories = allItems.groupBy { it.factoryName }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("محصولات کارخانجات", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            factories.forEach { (factory, items) ->
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(factory, style = MaterialTheme.typography.titleMedium)
                            val products = items.groupBy { "${it.productType} ${it.size} ${it.declaredWeight}kg" }
                            products.forEach { (product, pItems) ->
                                val min = pItems.minOf { it.weightPerPiece }
                                val max = pItems.maxOf { it.weightPerPiece }
                                Text("  $product → min: ${min}kg | max: ${max}kg")
                            }
                        }
                    }
                }
            }
        }
    }
}
