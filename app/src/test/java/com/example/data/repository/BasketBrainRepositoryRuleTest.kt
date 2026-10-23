package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.PantryItem
import com.example.data.model.PurchaseRecord
import com.example.data.model.UserProfile
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.UUID

class BasketBrainRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndObserveUserProfile_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = BasketBrainRepository(firestore)

        val profile = UserProfile(
            userId = uid,
            householdName = "Alice Family",
            adultsCount = 2,
            childrenCount = 1,
            weeklyBudgetCents = 10000L,
            currencyCode = "USD",
            onboardingCompleted = true
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveUserProfile(profile) }
        assertThat(saveResult.isSuccess).isTrue()

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProfile(uid).first { it != null && it.householdName == "Alice Family" }
        }
        assertThat(observed?.weeklyBudgetCents).isEqualTo(10000L)
    }

    @Test
    fun createAndObservePantryItem_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = BasketBrainRepository(firestore)
        val uniqueId = "pantry_${UUID.randomUUID().toString().replace("-", "")}"

        val item = PantryItem(
            id = uniqueId,
            userId = uid,
            name = "Basmati Rice",
            quantity = 2.0,
            unit = "kg",
            category = "Grains",
            storageLocation = "Cupboard"
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createPantryItem(item) }
        assertThat(createResult.isSuccess).isTrue()

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observePantryItems(uid).first { list -> list.any { it.id == uniqueId } }
        }
        assertThat(emitted.map { it.name }).contains("Basmati Rice")
    }

    @Test
    fun getPantryItemById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = BasketBrainRepository(firestore)
        val itemId = "pantry_${UUID.randomUUID().toString().replace("-", "")}"
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createPantryItem(
                PantryItem(
                    id = itemId,
                    userId = aliceUid,
                    name = "Private Lentils",
                    quantity = 1.0,
                    unit = "kg",
                    category = "Grains",
                    storageLocation = "Cupboard"
                )
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = BasketBrainRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getPantryItemById(aliceUid, itemId) }
        assertThat(result.isFailure).isTrue()
        val firestoreEx = result.exceptionOrNull()?.unwrapFirestoreException()
        assertThat(firestoreEx?.code).isEqualTo(FirebaseFirestoreException.Code.PERMISSION_DENIED)
    }

    @Test
    fun observePurchases_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = BasketBrainRepository(firestore)

        var caughtException: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observePurchases(aliceUid).first()
            }
        } catch (t: Throwable) {
            caughtException = t
        }
        assertThat(caughtException).isNotNull()
        val firestoreException = checkNotNull(caughtException?.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $caughtException"
        }
        assertThat(firestoreException.code).isEqualTo(FirebaseFirestoreException.Code.PERMISSION_DENIED)
    }

    @Test
    fun recordAndDeletePurchase_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = BasketBrainRepository(firestore)
        val purchaseId = "pur_${UUID.randomUUID().toString().replace("-", "")}"

        val record = PurchaseRecord(
            id = purchaseId,
            userId = uid,
            storeName = "Green Grocer",
            purchaseDate = "2026-10-22",
            category = "Vegetables",
            totalAmountCents = 2450L,
            itemsSummary = "Tomatoes, Spinach, Onions",
            isFromReceipt = false
        )
        val createRes = withTimeout(DEFAULT_TIMEOUT_MS) { repository.recordPurchase(record) }
        assertThat(createRes.isSuccess).isTrue()

        val deleteRes = withTimeout(DEFAULT_TIMEOUT_MS) { repository.deletePurchase(purchaseId) }
        assertThat(deleteRes.isSuccess).isTrue()
    }

    private companion object {
        const val ALICE_EMAIL = "alice@basketbrain.test"
        const val BOB_EMAIL = "bob@basketbrain.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
