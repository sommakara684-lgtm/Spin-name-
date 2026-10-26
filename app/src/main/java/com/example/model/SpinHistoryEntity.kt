package com.example.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "spin_history",
    foreignKeys = [
        ForeignKey(
            entity = WheelEntity::class,
            parentColumns = ["id"],
            childColumns = ["wheelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["wheelId"])]
)
data class SpinHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wheelId: Long,
    val winnerName: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class WinnerStat(
    val winnerName: String,
    val winCount: Int
)
