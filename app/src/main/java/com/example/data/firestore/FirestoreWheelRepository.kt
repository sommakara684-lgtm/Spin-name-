package com.example.data.firestore

import android.content.Context
import com.example.R
import com.example.model.firestore.FirestoreSpinHistory
import com.example.model.firestore.FirestoreWheel
import com.example.model.firestore.FirestoreWheelItem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreWheelRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun observeWheels(userId: String): Flow<List<FirestoreWheel>> {
        val path = "users/$userId/wheels"
        return db.collection("users").document(userId).collection("wheels")
            .snapshots()
            .map { snapshot -> snapshot.toObjects(FirestoreWheel::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun createWheel(
        userId: String,
        title: String,
        description: String = "",
        themePalette: String = "RAINBOW",
        initialItems: List<String> = emptyList()
    ): String {
        val wheelsRef = db.collection("users").document(userId).collection("wheels")
        val wheelDoc = wheelsRef.document()
        val payload = mapOf(
            "userId" to userId,
            "title" to title.trim(),
            "description" to description.trim(),
            "themePalette" to themePalette,
            "spinDurationSeconds" to 4.5,
            "autoRemoveWinner" to false,
            "soundEnabled" to true,
            "hapticsEnabled" to true,
            "isPreset" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            wheelDoc.set(payload).await()
            if (initialItems.isNotEmpty()) {
                val batch = db.batch()
                val itemsRef = wheelDoc.collection("items")
                initialItems.forEachIndexed { index, name ->
                    val itemDoc = itemsRef.document()
                    val itemPayload = mapOf(
                        "wheelId" to wheelDoc.id,
                        "userId" to userId,
                        "name" to name.trim(),
                        "isEnabled" to true,
                        "weight" to 1L,
                        "colorHex" to null,
                        "orderIndex" to index.toLong(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    batch.set(itemDoc, itemPayload)
                }
                batch.commit().await()
            }
            return wheelDoc.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, wheelDoc.path)
            throw e
        }
    }

    suspend fun updateWheel(userId: String, wheel: FirestoreWheel) {
        require(wheel.id.isNotBlank()) { "Wheel ID must not be blank for update" }
        requireNotNull(wheel.createdAt) { "Wheel createdAt must not be null for update" }
        val docRef = db.collection("users").document(userId).collection("wheels").document(wheel.id)
        try {
            docRef.set(wheel.copy(updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteWheel(userId: String, wheelId: String) {
        val docRef = db.collection("users").document(userId).collection("wheels").document(wheelId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observeItems(userId: String, wheelId: String): Flow<List<FirestoreWheelItem>> {
        val path = "users/$userId/wheels/$wheelId/items"
        return db.collection("users").document(userId).collection("wheels").document(wheelId).collection("items")
            .snapshots()
            .map { snapshot -> snapshot.toObjects(FirestoreWheelItem::class.java).sortedBy { it.orderIndex } }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun addItem(
        userId: String,
        wheelId: String,
        name: String,
        weight: Long = 1L,
        orderIndex: Long = 0L
    ): String {
        val docRef = db.collection("users").document(userId).collection("wheels").document(wheelId).collection("items").document()
        val payload = mapOf(
            "wheelId" to wheelId,
            "userId" to userId,
            "name" to name.trim(),
            "isEnabled" to true,
            "weight" to weight,
            "colorHex" to null,
            "orderIndex" to orderIndex,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateItem(userId: String, wheelId: String, item: FirestoreWheelItem) {
        require(item.id.isNotBlank()) { "Item ID must not be blank for update" }
        requireNotNull(item.createdAt) { "Item createdAt must not be null for update" }
        val docRef = db.collection("users").document(userId).collection("wheels").document(wheelId).collection("items").document(item.id)
        try {
            docRef.set(item.copy(updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteItem(userId: String, wheelId: String, itemId: String) {
        val docRef = db.collection("users").document(userId).collection("wheels").document(wheelId).collection("items").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observeHistory(userId: String, wheelId: String): Flow<List<FirestoreSpinHistory>> {
        val path = "users/$userId/wheels/$wheelId/history"
        return db.collection("users").document(userId).collection("wheels").document(wheelId).collection("history")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FirestoreSpinHistory::class.java).sortedByDescending { it.timestamp?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun recordSpin(
        userId: String,
        wheelId: String,
        winnerName: String,
        autoRemoveWinner: Boolean = false,
        winningItemId: String? = null
    ) {
        val histRef = db.collection("users").document(userId).collection("wheels").document(wheelId).collection("history").document()
        val payload = mapOf(
            "wheelId" to wheelId,
            "userId" to userId,
            "winnerName" to winnerName,
            "timestamp" to FieldValue.serverTimestamp()
        )
        try {
            histRef.set(payload).await()
            if (autoRemoveWinner && winningItemId != null) {
                val itemRef = db.collection("users").document(userId).collection("wheels").document(wheelId).collection("items").document(winningItemId)
                itemRef.update("isEnabled", false).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, histRef.path)
            throw e
        }
    }
}
