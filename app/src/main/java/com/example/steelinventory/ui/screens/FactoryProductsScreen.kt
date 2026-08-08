package com.example.steelinventory.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.steelinventory.data.InventoryDao
import com.example.steelinventory.data.InventoryItem
import com.example.steelinventory.util.kg
import com.example.steelinventory.util.num

/**
 * گروه محصول: چند رسید هم‌نوع که فقط برای «نمایش» با هم تجمیع شده‌اند.
 * داده‌های دیتابیس دست‌نخورده باقی می‌مانند.
 */
data class ProductGroup(
    val factoryName: String,
    val productType: String,
    val size: String,
    val declaredWeight: Double,
    val totalBundles: Int,
    val totalWeight: Double,
    val minBranchWeight: Double,
    val maxBranchWeight: Double,
    val rows: List<InventoryItem>
)

/** تبدیل لیست خام رسیدها به لیست گروه‌های تجمیع‌شده */
private fun List<InventoryItem>.toProductGroups(): List<ProductGroup> =
    this.groupBy { listOf(it.factoryName, it.productType, it.size, it.declaredWeight) }
        .map { (_, rows) ->
            ProductGroup(
                factoryName = rows.first().factoryName,
                productType = rows.first().productType,
                size = rows.first().size,
                declaredWeight = rows.first().declaredWeight,
                totalBundles = rows.sumOf { it.bundleCount },
                totalWeight = rows.sumOf { it.bundleWeight },
                // بازه کل قلم: کمینه از فیلد «از» و بیشینه از فیلد «تا»
                minBranchWeight = rows.minOf { it.weightPerPieceMin },
                maxBranchWeight = rows.maxOf { it.weightPerPieceMax },
                rows = rows.sortedByDescending { it.id }
            )
        }
        .sortedWith(compareBy({ it.factoryName }, { it.productType }, { it.size }))

@Composable
fun FactoryProductsScreen(dao: InventoryDao) {

    val items by dao.getAllItems().collectAsState(initial = emptyList())
    val groups = remember(items) { items.toProductGroups() }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {

        Text(
            text = "محصولات کارخانه",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${groups.size} قلم کالا  •  ${items.size}
