package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wheels")
data class WheelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val themePalette: String = WheelPaletteTheme.RAINBOW.name,
    val spinDurationSeconds: Float = 4.5f,
    val autoRemoveWinner: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val isPreset: Boolean = false
)
