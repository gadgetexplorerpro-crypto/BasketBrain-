package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiMealPlannerService
import com.example.data.model.BudgetSummary
import com.example.data.model.GroceryItem
import com.example.data.model.HouseholdMember
import com.example.data.model.LeftoverRescueSuggestion
import com.example.data.model.PantryItem
import com.example.data.model.PlannedMeal
import com.example.data.model.PurchaseRecord
import com.example.data.model.ScannedReceiptDraft
import com.example.data.model.UserProfile
import com.example.data.repository.BasketBrainRepository
import com.example.domain.BudgetAndGroceryEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val sender: String, // "USER" or "ASSISTANT"
    val text: String
)

enum class MainNavTab(val route: String, val label: String) {
    HOME("home", "Home"),
    MEAL_PLAN("meal_plan", "Meal Plan"),
    GROCERY("grocery", "Grocery"),
    PANTRY("pantry", "Pantry"),
    PROFILE("profile", "Profile")
}

enum class ActiveModalSheet {
    NONE,
    ADD_EXPENSE,
    SCAN_RECEIPT,
    LEFTOVER_RESCUE,
    RECIPE_DISCOVERY,
    SPENDING_ANALYTICS,
    AI_ASSISTANT,
    SUBSCRIPTION_PLANS,
    FAMILY_MEMBERS,
    DATA_EXPORT_PREVIEW
}

