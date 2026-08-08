package com.example.steelinventory.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.steelinventory.data.AppDatabase
import com.example.steelinventory.data.InventoryItem
import com.example.steelinventory.util.kg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactoryProductsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = remember { db.inventoryDao() }

    // Flow را مستقیم به State تبدیل می‌کنیم؛ با هر تغییر دیتابیس خودکار به‌روز می‌شود
    val allItems by dao.getAllItems().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedProductType by remember { mutableStateOf("همه") }
    var expandedFactories by remember { mutableStateOf(setOf<String>()) }

    val productTypes = remember(allItems) {
        listOf("همه") + allItems.map { it.productType }.distinct().sorted()
    }

    val filteredItems = remember(allItems, searchQuery, selectedProductType) {
        allItems.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.factoryName.contains(searchQuery, ignoreCase = true) ||
                    item.productType.contains(searchQuery, ignoreCase = true) ||
                    item.size.contains(searchQuery, ignoreCase = true)

            val matchesType = selectedProductType == "همه" || item.productType == selectedProductType

            matchesSearch && matchesType
        }
    }

    val groupedByFactory = remember(filteredItems) {
        filteredItems
            .groupBy { it.factoryName }
            .toSortedMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("محصولات به تفکیک کارخانه") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("جستجو...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            ScrollableTabRow(
                // اگر نوع انتخابی در لیست نبود، به تب اول برمی‌گردیم تا کرش نکند
                selectedTabIndex = productTypes.indexOf(selectedProductType).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 0.dp
            ) {
                productTypes.forEach { type ->
                    Tab(
                        selected = selectedProductType == type,
                        onClick = { selectedProductType = type },
                        text = { Text(type) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (groupedByFactory.isEmpty()) {
                Text(
                    "هیچ محصولی یافت نشد",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedByFactory.forEach { (factoryName, items) ->
                        item(key = factoryName) {
                            FactorySection(
                                factoryName = factoryName,
                                items = items,
                                isExpanded = expandedFactories.contains(factoryName),
                                onToggle = {
                                    expandedFactories =
                                        if (expandedFactories.contains(factoryName)) {
                                            expandedFactories - factoryName
                                        } else {
                                            expandedFactories + factoryName
                                        }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FactorySection(
    factoryName: String,
    items: List<InventoryItem>,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = factoryName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${items.size} قلم محصول",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "بستن" else "باز کردن"
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider()
                    items.forEachIndexed { index, item ->
                        ProductRow(item)
                        if (index < items.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductRow(item: InventoryItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${item.productType} - ${item.size}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "تعداد بندیل: ${item.bundleCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "وزن بندیل: ${kg(item.bundleWeight)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "وزن اعلامی:",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = kg(item.declaredWeight),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
