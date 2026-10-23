package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.GroceryItem
import com.example.data.model.HouseholdMember
import com.example.data.model.PantryItem
import com.example.data.model.PlannedMeal
import com.example.data.model.PurchaseRecord
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class BasketBrainRepository(private val db: FirebaseFirestore) {

    // CRITICAL: Always initialize with R.string.firestore_database_id
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. User Profile ---

    fun observeUserProfile(userId: String = requireUserId()): Flow<UserProfile?> = flow {
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                    throw error
                }
        )
    }

    suspend fun getUserProfile(userId: String = requireUserId()): Result<UserProfile?> = runCatching {
        val docRef = db.collection("users").document(userId)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val existing = docRef.get().await()
            val toWrite = if (existing.exists()) {
                val existingCreatedAt = existing.getTimestamp("createdAt") ?: profile.createdAt
                profile.copy(
                    id = uid,
                    userId = uid,
                    createdAt = existingCreatedAt,
                    updatedAt = null
                )
            } else {
                profile.copy(
                    id = uid,
                    userId = uid,
                    createdAt = null,
                    updatedAt = null
                )
            }
            docRef.set(toWrite).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- 2. Household Members ---

    fun observeMembers(userId: String = requireUserId()): Flow<List<HouseholdMember>> = flow {
        val path = "users/$userId/members"
        emitAll(
            db.collection("users").document(userId).collection("members")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(HouseholdMember::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addMember(member: HouseholdMember): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("members")
        val docRef = if (member.id.isNotBlank()) colRef.document(member.id) else colRef.document()
        try {
            docRef.set(
                member.copy(
                    id = docRef.id,
                    userId = uid,
                    createdAt = null,
                    updatedAt = null
                )
            ).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMember(memberId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("members").document(memberId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 3. Planned Meals ---

    fun observePlannedMeals(userId: String = requireUserId()): Flow<List<PlannedMeal>> = flow {
        val path = "users/$userId/planned_meals"
        emitAll(
            db.collection("users").document(userId).collection("planned_meals")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(PlannedMeal::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedWith(compareBy<PlannedMeal> { it.dayIndex }.thenBy { it.mealType })
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createPlannedMeal(meal: PlannedMeal): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("planned_meals")
        val docRef = if (meal.id.isNotBlank()) colRef.document(meal.id) else colRef.document()
        try {
            docRef.set(
                meal.copy(
                    id = docRef.id,
                    userId = uid,
                    createdAt = null,
                    updatedAt = null
                )
            ).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updatePlannedMeal(meal: PlannedMeal): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(meal.id.isNotBlank()) { "Meal ID must not be blank for update" }
        requireNotNull(meal.createdAt) { "Meal createdAt must not be null for update" }
        val docRef = db.collection("users").document(uid).collection("planned_meals").document(meal.id)
        try {
            docRef.set(meal.copy(userId = uid, updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deletePlannedMeal(mealId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("planned_meals").document(mealId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 4. Pantry Items ---

    fun observePantryItems(userId: String = requireUserId()): Flow<List<PantryItem>> = flow {
        val path = "users/$userId/pantry_items"
        emitAll(
            db.collection("users").document(userId).collection("pantry_items")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(PantryItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.name.lowercase() }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createPantryItem(item: PantryItem): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("pantry_items")
        val docRef = if (item.id.isNotBlank()) colRef.document(item.id) else colRef.document()
        try {
            docRef.set(
                item.copy(
                    id = docRef.id,
                    userId = uid,
                    createdAt = null,
                    updatedAt = null
                )
            ).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun getPantryItemById(targetUserId: String, itemId: String): Result<PantryItem?> = runCatching {
        val docRef = db.collection("users").document(targetUserId).collection("pantry_items").document(itemId)
        try {
            val snap = docRef.get().await()
            snap.toObject(PantryItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun updatePantryItem(item: PantryItem): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(item.id.isNotBlank()) { "PantryItem ID must not be blank for update" }
        requireNotNull(item.createdAt) { "PantryItem createdAt must not be null for update" }
        val docRef = db.collection("users").document(uid).collection("pantry_items").document(item.id)
        try {
            docRef.set(item.copy(userId = uid, updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deletePantryItem(itemId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("pantry_items").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 5. Smart Grocery List ---

    fun observeGroceryItems(userId: String = requireUserId()): Flow<List<GroceryItem>> = flow {
        val path = "users/$userId/grocery_items"
        emitAll(
            db.collection("users").document(userId).collection("grocery_items")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(GroceryItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedWith(compareBy<GroceryItem> { it.isPurchased }.thenBy { it.category }.thenBy { it.name })
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveGroceryItem(item: GroceryItem): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("grocery_items")
        val docRef = if (item.id.isNotBlank()) colRef.document(item.id) else colRef.document()
        try {
            if (item.id.isNotBlank() && item.createdAt != null) {
                docRef.set(item.copy(userId = uid, updatedAt = null)).await()
            } else {
                docRef.set(
                    item.copy(
                        id = docRef.id,
                        userId = uid,
                        createdAt = null,
                        updatedAt = null
                    )
                ).await()
            }
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteGroceryItem(itemId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("grocery_items").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 6. Grocery Purchases & Receipt Transactions ---

    fun observePurchases(userId: String = requireUserId()): Flow<List<PurchaseRecord>> = flow {
        val path = "users/$userId/purchases"
        emitAll(
            db.collection("users").document(userId).collection("purchases")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(PurchaseRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.purchaseDate }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun recordPurchase(purchase: PurchaseRecord): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("purchases")
        val docRef = if (purchase.id.isNotBlank()) colRef.document(purchase.id) else colRef.document()
        try {
            docRef.set(
                purchase.copy(
                    id = docRef.id,
                    userId = uid,
                    createdAt = null,
                    updatedAt = null
                )
            ).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deletePurchase(purchaseId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("purchases").document(purchaseId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 7. Export User Data & Account Deletion ---

    suspend fun exportUserDataJson(
        profile: UserProfile?,
        members: List<HouseholdMember>,
        meals: List<PlannedMeal>,
        pantry: List<PantryItem>,
        groceries: List<GroceryItem>,
        purchases: List<PurchaseRecord>
    ): String {
        return JSONObject().apply {
            put("app", "BasketBrain")
            put("exportedAt", System.currentTimeMillis())
            put("householdName", profile?.householdName ?: "My Household")
            put("weeklyBudgetCents", profile?.weeklyBudgetCents ?: 0L)
            put("currencyCode", profile?.currencyCode ?: "USD")
            put("membersCount", members.size)
            put("plannedMeals", JSONArray(meals.map { JSONObject().put("recipe", it.recipeName).put("day", it.dayName).put("costCents", it.estimatedCostCents) }))
            put("pantryItems", JSONArray(pantry.map { JSONObject().put("name", it.name).put("qty", it.quantity).put("unit", it.unit) }))
            put("groceryItems", JSONArray(groceries.map { JSONObject().put("name", it.name).put("qty", it.requiredQuantity).put("estCents", it.estimatedPriceCents) }))
            put("purchases", JSONArray(purchases.map { JSONObject().put("store", it.storeName).put("date", it.purchaseDate).put("totalCents", it.totalAmountCents) }))
        }.toString(2)
    }

    suspend fun deleteUserDataOnly(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val userDoc = db.collection("users").document(uid)
        val subcollections = listOf("members", "planned_meals", "pantry_items", "grocery_items", "purchases")
        try {
            for (sub in subcollections) {
                val snap = userDoc.collection(sub).whereEqualTo("userId", uid).get().await()
                for (doc in snap.documents) {
                    doc.reference.delete().await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, userDoc.path)
            throw e
        }
    }

    suspend fun deleteAllUserData(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val userDoc = db.collection("users").document(uid)
        val subcollections = listOf("members", "planned_meals", "pantry_items", "grocery_items", "purchases")
        try {
            for (sub in subcollections) {
                val snap = userDoc.collection(sub).whereEqualTo("userId", uid).get().await()
                for (doc in snap.documents) {
                    doc.reference.delete().await()
                }
            }
            userDoc.delete().await()
            try {
                auth.currentUser?.delete()?.await()
            } catch (_: Exception) {
                // Auth session will be signed out by caller even if re-auth is needed
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, userDoc.path)
            throw e
        }
    }
}
