package com.example.steelinventory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(nav: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("مدیریت انبار فولاد", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        listOf(
            "ثبت کالای جدید" to "add",
            "موجودی انبار" to "inventory",
            "محصولات کارخانجات" to "factory_products",
            "گزارش کالایی" to "product_report",
            "مشخصات فنی ناودانی" to "channel_spec"
        ).forEach { (label, route) ->
            Button(onClick = { nav.navigate(route) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(label)
            }
        }
    }
}
