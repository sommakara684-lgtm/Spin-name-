package com.example.data

import com.example.model.SpinHistoryEntity
import com.example.model.WheelEntity
import com.example.model.WheelItemEntity
import com.example.model.WinnerStat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class WheelRepository(private val dao: WheelDao) {

    val allWheels: Flow<List<WheelEntity>> = dao.getAllWheels()

    suspend fun ensureInitialData() {
        val existing = dao.getAllWheels().firstOrNull()
        if (existing.isNullOrEmpty()) {
            AppDatabase.populateInitialPresets(dao)
        }
    }

    fun getWheelById(id: Long): Flow<WheelEntity?> = dao.getWheelById(id)

    suspend fun getWheelByIdSync(id: Long): WheelEntity? = dao.getWheelByIdSync(id)

    suspend fun createWheel(wheel: WheelEntity, initialItems: List<String>): Long {
        val wheelId = dao.insertWheel(wheel)
        val items = initialItems.mapIndexed { idx, name ->
            WheelItemEntity(wheelId = wheelId, name = name, orderIndex = idx)
        }
        if (items.isNotEmpty()) {
            dao.insertItems(items)
        }
        return wheelId
    }

    suspend fun updateWheel(wheel: WheelEntity) = dao.updateWheel(wheel)

    suspend fun deleteWheel(wheel: WheelEntity) = dao.deleteWheel(wheel)

    fun getItemsForWheel(wheelId: Long): Flow<List<WheelItemEntity>> =
        dao.getItemsForWheel(wheelId)

    suspend fun addItem(wheelId: Long, name: String, weight: Int = 1): Long {
        val currentItems = dao.getItemsForWheelSync(wheelId)
        val nextOrder = (currentItems.maxOfOrNull { it.orderIndex } ?: -1) + 1
        return dao.insertItem(
            WheelItemEntity(
                wheelId = wheelId,
                name = name.trim(),
                weight = weight,
                orderIndex = nextOrder
            )
        )
    }

    suspend fun addBulkItems(wheelId: Long, rawText: String) {
        val names = rawText
            .split("\n", ",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (names.isEmpty()) return

        val currentItems = dao.getItemsForWheelSync(wheelId)
        var nextOrder = (currentItems.maxOfOrNull { it.orderIndex } ?: -1) + 1

        val entities = names.map { name ->
            WheelItemEntity(
                wheelId = wheelId,
                name = name,
                orderIndex = nextOrder++
            )
        }
        dao.insertItems(entities)
    }

    suspend fun updateItem(item: WheelItemEntity) = dao.updateItem(item)

    suspend fun deleteItem(item: WheelItemEntity) = dao.deleteItem(item)

    suspend fun clearItems(wheelId: Long) = dao.clearItemsForWheel(wheelId)

    suspend fun toggleAllItems(wheelId: Long, isEnabled: Boolean) =
        dao.toggleAllItems(wheelId, isEnabled)

    suspend fun shuffleItems(wheelId: Long) {
        val items = dao.getItemsForWheelSync(wheelId).shuffled()
        items.forEachIndexed { index, item ->
            dao.updateItem(item.copy(orderIndex = index))
        }
    }

    suspend fun sortItemsAlphabetically(wheelId: Long) {
        val items = dao.getItemsForWheelSync(wheelId).sortedBy { it.name.lowercase() }
        items.forEachIndexed { index, item ->
            dao.updateItem(item.copy(orderIndex = index))
        }
    }

    fun getHistoryForWheel(wheelId: Long): Flow<List<SpinHistoryEntity>> =
        dao.getHistoryForWheel(wheelId)

    fun getWinnerStats(wheelId: Long): Flow<List<WinnerStat>> =
        dao.getWinnerStats(wheelId)

    suspend fun recordWinner(wheelId: Long, winnerName: String, autoRemoveWinner: Boolean, winningItemId: Long?) {
        dao.insertHistory(
            SpinHistoryEntity(
                wheelId = wheelId,
                winnerName = winnerName
            )
        )
        if (autoRemoveWinner && winningItemId != null) {
            val item = dao.getItemsForWheelSync(wheelId).firstOrNull { it.id == winningItemId }
            if (item != null) {
                dao.updateItem(item.copy(isEnabled = false))
            }
        }
    }

    suspend fun clearHistory(wheelId: Long) = dao.clearHistoryForWheel(wheelId)
}
