package com.example.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wheel_items",
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
data class WheelItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wheelId: Long,
    val name: String,
    val isEnabled: Boolean = true,
    val weight: Int = 1,
    val colorHex: Long? = null,
    val orderIndex: Int = 0
)
