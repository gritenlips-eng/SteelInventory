package com.example.steelinventory.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productType: String,   // نبشی / ناودانی
    val size: String,
    val declaredWeight: Double,
    val factoryName: String,
    val bundleCount: Int,
    val weightPerPiece: Double,
    val bundleWeight: Double,  // محاسباتی
    val receiptDate: String
)

@Entity(tableName = "channel_specs")
data class ChannelSpec(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val size: String,
    val declaredWeight: Double,
    val factoryName: String,
    val wingWidth: Double,   // b
    val webThickness: Double // s
)
