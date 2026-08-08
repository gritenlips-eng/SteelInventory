package com.example.steelinventory.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Insert
    suspend fun insertItem(item: InventoryItem)

    @Insert
    suspend fun insertAll(items: List<InventoryItem>)

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Query("DELETE FROM inventory_items")
    suspend fun deleteAllItems()

    @Query("SELECT * FROM inventory_items ORDER BY receiptDate DESC")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items ORDER BY receiptDate DESC")
    suspend fun getAllItemsOnce(): List<InventoryItem>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    fun getItemById(id: Long): Flow<InventoryItem?>


    @Query(
        """
        SELECT * FROM inventory_items
        WHERE productType = :type
          AND size = :size
          AND factoryName = :factory
          AND ABS(declaredWeight - :dw) < 0.001
        ORDER BY receiptDate DESC
        """
    )
    fun getItemsByHierarchy(
        type: String,
        size: String,
        dw: Double,
        factory: String
    ): Flow<List<InventoryItem>>

    @Query("SELECT DISTINCT factoryName FROM inventory_items ORDER BY factoryName ASC")
    fun getFactories(): Flow<List<String>>

    @Query("SELECT * FROM inventory_items WHERE factoryName = :factory ORDER BY productType ASC, size ASC")
    fun getItemsByFactory(factory: String): Flow<List<InventoryItem>>
}

@Dao
interface ChannelSpecDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(spec: ChannelSpec)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(specs: List<ChannelSpec>)

    @Delete
    suspend fun delete(spec: ChannelSpec)

    @Query("DELETE FROM channel_specs")
    suspend fun deleteAll()

    @Query("SELECT * FROM channel_specs ORDER BY size ASC")
    fun getAll(): Flow<List<ChannelSpec>>

    @Query("SELECT * FROM channel_specs ORDER BY size ASC")
    suspend fun getAllOnce(): List<ChannelSpec>

    @Query("SELECT * FROM channel_specs WHERE size = :size AND ABS(declaredWeight - :dw) < 0.001")
    fun getByProduct(size: String, dw: Double): Flow<List<ChannelSpec>>
}
