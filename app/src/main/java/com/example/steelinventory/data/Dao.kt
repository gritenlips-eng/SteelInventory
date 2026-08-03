package com.example.steelinventory.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Insert suspend fun insertItem(item: InventoryItem)
    @Query("SELECT * FROM inventory_items ORDER BY receiptDate DESC")
    fun getAllItems(): Flow<List<InventoryItem>>
    @Query("SELECT * FROM inventory_items WHERE productType = :type AND size = :size AND declaredWeight = :dw AND factoryName = :factory")
    fun getItemsByHierarchy(type: String, size: String, dw: Double, factory: String): Flow<List<InventoryItem>>
    @Query("SELECT DISTINCT factoryName FROM inventory_items")
    fun getFactories(): Flow<List<String>>
    @Query("SELECT * FROM inventory_items WHERE factoryName = :factory")
    fun getItemsByFactory(factory: String): Flow<List<InventoryItem>>
}

@Dao
interface ChannelSpecDao {
    @Insert suspend fun insert(spec: ChannelSpec)
    @Query("SELECT * FROM channel_specs")
    fun getAll(): Flow<List<ChannelSpec>>
    @Query("SELECT * FROM channel_specs WHERE size = :size AND declaredWeight = :dw")
    fun getByProduct(size: String, dw: Double): Flow<List<ChannelSpec>>
}
