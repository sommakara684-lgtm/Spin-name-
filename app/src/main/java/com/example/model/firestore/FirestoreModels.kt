package com.example.model.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreWheel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val themePalette: String = "RAINBOW",
    val spinDurationSeconds: Double = 4.5,
    val autoRemoveWinner: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val isPreset: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreWheelItem(
    @DocumentId val id: String = "",
    val wheelId: String = "",
    val userId: String = "",
    val name: String = "",
    val isEnabled: Boolean = true,
    val weight: Long = 1L,
    val colorHex: Long? = null,
    val orderIndex: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreSpinHistory(
    @DocumentId val id: String = "",
    val wheelId: String = "",
    val userId: String = "",
    val winnerName: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)
