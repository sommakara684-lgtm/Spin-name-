package com.example.model

import androidx.compose.ui.graphics.Color

enum class WheelPaletteTheme(val displayName: String, val hexColors: List<Long>) {
    RAINBOW(
        displayName = "Rainbow",
        hexColors = listOf(
            0xFFEF4444, // Red
            0xFFF97316, // Orange
            0xFFF59E0B, // Amber
            0xFF10B981, // Emerald
            0xFF06B6D4, // Cyan
            0xFF3B82F6, // Blue
            0xFF8B5CF6, // Violet
            0xFFEC4899  // Pink
        )
    ),
    NEON(
        displayName = "Neon Glow",
        hexColors = listOf(
            0xFF00F5D4, // Bright Mint
            0xFF7B2CBF, // Electric Purple
            0xFFF72585, // Hot Pink
            0xFF4CC9F0, // Vivid Cyan
            0xFF7209B7, // Deep Violet
            0xFFFFBE0B, // Golden Yellow
            0xFFFB5607  // Orange Blaze
        )
    ),
    PASTEL(
        displayName = "Pastel Sweet",
        hexColors = listOf(
            0xFFFFB3BA, // Pastel Red
            0xFFFFDFBA, // Pastel Peach
            0xFFFFF5BA, // Pastel Yellow
            0xFFBAFFC9, // Pastel Green
            0xFFBAE1FF, // Pastel Blue
            0xFFD7BAFF, // Pastel Lavender
            0xFFFFC6FF  // Pastel Rose
        )
    ),
    SUNSET(
        displayName = "Sunset Flame",
        hexColors = listOf(
            0xFFE11D48, // Rose Red
            0xFFF43F5E, // Coral
            0xFFFB923C, // Orange
            0xFFFBBF24, // Warm Sun
            0xFFC026D3, // Fuchsia
            0xFF7C3AED  // Purple
        )
    ),
    OCEAN(
        displayName = "Ocean & Mint",
        hexColors = listOf(
            0xFF0D9488, // Teal
            0xFF0284C7, // Sky
            0xFF2563EB, // Royal Blue
            0xFF10B981, // Green
            0xFF06B6D4, // Cyan
            0xFF4F46E5  // Indigo
        )
    ),
    CANDY(
        displayName = "Candy Pop",
        hexColors = listOf(
            0xFFFF6B6B, // Strawberry
            0xFF4ECDC4, // Mint
            0xFFFFE66D, // Banana
            0xFF1A535C, // Deep Forest
            0xFFFF9F1C, // Mango
            0xFF9B5DE5  // Grape
        )
    );

    val composeColors: List<Color> get() = hexColors.map { Color(it) }

    fun getColorForIndex(index: Int): Color {
        return composeColors[index % composeColors.size]
    }

    companion object {
        fun fromName(name: String?): WheelPaletteTheme {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: RAINBOW
        }
    }
}
