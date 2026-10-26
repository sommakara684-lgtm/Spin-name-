package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firestore.FirestoreWheelRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirestoreWheelRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createWheel_authenticatedOwner_createsDocument() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = FirestoreWheelRepository(firestore)

        val wheelId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createWheel(
                userId = aliceUid,
                title = "Lucky Wheel",
                themePalette = "RAINBOW"
            )
        }
        assertNotNull(wheelId)
        assertTrue(wheelId.isNotBlank())
    }

    @Test
    fun observeWheels_authenticatedOwner_emitsCreatedWheel() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = FirestoreWheelRepository(firestore)

        val wheelId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createWheel(
                userId = aliceUid,
                title = "Dinner Chooser",
                themePalette = "SUNSET"
            )
        }

        val wheels = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeWheels(aliceUid).first { list -> list.any { it.id == wheelId } }
        }
        assertTrue(wheels.any { it.id == wheelId && it.title == "Dinner Chooser" })
    }

    @Test
    fun observeWheels_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestoreWheelRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createWheel(
                userId = aliceUid,
                title = "Private Alice Wheel"
            )
        }

        // Sign in as Bob and attempt to read Alice's wheels
        signInTestUser(BOB_EMAIL)
        val bobRepo = FirestoreWheelRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeWheels(aliceUid).first()
            }
            fail("Expected exception when Bob reads Alice's wheels")
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException but got $e", firestoreException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
        }
    }

    @Test
    fun observeWheels_unauthenticatedCaller_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = FirestoreWheelRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createWheel(userId = aliceUid, title = "Unauth Test Wheel")
        }

        // Sign out
        auth.signOut()

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeWheels(aliceUid).first()
            }
            fail("Expected exception when unauthenticated user reads wheels")
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException but got $e", firestoreException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_rule@test.com"
        const val BOB_EMAIL = "bob_rule@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
