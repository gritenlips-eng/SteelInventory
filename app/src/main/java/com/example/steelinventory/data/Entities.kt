package com.example.steelinventory.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_items",
    indices = [
        // ایندکس غیریکتا: فقط برای سرعت جستجو
        // unique = true حذف شد تا چند رسید با مشخصات یکسان بتوانند هم‌زمان ثبت شوند
        Index(value = ["factoryName", "productType", "size"])
    ]
)
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productType: String,
    val size: String,
    val declaredWeight: Double,
    val factoryName: String,
    val bundleCount: Int,
    val weightPerPieceMin: Double,
    val weightPerPieceMax: Double,
    val bundleWeight: Double,
    val receiptDate: String
)

@Entity(tableName = "channel_specs")
data class ChannelSpec(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val size: String,
    val declaredWeight: Double,
    val factoryName: String,
    val wingWidth: Double,
    val webThickness: Double
)