class BasketBrainViewModel(
    private val repository: BasketBrainRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(MainNavTab.HOME)
    val selectedTab: StateFlow<MainNavTab> = _selectedTab.asStateFlow()

    private val _activeSheet = MutableStateFlow(ActiveModalSheet.NONE)
    val activeSheet: StateFlow<ActiveModalSheet> = _activeSheet.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    private val _leftoverIdeas = MutableStateFlow<List<LeftoverRescueSuggestion>>(emptyList())
    val leftoverIdeas: StateFlow<List<LeftoverRescueSuggestion>> = _leftoverIdeas.asStateFlow()

    private val _scannedReceiptDraft = MutableStateFlow<ScannedReceiptDraft?>(null)
    val scannedReceiptDraft: StateFlow<ScannedReceiptDraft?> = _scannedReceiptDraft.asStateFlow()

    private val _exportedDataJson = MutableStateFlow<String>("")
    val exportedDataJson: StateFlow<String> = _exportedDataJson.asStateFlow()

    private val _assistantMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome",
                sender = "ASSISTANT",
                text = "Hi! I'm BasketBrain Assistant. Ask me what to cook tonight for under $5, how to stretch your grocery budget, or how to use your pantry staples!"
            )
        )
    )
    val assistantMessages: StateFlow<List<ChatMessage>> = _assistantMessages.asStateFlow()

    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
        .catch { e ->
            Log.w(TAG, "Error observing userProfile", e)
            emit(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val members: StateFlow<List<HouseholdMember>> = repository.observeMembers(currentUserId)
        .catch { e ->
            Log.w(TAG, "Error observing members", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val plannedMeals: StateFlow<List<PlannedMeal>> = repository.observePlannedMeals(currentUserId)
        .catch { e ->
            Log.w(TAG, "Error observing plannedMeals", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val pantryItems: StateFlow<List<PantryItem>> = repository.observePantryItems(currentUserId)
        .catch { e ->
            Log.w(TAG, "Error observing pantryItems", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val groceryItems: StateFlow<List<GroceryItem>> = repository.observeGroceryItems(currentUserId)
        .catch { e ->
            Log.w(TAG, "Error observing groceryItems", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val purchases: StateFlow<List<PurchaseRecord>> = repository.observePurchases(currentUserId)
        .catch { e ->
            Log.w(TAG, "Error observing purchases", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val budgetSummary: StateFlow<BudgetSummary> = combine(
        userProfile,
        purchases,
        plannedMeals,
        groceryItems
    ) { profile, purchaseList, mealList, groceryList ->
        BudgetAndGroceryEngine.calculateBudgetSummary(
            profile = profile ?: UserProfile(userId = currentUserId),
            purchases = purchaseList,
            plannedMeals = mealList,
            groceryItems = groceryList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BudgetAndGroceryEngine.calculateBudgetSummary(
            profile = UserProfile(userId = currentUserId),
            purchases = emptyList(),
            plannedMeals = emptyList(),
            groceryItems = emptyList()
        )
    )

    fun selectTab(tab: MainNavTab) {
        _selectedTab.value = tab
    }

    fun openSheet(sheet: ActiveModalSheet) {
        _activeSheet.value = sheet
    }

    fun closeSheet() {
        _activeSheet.value = ActiveModalSheet.NONE
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun showStatus(message: String) {
        _statusBanner.value = message
    }

    /**
     * Completes initial onboarding, saves household profile, seeds initial pantry staples
     * entered by the user, generates the budget-first 7-day meal plan, and builds the consolidated grocery list.
     */
    fun completeOnboarding(
        updatedProfile: UserProfile,
        initialPantryCsv: String
    ) {
        viewModelScope.launch {
            _isGeneratingAi.value = true
            try {
                val finalProfile = updatedProfile.copy(
                    id = currentUserId,
                    userId = currentUserId,
                    onboardingCompleted = true
                )
                repository.saveUserProfile(finalProfile)

                val createdPantry = mutableListOf<PantryItem>()
                val tokens = initialPantryCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                for (token in tokens) {
                    val item = PantryItem(
                        userId = currentUserId,
                        name = token.replaceFirstChar { it.uppercase() },
                        quantity = 1.0,
                        unit = if (token.equals("eggs", ignoreCase = true)) "pcs" else "kg",
                        category = if (token.equals("eggs", ignoreCase = true)) "Dairy" else "Grains",
                        purchaseDate = todayIsoDate(),
                        expiryDate = "2026-11-30",
                        storageLocation = "Cupboard"
                    )
                    repository.createPantryItem(item)
                    createdPantry.add(item)
                }

                // Generate initial budget-first meal plan
                val generatedPlan = GeminiMealPlannerService.generateAiMealPlan(
                    userId = currentUserId,
                    profile = finalProfile,
                    pantryItems = createdPantry,
                    members = members.value
                )
                for (meal in generatedPlan) {
                    repository.createPlannedMeal(meal)
                }

                // Consolidate smart grocery list subtracting pantry quantities
                val consolidated = BudgetAndGroceryEngine.consolidateGroceryList(
                    userId = currentUserId,
                    plannedMeals = generatedPlan,
                    pantryItems = createdPantry
                )
                for (gItem in consolidated) {
                    repository.saveGroceryItem(gItem)
                }

                _statusBanner.value = "Welcome to BasketBrain! Your budget-first meal plan and smart grocery list are ready."
            } catch (e: Exception) {
                _statusBanner.value = "Saved profile: ${e.localizedMessage ?: "Ready"}"
            } finally {
                _isGeneratingAi.value = false
            }
        }
    }

    fun updateProfileSettings(updated: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(updated.copy(userId = currentUserId, onboardingCompleted = true))
                .onSuccess { _statusBanner.value = "Household & budget preferences saved." }
                .onFailure { _statusBanner.value = "Failed to save profile: ${it.localizedMessage}" }
        }
    }

    fun addFamilyMember(
        name: String,
        role: String,
        dietaryPreference: String,
        dislikedFoods: String,
        allergies: String,
        servingMultiplier: Double
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val member = HouseholdMember(
                userId = currentUserId,
                name = name.trim(),
                role = role,
                dietaryPreference = dietaryPreference.ifBlank { "No restrictions" },
                dislikedFoods = dislikedFoods.trim(),
                servingMultiplier = servingMultiplier.coerceIn(0.25, 3.0),
                allergies = allergies.trim()
            )
            repository.addMember(member)
                .onSuccess { _statusBanner.value = "Added $name to household family plan." }
        }
    }

    fun removeFamilyMember(memberId: String) {
        viewModelScope.launch {
            repository.deleteMember(memberId)
        }
    }

    fun generateBudgetFirstMealPlanNow(
        customBudgetCents: Long? = null,
        customDays: Int? = null
    ) {
        viewModelScope.launch {
            val currentProf = userProfile.value ?: UserProfile(userId = currentUserId, onboardingCompleted = true)
            if (!BudgetAndGroceryEngine.canAccessProFeature(
                    currentProf.subscriptionState,
                    "AI_GENERATION",
                    currentProf.aiGenerationsUsed
                )
            ) {
                _statusBanner.value = "Free tier AI generation limit reached (5/5). Upgrade to BasketBrain Pro for higher limits."
                return@launch
            }

            _isGeneratingAi.value = true
            try {
                val effectiveProfile = currentProf.copy(
                    weeklyBudgetCents = customBudgetCents ?: currentProf.weeklyBudgetCents,
                    planningDays = customDays ?: currentProf.planningDays,
                    aiGenerationsUsed = currentProf.aiGenerationsUsed + 1
                )
                repository.saveUserProfile(effectiveProfile)

                // Delete old meals first
                for (old in plannedMeals.value) {
                    if (old.id.isNotBlank()) repository.deletePlannedMeal(old.id)
                }

                val newMeals = GeminiMealPlannerService.generateAiMealPlan(
                    userId = currentUserId,
                    profile = effectiveProfile,
                    pantryItems = pantryItems.value,
                    members = members.value
                )
                for (meal in newMeals) {
                    repository.createPlannedMeal(meal)
                }

                // Rebuild consolidated smart grocery list
                rebuildConsolidatedGroceryList(newMeals, pantryItems.value)
                _statusBanner.value = "Generated ${newMeals.size} budget-first meals reusing your pantry!"
            } catch (e: Exception) {
                _statusBanner.value = "Could not generate meal plan: ${e.localizedMessage}"
            } finally {
                _isGeneratingAi.value = false
            }
        }
    }

    fun markMealCookedAndDeductPantry(meal: PlannedMeal) {
        viewModelScope.launch {
            if (meal.id.isBlank() || meal.createdAt == null) return@launch
            val updatedMeal = meal.copy(isCooked = !meal.isCooked)
            repository.updatePlannedMeal(updatedMeal)

            if (!meal.isCooked) {
                val deductedPantry = BudgetAndGroceryEngine.deductPantryForCookedMeal(meal, pantryItems.value)
                for (pItem in deductedPantry) {
                    if (pItem.id.isNotBlank() && pItem.createdAt != null) {
                        repository.updatePantryItem(pItem)
                    }
                }
                _statusBanner.value = "Marked '${meal.recipeName}' cooked & deducted used pantry ingredients!"
            } else {
                _statusBanner.value = "Unmarked '${meal.recipeName}' as cooked."
            }
        }
    }

    fun swapMealWithAffordableAlternative(meal: PlannedMeal) {
        viewModelScope.launch {
            if (meal.id.isBlank() || meal.createdAt == null) return@launch
            val alternatives = listOf(
                Triple(
                    "Spiced Chickpea & Spinach Rice Skillet",
                    115L * meal.servings,
                    "Rice|0.25|kg|Grains|90;Chickpeas|0.25|kg|Grains|120;Spinach|0.15|kg|Vegetables|110"
                ),
                Triple(
                    "Golden Lentil & Roasted Carrot Bowl",
                    100L * meal.servings,
                    "Lentils|0.25|kg|Grains|100;Carrots|0.25|kg|Vegetables|80;Onions|0.15|kg|Vegetables|60"
                ),
                Triple(
                    "Savory Garlic Egg & Tomato Rice",
                    105L * meal.servings,
                    "Rice|0.25|kg|Grains|90;Eggs|4.0|pcs|Dairy|120;Tomatoes|0.25|kg|Vegetables|90"
                )
            )
            val pick = alternatives.firstOrNull { !it.first.equals(meal.recipeName, ignoreCase = true) }
                ?: alternatives.first()
            val swapped = meal.copy(
                recipeName = pick.first,
                estimatedCostCents = pick.second,
                costPerServingCents = pick.second / meal.servings.coerceAtLeast(1),
                ingredientsSummary = pick.third
            )
            repository.updatePlannedMeal(swapped)
            val updatedList = plannedMeals.value.map { if (it.id == meal.id) swapped else it }
            rebuildConsolidatedGroceryList(updatedList, pantryItems.value)
            _statusBanner.value = "Swapped to '${swapped.recipeName}' and updated grocery quantities & costs."
        }
    }

    fun toggleSaveRecipe(meal: PlannedMeal) {
        viewModelScope.launch {
            if (meal.id.isBlank() || meal.createdAt == null) return@launch
            val updated = meal.copy(isSavedRecipe = !meal.isSavedRecipe)
            repository.updatePlannedMeal(updated)
            _statusBanner.value = if (updated.isSavedRecipe) {
                "Saved '${meal.recipeName}' to Favorites!"
            } else {
                "Removed '${meal.recipeName}' from Favorites."
            }
        }
    }

    fun addSingleMealIngredientsToGrocery(meal: PlannedMeal) {
        viewModelScope.launch {
            val consolidated = BudgetAndGroceryEngine.consolidateGroceryList(
                userId = currentUserId,
                plannedMeals = listOf(meal),
                pantryItems = pantryItems.value,
                existingGroceryItems = groceryItems.value
            )
            for (item in consolidated) {
                repository.saveGroceryItem(item)
            }
            _statusBanner.value = "Consolidated ingredients for '${meal.recipeName}' into your Grocery List."
        }
    }

    fun syncConsolidatedGroceryListNow() {
        viewModelScope.launch {
            rebuildConsolidatedGroceryList(plannedMeals.value, pantryItems.value)
            _statusBanner.value = "Grocery list consolidated from planned meals (pantry stock subtracted)."
        }
    }

    private suspend fun rebuildConsolidatedGroceryList(
        meals: List<PlannedMeal>,
        pantry: List<PantryItem>
    ) {
        val currentGroceries = groceryItems.value
        val consolidated = BudgetAndGroceryEngine.consolidateGroceryList(
            userId = currentUserId,
            plannedMeals = meals,
            pantryItems = pantry,
            existingGroceryItems = currentGroceries
        )
        // Remove stale non-custom items not in consolidated
        for (old in currentGroceries) {
            if (old.id.isNotBlank() && old.fromRecipeName != "Custom" && !old.isRecurring) {
                val stillNeeded = consolidated.any {
                    it.name.equals(old.name, ignoreCase = true) && it.unit.equals(old.unit, ignoreCase = true)
                }
                if (!stillNeeded) {
                    repository.deleteGroceryItem(old.id)
                }
            }
        }
        for (item in consolidated) {
            repository.saveGroceryItem(item)
        }
    }

    fun addCustomGroceryItem(
        name: String,
        quantity: Double,
        unit: String,
        category: String,
        estimatedPriceCents: Long,
        isRecurring: Boolean
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val item = GroceryItem(
                userId = currentUserId,
                name = name.trim(),
                requiredQuantity = quantity.coerceAtLeast(0.1),
                unit = unit.ifBlank { "pcs" },
                category = category,
                estimatedPriceCents = estimatedPriceCents.coerceAtLeast(0L),
                isPurchased = false,
                fromRecipeName = "Custom",
                isRecurring = isRecurring
            )
            repository.saveGroceryItem(item)
            _statusBanner.value = "Added '${item.name}' to Grocery List."
        }
    }

    fun updateGroceryItemPriceOrQty(
        item: GroceryItem,
        newQty: Double,
        newEstimatedPriceCents: Long
    ) {
        viewModelScope.launch {
            repository.saveGroceryItem(
                item.copy(
                    requiredQuantity = newQty.coerceAtLeast(0.1),
                    estimatedPriceCents = newEstimatedPriceCents.coerceAtLeast(0L)
                )
            )
            _statusBanner.value = "Updated estimate for '${item.name}'."
        }
    }

    /**
     * Marks a grocery item as purchased. Only records spending when the user confirms
     * the actual purchase price (`confirmActualPriceCents != null`).
     */
    fun markGroceryItemPurchased(
        item: GroceryItem,
        confirmActualPriceCents: Long? = null,
        storeName: String = "Supermarket"
    ) {
        viewModelScope.launch {
            val newPurchasedState = !item.isPurchased
            val updated = item.copy(
                isPurchased = newPurchasedState,
                actualPriceCents = confirmActualPriceCents ?: item.estimatedPriceCents
            )
            repository.saveGroceryItem(updated)

            if (newPurchasedState && confirmActualPriceCents != null && confirmActualPriceCents > 0L) {
                val purchase = PurchaseRecord(
                    userId = currentUserId,
                    storeName = storeName.ifBlank { "Grocery Store" },
                    purchaseDate = todayIsoDate(),
                    category = item.category,
                    totalAmountCents = confirmActualPriceCents,
                    itemsSummary = "${item.name} (${item.requiredQuantity} ${item.unit})",
                    isFromReceipt = false
                )
                repository.recordPurchase(purchase)
                // Also add purchased item into Pantry inventory!
                val existingPantry = pantryItems.value.firstOrNull {
                    it.name.equals(item.name, ignoreCase = true) && it.unit.equals(item.unit, ignoreCase = true)
                }
                if (existingPantry != null && existingPantry.id.isNotBlank() && existingPantry.createdAt != null) {
                    repository.updatePantryItem(
                        existingPantry.copy(
                            quantity = existingPantry.quantity + item.requiredQuantity,
                            purchasePriceCents = confirmActualPriceCents
                        )
                    )
                } else {
                    repository.createPantryItem(
                        PantryItem(
                            userId = currentUserId,
                            name = item.name,
                            quantity = item.requiredQuantity,
                            unit = item.unit,
                            category = item.category,
                            purchaseDate = todayIsoDate(),
                            expiryDate = "2026-11-20",
                            purchasePriceCents = confirmActualPriceCents,
                            storageLocation = if (item.category in listOf("Dairy", "Vegetables", "Meat and seafood")) "Refrigerator" else "Cupboard"
                        )
                    )
                }
                _statusBanner.value = "Purchased '${item.name}', recorded expense, and stocked Pantry!"
            }
        }
    }

    fun deleteGroceryItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteGroceryItem(itemId)
        }
    }

    fun recordGroceryPurchase(
        storeName: String,
        purchaseDate: String,
        category: String,
        totalAmountCents: Long,
        itemsSummary: String,
        receiptHash: String = "",
        isFromReceipt: Boolean = false
    ) {
        if (storeName.isBlank() || totalAmountCents <= 0L) return
        viewModelScope.launch {
            // Duplicate receipt guard
            if (isFromReceipt && receiptHash.isNotBlank()) {
                val isDuplicate = purchases.value.any { it.receiptHash == receiptHash }
                if (isDuplicate) {
                    _statusBanner.value = "Duplicate receipt detected — this receipt has already been recorded."
                    return@launch
                }
            }
            val record = PurchaseRecord(
                userId = currentUserId,
                storeName = storeName.trim(),
                purchaseDate = purchaseDate.ifBlank { todayIsoDate() },
                category = category,
                totalAmountCents = totalAmountCents,
                itemsSummary = itemsSummary.ifBlank { "Grocery shopping" },
                receiptHash = receiptHash,
                isFromReceipt = isFromReceipt
            )
            repository.recordPurchase(record)
                .onSuccess {
                    _scannedReceiptDraft.value = null
                    _statusBanner.value = "Recorded purchase at ${record.storeName} (${
                        BudgetAndGroceryEngine.formatCurrency(
                            totalAmountCents,
                            userProfile.value?.currencyCode ?: "USD"
                        )
                    })."
                }
        }
    }

    fun deletePurchaseRecord(purchaseId: String) {
        viewModelScope.launch {
            repository.deletePurchase(purchaseId)
                .onSuccess { _statusBanner.value = "Deleted transaction and updated remaining budget." }
        }
    }

    fun addPantryItem(
        name: String,
        quantity: Double,
        unit: String,
        category: String,
        storageLocation: String,
        expiryDate: String,
        purchasePriceCents: Long
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val item = PantryItem(
                userId = currentUserId,
                name = name.trim(),
                quantity = quantity.coerceAtLeast(0.0),
                unit = unit.ifBlank { "kg" },
                category = category,
                purchaseDate = todayIsoDate(),
                expiryDate = expiryDate.ifBlank { "2026-11-30" },
                purchasePriceCents = purchasePriceCents.coerceAtLeast(0L),
                storageLocation = storageLocation,
                lowStockThreshold = 0.5
            )
            repository.createPantryItem(item)
                .onSuccess {
                    _statusBanner.value = "Added '${item.name}' to ${item.storageLocation}."
                }
        }
    }

    fun adjustPantryItemQuantity(item: PantryItem, delta: Double) {
        viewModelScope.launch {
            if (item.id.isBlank() || item.createdAt == null) return@launch
            val newQty = (item.quantity + delta).coerceAtLeast(0.0)
            if (newQty <= 0.0) {
                repository.deletePantryItem(item.id)
                _statusBanner.value = "Used up '${item.name}' and removed from pantry."
            } else {
                repository.updatePantryItem(item.copy(quantity = newQty))
            }
        }
    }

    fun deletePantryItem(itemId: String) {
        viewModelScope.launch {
            repository.deletePantryItem(itemId)
        }
    }

    fun runLeftoverRescue(selectedIngredients: List<String>) {
        viewModelScope.launch {
            _leftoverIdeas.value = BudgetAndGroceryEngine.generateLeftoverRescueIdeas(selectedIngredients)
        }
    }

    fun scanReceiptImage(bitmap: Bitmap?, storeHint: String) {
        viewModelScope.launch {
            _isGeneratingAi.value = true
            try {
                val draft = GeminiMealPlannerService.scanReceiptBitmap(bitmap, storeHint)
                _scannedReceiptDraft.value = draft
            } finally {
                _isGeneratingAi.value = false
            }
        }
    }

    fun clearScannedReceiptDraft() {
        _scannedReceiptDraft.value = null
    }

    fun sendAssistantQuestion(question: String) {
        if (question.isBlank()) return
        val trimmed = question.trim()
        _assistantMessages.value = _assistantMessages.value + ChatMessage(
            sender = "USER",
            text = trimmed
        )
        viewModelScope.launch {
            _isGeneratingAi.value = true
            try {
                val reply = GeminiMealPlannerService.askBasketBrainAssistant(
                    question = trimmed,
                    profile = userProfile.value ?: UserProfile(userId = currentUserId),
                    pantryItems = pantryItems.value,
                    plannedMeals = plannedMeals.value,
                    remainingBudgetCents = budgetSummary.value.remainingCents
                )
                _assistantMessages.value = _assistantMessages.value + ChatMessage(
                    sender = "ASSISTANT",
                    text = reply
                )
            } finally {
                _isGeneratingAi.value = false
            }
        }
    }

    fun prepareDataExport() {
        viewModelScope.launch {
            val json = repository.exportUserDataJson(
                profile = userProfile.value,
                members = members.value,
                meals = plannedMeals.value,
                pantry = pantryItems.value,
                groceries = groceryItems.value,
                purchases = purchases.value
            )
            _exportedDataJson.value = json
            _activeSheet.value = ActiveModalSheet.DATA_EXPORT_PREVIEW
        }
    }

    fun deleteCloudDataOnly() {
        viewModelScope.launch {
            repository.deleteUserDataOnly()
                .onSuccess {
                    _statusBanner.value = "All cloud meal plans, pantry items, groceries, and purchases deleted (account kept active)."
                }
                .onFailure {
                    _statusBanner.value = "Error deleting cloud data: ${it.localizedMessage}"
                }
        }
    }

    fun deleteAccountData(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAllUserData()
                .onSuccess { onDeleted() }
                .onFailure { _statusBanner.value = "Error deleting data: ${it.localizedMessage}" }
        }
    }

    private fun todayIsoDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private companion object {
        const val TAG = "BasketBrainVM"
    }
}
