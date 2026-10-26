package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.SpinHistoryEntity
import com.example.model.WheelEntity
import com.example.model.WheelItemEntity
import com.example.model.WinnerStat
import kotlinx.coroutines.flow.Flow

@Dao
interface WheelDao {
    @Query("SELECT * FROM wheels ORDER BY isPreset DESC, createdAt DESC")
    fun getAllWheels(): Flow<List<WheelEntity>>

    @Query("SELECT * FROM wheels WHERE id = :wheelId LIMIT 1")
    fun getWheelById(wheelId: Long): Flow<WheelEntity?>

    @Query("SELECT * FROM wheels WHERE id = :wheelId LIMIT 1")
    suspend fun getWheelByIdSync(wheelId: Long): WheelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWheel(wheel: WheelEntity): Long

    @Update
    suspend fun updateWheel(wheel: WheelEntity)

    @Delete
    suspend fun deleteWheel(wheel: WheelEntity)

    @Query("SELECT * FROM wheel_items WHERE wheelId = :wheelId ORDER BY orderIndex ASC, id ASC")
    fun getItemsForWheel(wheelId: Long): Flow<List<WheelItemEntity>>

    @Query("SELECT * FROM wheel_items WHERE wheelId = :wheelId ORDER BY orderIndex ASC, id ASC")
    suspend fun getItemsForWheelSync(wheelId: Long): List<WheelItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WheelItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<WheelItemEntity>)

    @Update
    suspend fun updateItem(item: WheelItemEntity)

    @Delete
    suspend fun deleteItem(item: WheelItemEntity)

    @Query("DELETE FROM wheel_items WHERE wheelId = :wheelId")
    suspend fun clearItemsForWheel(wheelId: Long)

    @Query("UPDATE wheel_items SET isEnabled = :isEnabled WHERE wheelId = :wheelId")
    suspend fun toggleAllItems(wheelId: Long, isEnabled: Boolean)

    @Query("SELECT * FROM spin_history WHERE wheelId = :wheelId ORDER BY timestamp DESC")
    fun getHistoryForWheel(wheelId: Long): Flow<List<SpinHistoryEntity>>

    @Query("SELECT winnerName, COUNT(*) as winCount FROM spin_history WHERE wheelId = :wheelId GROUP BY winnerName ORDER BY winCount DESC")
    fun getWinnerStats(wheelId: Long): Flow<List<WinnerStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: SpinHistoryEntity): Long

    @Query("DELETE FROM spin_history WHERE wheelId = :wheelId")
    suspend fun clearHistoryForWheel(wheelId: Long)
}
