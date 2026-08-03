package com.example.steelinventory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.steelinventory.data.InventoryDao
import com.example.steelinventory.util.kg
import com.example.steelinventory.util.kgRange

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
                        Column(
                            modifier = Modifier.padding(12.dp),
