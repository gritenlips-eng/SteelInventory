package com.example.steelinventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.steelinventory.data.AppDatabase
import com.example.steelinventory.ui.screens.*
import com.example.steelinventory.ui.theme.SteelInventoryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(applicationContext)
        setContent {
            SteelInventoryTheme {
                val nav = rememberNavController()
                NavHost(nav, startDestination = "home") {
                    composable("home") { HomeScreen(nav) }
                    composable("add") { AddItemScreen(db.inventoryDao()) { nav.popBackStack() } }
                    composable("inventory") { InventoryScreen(db.inventoryDao(), nav) }
                    composable("factory_products") { FactoryProductsScreen(nav) }
                    composable("product_report") { ProductReportScreen(db.inventoryDao()) }
                    composable("channel_spec") { ChannelSpecScreen(db.channelSpecDao()) }
                    composable("settings") {
                        SettingsScreen(onBack = { nav.popBackStack() })
                    }
                }
            }
        }
    }
}
