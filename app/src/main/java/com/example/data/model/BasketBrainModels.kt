package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Household Chef",
    val householdName: String = "My Household",
    val adultsCount: Int = 2,
    val childrenCount: Int = 1,
    val childAgeRanges: String = "6-10 yrs",
    val mealsPerDay: Int = 3,
    val planningDays: Int = 7,
    val weeklyBudgetCents: Long = 10000L, // $100.00 default
    val monthlyBudgetCents: Long = 40000L,
    val budgetCycle: String = "WEEKLY",
    val carryOverEnabled: Boolean = true,
    val currencyCode: String = "USD",
    val measurementSystem: String = "METRIC",
    val dietaryRestrictions: String = "No restrictions",
    val allergies: String = "",
    val dislikedIngredients: String = "",
    val favoriteCuisines: String = "Mediterranean, Asian, Homestyle",
    val cookingExperience: String = "INTERMEDIATE",
    val maxCookingTimeMins: Int = 35,
    val kitchenEquipment: String = "Stove, Oven, Skillet, Pot",
    val preferredStores: String = "Local Supermarket, Aldi, Trader Joe's",
    val subscriptionState: String = "FREE",
    val aiGenerationsUsed: Int = 0,
    val onboardingCompleted: Boolean = false,
    val notificationsEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class HouseholdMember(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val role: String = "ADULT", // ADULT, PARTNER, CHILD
    val dietaryPreference: String = "No restrictions",
    val dislikedFoods: String = "",
    val servingMultiplier: Double = 1.0,
    val allergies: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PlannedMeal(
    @DocumentId val id: String = "",
    val userId: String = "",
    val planId: String = "active_week",
    val dayIndex: Int = 1,
    val dayName: String = "Monday",
    val mealType: String = "DINNER", // BREAKFAST, LUNCH, DINNER, SNACK
    val recipeName: String = "",
    val prepTimeMins: Int = 10,
    val cookTimeMins: Int = 20,
    val servings: Int = 3,
    val estimatedCostCents: Long = 450L,
    val costPerServingCents: Long = 150L,
    val ingredientsSummary: String = "", // Format: "Name|qty|unit|Category|cents;..."
    val instructions: String = "",
    val caloriesPerServing: Int = 480,
    val proteinGrams: Int = 24,
    val carbsGrams: Int = 52,
    val fatGrams: Int = 14,
    @get:PropertyName("isCooked") @field:PropertyName("isCooked") val isCooked: Boolean = false,
    val familyModificationNote: String = "Shared base meal; add chili flakes for adults, keep mild for kids.",
    @get:PropertyName("isSavedRecipe") @field:PropertyName("isSavedRecipe") val isSavedRecipe: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PantryItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val quantity: Double = 1.0,
    val unit: String = "kg",
    val category: String = "Grains",
    val purchaseDate: String = "2026-10-20",
    val expiryDate: String = "2026-11-15",
    val purchasePriceCents: Long = 0L,
    val storageLocation: String = "Cupboard", // Refrigerator, Freezer, Cupboard, Other
    val lowStockThreshold: Double = 0.5,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GroceryItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val requiredQuantity: Double = 1.0,
    val unit: String = "kg",
    val category: String = "Vegetables",
    val estimatedPriceCents: Long = 250L,
    @get:PropertyName("isPurchased") @field:PropertyName("isPurchased") val isPurchased: Boolean = false,
    val actualPriceCents: Long = 0L,
    val fromRecipeName: String = "Meal Plan",
    @get:PropertyName("isRecurring") @field:PropertyName("isRecurring") val isRecurring: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PurchaseRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val storeName: String = "",
    val purchaseDate: String = "2026-10-22",
    val category: String = "Vegetables",
    val totalAmountCents: Long = 0L,
    val itemsSummary: String = "",
    val receiptHash: String = "",
    @get:PropertyName("isFromReceipt") @field:PropertyName("isFromReceipt") val isFromReceipt: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class ParsedIngredientSpec(
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    val estimatedCostCents: Long
)

data class BudgetSummary(
    val totalBudgetCents: Long,
    val spentCents: Long,
    val remainingCents: Long,
    val usagePercentage: Int,
    val isOverBudget: Boolean,
    val plannedMealsTotalCostCents: Long,
    val estimatedShoppingListCostCents: Long,
    val averageCostPerMealCents: Long,
    val averageCostPerServingCents: Long,
    val categorySpendingCents: Map<String, Long>,
    val daysRemainingInWeek: Int
)

data class LeftoverRescueSuggestion(
    val recipeName: String,
    val usedLeftovers: List<String>,
    val additionalIngredientsNeeded: List<String>,
    val estimatedAdditionalCostCents: Long,
    val prepTimeMins: Int,
    val instructions: String,
    val safetyReminder: String
)

data class ScannedReceiptDraft(
    val storeName: String,
    val purchaseDate: String,
    val category: String,
    val lineItems: List<ScannedReceiptLineItem>,
    val totalCents: Long,
    val receiptHash: String
)

data class ScannedReceiptLineItem(
    val name: String,
    val quantity: Double,
    val unitPriceCents: Long,
    val totalPriceCents: Long,
    val category: String
)

enum class SubscriptionTierState(val code: String, val label: String, val isProEntitled: Boolean) {
    FREE("FREE", "Free Plan", false),
    ACTIVE("ACTIVE", "BasketBrain Pro (Active)", true),
    TRIALING("TRIALING", "BasketBrain Pro (Trial)", true),
    GRACE_PERIOD("GRACE_PERIOD", "BasketBrain Pro (Grace Period)", true),
    EXPIRED("EXPIRED", "Expired (Free Tier Active)", false),
    CANCELED("CANCELED", "Canceled (Active Until Expiry)", true);

    companion object {
        fun fromCode(code: String): SubscriptionTierState =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: FREE
    }
}

object BasketBrainCatalog {
    val currencies = listOf(
        "USD" to "$",
        "GBP" to "£",
        "CAD" to "CA$",
        "AUD" to "A$",
        "EUR" to "€",
        "PKR" to "Rs "
    )

    val spendingCategories = listOf(
        "Vegetables",
        "Fruits",
        "Meat and seafood",
        "Dairy",
        "Grains",
        "Snacks",
        "Beverages",
        "Other"
    )

    val storageLocations = listOf(
        "Refrigerator",
        "Freezer",
        "Cupboard",
        "Other"
    )

    val dietaryOptions = listOf(
        "No restrictions",
        "Vegetarian",
        "Vegan",
        "Halal",
        "High protein",
        "Dairy free",
        "Gluten free",
        "Low sodium"
    )
}
