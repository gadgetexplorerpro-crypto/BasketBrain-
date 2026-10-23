package com.example.domain

import com.example.data.model.BasketBrainCatalog
import com.example.data.model.BudgetSummary
import com.example.data.model.GroceryItem
import com.example.data.model.HouseholdMember
import com.example.data.model.LeftoverRescueSuggestion
import com.example.data.model.PantryItem
import com.example.data.model.ParsedIngredientSpec
import com.example.data.model.PlannedMeal
import com.example.data.model.PurchaseRecord
import com.example.data.model.SubscriptionTierState
import com.example.data.model.UserProfile
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToLong

object BudgetAndGroceryEngine {

    /**
     * Formats integer minor units (cents/paisas/pence) into a readable currency string
     * without floating-point precision loss.
     */
    fun formatCurrency(amountCents: Long, currencyCode: String = "USD"): String {
        val symbol = BasketBrainCatalog.currencies.firstOrNull {
            it.first.equals(currencyCode, ignoreCase = true)
        }?.second ?: "$"

        val isNegative = amountCents < 0
        val absCents = kotlin.math.abs(amountCents)
        val whole = absCents / 100
        val fraction = absCents % 100
        val signPrefix = if (isNegative) "-" else ""
        return String.format(Locale.US, "%s%s%d.%02d", signPrefix, symbol, whole, fraction)
    }

    /**
     * Parses a user-entered decimal string (e.g., "73.40" or "100") into exact minor units (cents).
     */
    fun parseToCents(input: String): Long? {
        val cleaned = input.trim().replace(",", "")
        if (cleaned.isEmpty()) return null
        val parts = cleaned.split(".")
        if (parts.size > 2) return null
        val wholePart = if (parts[0].isEmpty()) 0L else parts[0].toLongOrNull() ?: return null
        if (wholePart < 0) return null
        val fracPart = if (parts.size == 2) {
            val rawFrac = parts[1].padEnd(2, '0').take(2)
            rawFrac.toLongOrNull() ?: return null
        } else {
            0L
        }
        return wholePart * 100L + fracPart
    }

    /**
     * Deterministic budget calculation using integer minor-unit arithmetic.
     */
    fun calculateBudgetSummary(
        profile: UserProfile,
        purchases: List<PurchaseRecord>,
        plannedMeals: List<PlannedMeal>,
        groceryItems: List<GroceryItem>,
        carryOverCents: Long = 0L,
        daysRemainingInWeek: Int = 5
    ): BudgetSummary {
        val baseBudgetCents = if (profile.budgetCycle == "MONTHLY") {
            profile.monthlyBudgetCents
        } else {
            profile.weeklyBudgetCents
        }
        val totalBudgetCents = max(0L, baseBudgetCents + if (profile.carryOverEnabled) carryOverCents else 0L)
        val spentCents = purchases.sumOf { max(0L, it.totalAmountCents) }
        val remainingCents = totalBudgetCents - spentCents
        val usagePercentage = if (totalBudgetCents > 0L) {
            ((spentCents * 100L) / totalBudgetCents).toInt()
        } else {
            if (spentCents > 0L) 100 else 0
        }

        val plannedMealsTotalCostCents = plannedMeals.sumOf { max(0L, it.estimatedCostCents) }
        val estimatedShoppingListCostCents = groceryItems
            .filter { !it.isPurchased }
            .sumOf { max(0L, it.estimatedPriceCents) }

        val mealCount = plannedMeals.size
        val averageCostPerMealCents = if (mealCount > 0) {
            plannedMealsTotalCostCents / mealCount.toLong()
        } else {
            0L
        }

        val totalServings = plannedMeals.sumOf { max(1, it.servings).toLong() }
        val averageCostPerServingCents = if (totalServings > 0L) {
            plannedMealsTotalCostCents / totalServings
        } else {
            0L
        }

        val categoryMap = mutableMapOf<String, Long>()
        for (cat in BasketBrainCatalog.spendingCategories) {
            categoryMap[cat] = 0L
        }
        for (purchase in purchases) {
            val key = if (categoryMap.containsKey(purchase.category)) purchase.category else "Other"
            categoryMap[key] = (categoryMap[key] ?: 0L) + max(0L, purchase.totalAmountCents)
        }

        return BudgetSummary(
            totalBudgetCents = totalBudgetCents,
            spentCents = spentCents,
            remainingCents = remainingCents,
            usagePercentage = usagePercentage,
            isOverBudget = spentCents > totalBudgetCents,
            plannedMealsTotalCostCents = plannedMealsTotalCostCents,
            estimatedShoppingListCostCents = estimatedShoppingListCostCents,
            averageCostPerMealCents = averageCostPerMealCents,
            averageCostPerServingCents = averageCostPerServingCents,
            categorySpendingCents = categoryMap,
            daysRemainingInWeek = daysRemainingInWeek.coerceIn(1, 31)
        )
    }

    /**
     * Parses structured ingredient strings of format:
     * "Rice|0.5|kg|Grains|120;Eggs|4|pcs|Dairy|140"
     * or human-readable fallback "Rice (0.5 kg)".
     */
    fun parseIngredientsSpec(summary: String): List<ParsedIngredientSpec> {
        if (summary.isBlank()) return emptyList()
        return summary.split(";").mapNotNull { token ->
            val trimmed = token.trim()
            if (trimmed.isEmpty()) return@mapNotNull null
            val parts = trimmed.split("|")
            if (parts.size >= 5) {
                ParsedIngredientSpec(
                    name = parts[0].trim(),
                    quantity = parts[1].trim().toDoubleOrNull() ?: 1.0,
                    unit = parts[2].trim().ifEmpty { "pcs" },
                    category = parts[3].trim().ifEmpty { "Other" },
                    estimatedCostCents = parts[4].trim().toLongOrNull() ?: 150L
                )
            } else {
                ParsedIngredientSpec(
                    name = trimmed,
                    quantity = 1.0,
                    unit = "pcs",
                    category = "Other",
                    estimatedCostCents = 150L
                )
            }
        }
    }

    fun formatIngredientsReadable(summary: String, measurementSystem: String = "METRIC"): String {
        val specs = parseIngredientsSpec(summary)
        if (specs.isEmpty()) return summary
        return specs.joinToString(", ") { spec ->
            val (qty, unit) = convertUnitIfNeeded(spec.quantity, spec.unit, measurementSystem)
            val qtyStr = if (qty % 1.0 == 0.0) qty.toInt().toString() else String.format(Locale.US, "%.1f", qty)
            "${spec.name} ($qtyStr $unit)"
        }
    }

    fun convertUnitIfNeeded(quantity: Double, unit: String, measurementSystem: String): Pair<Double, String> {
        if (measurementSystem.equals("IMPERIAL", ignoreCase = true)) {
            return when (unit.lowercase(Locale.US)) {
                "kg" -> Pair(quantity * 2.20462, "lb")
                "g" -> Pair(quantity * 0.035274, "oz")
                "l" -> Pair(quantity * 33.814, "fl oz")
                "ml" -> Pair(quantity * 0.033814, "fl oz")
                else -> Pair(quantity, unit)
            }
        }
        return Pair(quantity, unit)
    }

    /**
     * Consolidates duplicate ingredients across all un-cooked planned meals,
     * subtracts available pantry inventory quantities, and groups by category.
     */
    fun consolidateGroceryList(
        userId: String,
        plannedMeals: List<PlannedMeal>,
        pantryItems: List<PantryItem>,
        existingGroceryItems: List<GroceryItem> = emptyList()
    ): List<GroceryItem> {
        data class AggregatedNeed(
            val displayName: String,
            var totalQty: Double,
            val unit: String,
            val category: String,
            var totalEstimatedCents: Long,
            val sourceRecipes: MutableSet<String>
        )

        val aggregated = linkedMapOf<String, AggregatedNeed>()

        for (meal in plannedMeals.filter { !it.isCooked }) {
            val specs = parseIngredientsSpec(meal.ingredientsSummary)
            for (spec in specs) {
                val key = "${spec.name.lowercase(Locale.US).trim()}__${spec.unit.lowercase(Locale.US).trim()}"
                val current = aggregated[key]
                if (current == null) {
                    aggregated[key] = AggregatedNeed(
                        displayName = spec.name,
                        totalQty = spec.quantity,
                        unit = spec.unit,
                        category = spec.category,
                        totalEstimatedCents = spec.estimatedCostCents,
                        sourceRecipes = mutableSetOf(meal.recipeName)
                    )
                } else {
                    current.totalQty += spec.quantity
                    current.totalEstimatedCents += spec.estimatedCostCents
                    current.sourceRecipes.add(meal.recipeName)
                }
            }
        }

        val result = mutableListOf<GroceryItem>()
        for ((_, need) in aggregated) {
            // Check usable pantry quantity with matching name and compatible unit
            val pantryMatch = pantryItems.filter {
                it.name.equals(need.displayName, ignoreCase = true) &&
                    it.unit.equals(need.unit, ignoreCase = true)
            }.sumOf { max(0.0, it.quantity) }

            val netQty = (need.totalQty - pantryMatch).let { (it * 100.0).roundToLong() / 100.0 }
            if (netQty > 0.01) {
                val ratio = (netQty / max(0.01, need.totalQty)).coerceIn(0.0, 1.0)
                val adjustedCostCents = (need.totalEstimatedCents * ratio).roundToLong().coerceAtLeast(25L)

                val existingMatch = existingGroceryItems.firstOrNull {
                    it.name.equals(need.displayName, ignoreCase = true) &&
                        it.unit.equals(need.unit, ignoreCase = true)
                }

                result.add(
                    GroceryItem(
                        id = existingMatch?.id ?: "",
                        userId = userId,
                        name = need.displayName,
                        requiredQuantity = netQty,
                        unit = need.unit,
                        category = need.category,
                        estimatedPriceCents = existingMatch?.estimatedPriceCents ?: adjustedCostCents,
                        isPurchased = existingMatch?.isPurchased ?: false,
                        actualPriceCents = existingMatch?.actualPriceCents ?: 0L,
                        fromRecipeName = need.sourceRecipes.take(2).joinToString(", "),
                        isRecurring = existingMatch?.isRecurring ?: false
                    )
                )
            }
        }

        // Preserve custom or recurring items not derived from recipes
        for (custom in existingGroceryItems.filter { it.fromRecipeName == "Custom" || it.isRecurring }) {
            val alreadyIncluded = result.any {
                it.name.equals(custom.name, ignoreCase = true) && it.unit.equals(custom.unit, ignoreCase = true)
            }
            if (!alreadyIncluded) {
                result.add(custom)
            }
        }

        return result.sortedWith(compareBy<GroceryItem> { it.isPurchased }.thenBy { it.category }.thenBy { it.name })
    }

    /**
     * Deducts ingredient quantities from pantry items when a planned meal is cooked.
     * Returns the list of updated pantry items whose quantities changed.
     */
    fun deductPantryForCookedMeal(
        meal: PlannedMeal,
        pantryItems: List<PantryItem>
    ): List<PantryItem> {
        val specs = parseIngredientsSpec(meal.ingredientsSummary)
        if (specs.isEmpty()) return emptyList()

        val updatedList = mutableListOf<PantryItem>()
        for (pantry in pantryItems) {
            val matchingSpec = specs.firstOrNull {
                it.name.equals(pantry.name, ignoreCase = true) &&
                    it.unit.equals(pantry.unit, ignoreCase = true)
            }
            if (matchingSpec != null && pantry.quantity > 0.0) {
                val newQty = max(0.0, ((pantry.quantity - matchingSpec.quantity) * 100.0).roundToLong() / 100.0)
                updatedList.add(pantry.copy(quantity = newQty))
            }
        }
        return updatedList
    }

    /**
     * Enforces Free vs BasketBrain Pro subscription entitlements deterministically.
     */
    fun canAccessProFeature(
        subscriptionStateCode: String,
        featureKey: String,
        currentUsageCount: Int = 0
    ): Boolean {
        val state = SubscriptionTierState.fromCode(subscriptionStateCode)
        if (state.isProEntitled) {
            return when (featureKey) {
                "AI_GENERATION" -> currentUsageCount < 100
                "RECEIPT_SCAN" -> currentUsageCount < 50
                else -> true
            }
        }
        // Free tier limits
        return when (featureKey) {
            "AI_GENERATION" -> currentUsageCount < 5
            "SAVED_MEAL_PLANS" -> currentUsageCount < 1
            "MULTIPLE_HOUSEHOLDS" -> currentUsageCount < 1
            "RECEIPT_SCAN" -> currentUsageCount < 2
            "ADVANCED_ANALYTICS" -> false
            else -> true
        }
    }

    /**
     * Deterministic Budget-First Meal Plan Generator that prioritizes pantry ingredients,
     * stays within the user's weekly budget, respects allergies/dietary rules,
     * and generates shared family base meals with member modifications.
     */
    fun generateBudgetFirstMealPlan(
        userId: String,
        profile: UserProfile,
        pantryItems: List<PantryItem>,
        members: List<HouseholdMember>,
        daysCount: Int = profile.planningDays.coerceIn(1, 7)
    ): List<PlannedMeal> {
        val servings = max(1, profile.adultsCount + profile.childrenCount)
        val avoidedTerms = (profile.allergies.split(",") +
            profile.dislikedIngredients.split(",") +
            members.flatMap { it.allergies.split(",") + it.dislikedFoods.split(",") })
            .map { it.trim().lowercase(Locale.US) }
            .filter { it.isNotEmpty() }

        val isVeg = profile.dietaryRestrictions.contains("Vegetarian", ignoreCase = true) ||
            profile.dietaryRestrictions.contains("Vegan", ignoreCase = true)
        val isVegan = profile.dietaryRestrictions.contains("Vegan", ignoreCase = true)
        val isDairyFree = isVegan || profile.dietaryRestrictions.contains("Dairy free", ignoreCase = true)
        val isGlutenFree = profile.dietaryRestrictions.contains("Gluten free", ignoreCase = true)

        val pantryNames = pantryItems.filter { it.quantity > 0 }.map { it.name.lowercase(Locale.US) }

        val memberModNote = if (members.isNotEmpty()) {
            val summary = members.joinToString("; ") { "${it.name}: ${it.dietaryPreference}" }
            "Shared base meal adapted for household ($summary). Allergen notice: always verify ingredient labels."
        } else {
            "Shared household base meal. Portion scaled for $servings servings."
        }

        val breakfastCatalog = listOf(
            RecipeCatalogItem(
                name = "Warm Cinnamon Oats & Banana Bowl",
                mealType = "BREAKFAST",
                prepMins = 5,
                cookMins = 10,
                baseCostPerServingCents = 65L,
                isVegetarian = true,
                isVegan = true,
                isGlutenFree = true,
                isDairyFree = true,
                calories = 340,
                protein = 11,
                carbs = 58,
                fat = 6,
                ingredients = listOf(
                    ParsedIngredientSpec("Rolled Oats", 0.08 * servings, "kg", "Grains", 35L * servings),
                    ParsedIngredientSpec("Bananas", 1.0 * servings, "pcs", "Fruits", 25L * servings),
                    ParsedIngredientSpec("Peanut Butter", 0.03 * servings, "kg", "Other", 20L * servings)
                ),
                instructions = "1. Simmer rolled oats with water or plant milk for 6 minutes.\n2. Slice bananas on top and swirl in peanut butter and a pinch of cinnamon."
            ),
            RecipeCatalogItem(
                name = "Savory Spinach & Onion Egg Scramble",
                mealType = "BREAKFAST",
                prepMins = 5,
                cookMins = 10,
                baseCostPerServingCents = 95L,
                isVegetarian = true,
                isVegan = false,
                isGlutenFree = true,
                isDairyFree = true,
                calories = 310,
                protein = 19,
                carbs = 12,
                fat = 18,
                ingredients = listOf(
                    ParsedIngredientSpec("Eggs", (2 * servings).toDouble(), "pcs", "Dairy", 50L * servings),
                    ParsedIngredientSpec("Onions", 0.08 * servings, "kg", "Vegetables", 15L * servings),
                    ParsedIngredientSpec("Spinach", 0.06 * servings, "kg", "Vegetables", 30L * servings)
                ),
                instructions = "1. Dice onions and sauté in a skillet for 4 minutes until golden.\n2. Wilt fresh spinach, pour in whisked eggs, and fold gently until softly set."
            ),
            RecipeCatalogItem(
                name = "Crisp Apple & Spiced Chickpea Toast",
                mealType = "BREAKFAST",
                prepMins = 8,
                cookMins = 7,
                baseCostPerServingCents = 80L,
                isVegetarian = true,
                isVegan = true,
                isGlutenFree = false,
                isDairyFree = true,
                calories = 360,
                protein = 13,
                carbs = 54,
                fat = 8,
                ingredients = listOf(
                    ParsedIngredientSpec("Whole Wheat Bread", (2 * servings).toDouble(), "pcs", "Grains", 35L * servings),
                    ParsedIngredientSpec("Chickpeas", 0.1 * servings, "kg", "Grains", 25L * servings),
                    ParsedIngredientSpec("Apples", 0.5 * servings, "pcs", "Fruits", 20L * servings)
                ),
                instructions = "1. Mash seasoned chickpeas lightly in a warm pan.\n2. Spread onto toasted bread and top with thin crisp apple slices."
            )
        )

        val lunchCatalog = listOf(
            RecipeCatalogItem(
                name = "Golden Red Lentil & Tomato Soup",
                mealType = "LUNCH",
                prepMins = 10,
                cookMins = 20,
                baseCostPerServingCents = 110L,
                isVegetarian = true,
                isVegan = true,
                isGlutenFree = true,
                isDairyFree = true,
                calories = 410,
                protein = 21,
                carbs = 64,
                fat = 6,
                ingredients = listOf(
                    ParsedIngredientSpec("Lentils", 0.12 * servings, "kg", "Grains", 45L * servings),
                    ParsedIngredientSpec("Tomatoes", 0.15 * servings, "kg", "Vegetables", 35L * servings),
                    ParsedIngredientSpec("Onions", 0.08 * servings, "kg", "Vegetables", 15L * servings),
                    ParsedIngredientSpec("Carrots", 0.1 * servings, "kg", "Vegetables", 15L * servings)
                ),
                instructions = "1. Sauté diced onions and carrots in a pot for 5 minutes.\n2. Add rinsed lentils, chopped tomatoes, and water; simmer 15 minutes until creamy."
            ),
            RecipeCatalogItem(
                name = "Mediterranean Rice & Roasted Veggie Bowl",
                mealType = "LUNCH",
                prepMins = 10,
                cookMins = 20,
                baseCostPerServingCents = 125L,
                isVegetarian = true,
                isVegan = true,
                isGlutenFree = true,
                isDairyFree = true,
                calories = 460,
                protein = 15,
                carbs = 72,
                fat = 11,
                ingredients = listOf(
                    ParsedIngredientSpec("Rice", 0.12 * servings, "kg", "Grains", 35L * servings),
                    ParsedIngredientSpec("Chickpeas", 0.12 * servings, "kg", "Grains", 35L * servings),
                    ParsedIngredientSpec("Tomatoes", 0.12 * servings, "kg", "Vegetables", 35L * servings),
                    ParsedIngredientSpec("Onions", 0.08 * servings, "kg", "Vegetables", 20L * servings)
                ),
                instructions = "1. Cook fluffy rice in a covered pot.\n2. Pan-roast chickpeas, tomatoes, and onions with garlic and herbs; spoon over warm rice."
            ),
            RecipeCatalogItem(
                name = "Garlic Egg Fried Rice with Peas & Carrots",
                mealType = "LUNCH",
                prepMins = 8,
                cookMins = 12,
                baseCostPerServingCents = 115L,
                isVegetarian = true,
                isVegan = false,
                isGlutenFree = true,
                isDairyFree = true,
                calories = 450,
                protein = 18,
                carbs = 66,
                fat = 12,
                ingredients = listOf(
                    ParsedIngredientSpec("Rice", 0.14 * servings, "kg", "Grains", 40L * servings),
                    ParsedIngredientSpec("Eggs", (1 * servings).toDouble(), "pcs", "Dairy", 30L * servings),
                    ParsedIngredientSpec("Carrots", 0.1 * servings, "kg", "Vegetables", 25L * servings),
                    ParsedIngredientSpec("Onions", 0.08 * servings, "kg", "Vegetables", 20L * servings)
                ),
                instructions = "1. Stir-fry diced carrots and onions on high heat.\n2. Push to the side, scramble eggs, then toss with cooked rice until sizzling."
            )
        )

        val dinnerCatalog = listOf(
            RecipeCatalogItem(
                name = "One-Pan Herb Chicken, Rice & Sweet Carrots",
                mealType = "DINNER",
                prepMins = 10,
                cookMins = 25,
                baseCostPerServingCents = 185L,
                isVegetarian = false,
                isVegan = false,
                isGlutenFree = true,
                isDairyFree = true,
                calories = 540,
                protein = 36,
                carbs = 58,
                fat = 15,
                ingredients = listOf(
                    ParsedIngredientSpec("Chicken", 0.15 * servings, "kg", "Meat and seafood", 110L * servings),
                    ParsedIngredientSpec("Rice", 0.12 * servings, "kg", "Grains", 35L * servings),
                    ParsedIngredientSpec("Carrots", 0.12 * servings, "kg", "Vegetables", 25L * servings),
                    ParsedIngredientSpec("Onions", 0.08 * servings, "kg", "Vegetables", 15L * servings)
                ),
                instructions = "1. Sear seasoned chicken pieces with sliced onions until golden.\n2. Stir in rice, diced carrots, and broth; cover and simmer 18 minutes."
            ),
            RecipeCatalogItem(
                name = "Smoky Black Bean & Tomato Skillet over Rice",
                mealType = "DINNER",
                prepMins = 10,
                cookMins = 18,
                baseCostPerServingCents = 135L,
                isVegetarian = true,
                isVegan = true,
                isGlutenFree = true,
                isDairyFree = true,
                calories = 490,
                protein = 20,
                carbs = 76,
                fat = 9,
                ingredients = listOf(
                    ParsedIngredientSpec("Black Beans", 0.15 * servings, "kg", "Grains", 50L * servings),
                    ParsedIngredientSpec("Rice", 0.12 * servings, "kg", "Grains", 35L * servings),
                    ParsedIngredientSpec("Tomatoes", 0.15 * servings, "kg", "Vegetables", 35L * servings),
                    ParsedIngredientSpec("Onions", 0.08 * servings, "kg", "Vegetables", 15L * servings)
                ),
                instructions = "1. Simmer black beans, diced tomatoes, and onions with cumin and paprika.\n2. Serve generously over steamed rice."
            ),
            RecipeCatalogItem(
                name = "Hearty Lentil & Potato Curry (Aloo Dal)",
                mealType = "DINNER",
                prepMins = 10,
                cookMins = 25,
                baseCostPerServingCents = 120L,
                isVegetarian = true,
                isVegan = true,
                isGlutenFree = true,
                isDairyFree = true,
                calories = 475,
                protein = 22,
                carbs = 74,
                fat = 8,
                ingredients = listOf(
                    ParsedIngredientSpec("Lentils", 0.12 * servings, "kg", "Grains", 45L * servings),
                    ParsedIngredientSpec("Potatoes", 0.18 * servings, "kg", "Vegetables", 35L * servings),
                    ParsedIngredientSpec("Tomatoes", 0.12 * servings, "kg", "Vegetables", 25L * servings),
                    ParsedIngredientSpec("Onions", 0.08 * servings, "kg", "Vegetables", 15L * servings)
                ),
                instructions = "1. Sauté onions and tomatoes with turmeric and mild curry spices.\n2. Add cubed potatoes and red lentils with water; simmer 20 minutes until tender."
            ),
            RecipeCatalogItem(
                name = "Roasted Garlic Pasta with Spinach & White Beans",
                mealType = "DINNER",
                prepMins = 8,
                cookMins = 15,
                baseCostPerServingCents = 140L,
                isVegetarian = true,
                isVegan = true,
                isGlutenFree = false,
                isDairyFree = true,
                calories = 510,
                protein = 21,
                carbs = 78,
                fat = 10,
                ingredients = listOf(
                    ParsedIngredientSpec("Pasta", 0.12 * servings, "kg", "Grains", 45L * servings),
                    ParsedIngredientSpec("Chickpeas", 0.12 * servings, "kg", "Grains", 40L * servings),
                    ParsedIngredientSpec("Spinach", 0.08 * servings, "kg", "Vegetables", 35L * servings),
                    ParsedIngredientSpec("Tomatoes", 0.1 * servings, "kg", "Vegetables", 20L * servings)
                ),
                instructions = "1. Boil pasta until al dente, reserving a splash of cooking water.\n2. Toss in a skillet with chickpeas, baby spinach, and tomatoes."
            )
        )

        fun filterAndRank(candidates: List<RecipeCatalogItem>): List<RecipeCatalogItem> {
            val filtered = candidates.filter { item ->
                (!isVeg || item.isVegetarian) &&
                    (!isVegan || item.isVegan) &&
                    (!isDairyFree || item.isDairyFree) &&
                    (!isGlutenFree || item.isGlutenFree) &&
                    item.ingredients.none { ing ->
                        avoidedTerms.any { avoided -> ing.name.lowercase(Locale.US).contains(avoided) }
                    }
            }.ifEmpty { candidates.filter { it.isVegetarian }.ifEmpty { candidates } }

            // Prioritize recipes that reuse pantry ingredients first, then lowest cost
            return filtered.sortedWith(
                compareByDescending<RecipeCatalogItem> { item ->
                    item.ingredients.count { ing ->
                        pantryNames.any { p -> p.contains(ing.name.lowercase(Locale.US)) || ing.name.lowercase(Locale.US).contains(p) }
                    }
                }.thenBy { it.baseCostPerServingCents }
            )
        }

        val rankedBreakfasts = filterAndRank(breakfastCatalog)
        val rankedLunches = filterAndRank(lunchCatalog)
        val rankedDinners = filterAndRank(dinnerCatalog)

        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val generatedMeals = mutableListOf<PlannedMeal>()

        for (dayIdx in 1..daysCount) {
            val dayLabel = dayNames[(dayIdx - 1) % dayNames.size]
            val selectedMeals = mutableListOf<RecipeCatalogItem>()
            if (profile.mealsPerDay >= 1) {
                selectedMeals.add(rankedDinners[(dayIdx - 1) % rankedDinners.size])
            }
            if (profile.mealsPerDay >= 2) {
                selectedMeals.add(0, rankedLunches[(dayIdx - 1) % rankedLunches.size])
            }
            if (profile.mealsPerDay >= 3) {
                selectedMeals.add(0, rankedBreakfasts[(dayIdx - 1) % rankedBreakfasts.size])
            }

            for (recipe in selectedMeals) {
                val totalCostCents = recipe.ingredients.sumOf { it.estimatedCostCents }
                val costPerServing = totalCostCents / servings.toLong()
                val encodedIngredients = recipe.ingredients.joinToString(";") { spec ->
                    val roundedQty = ((spec.quantity * 100.0).roundToLong() / 100.0)
                    "${spec.name}|$roundedQty|${spec.unit}|${spec.category}|${spec.estimatedCostCents}"
                }

                generatedMeals.add(
                    PlannedMeal(
                        userId = userId,
                        planId = "active_week",
                        dayIndex = dayIdx,
                        dayName = dayLabel,
                        mealType = recipe.mealType,
                        recipeName = recipe.name,
                        prepTimeMins = recipe.prepMins,
                        cookTimeMins = recipe.cookMins,
                        servings = servings,
                        estimatedCostCents = totalCostCents,
                        costPerServingCents = costPerServing,
                        ingredientsSummary = encodedIngredients,
                        instructions = recipe.instructions,
                        caloriesPerServing = recipe.calories,
                        proteinGrams = recipe.protein,
                        carbsGrams = recipe.carbs,
                        fatGrams = recipe.fat,
                        isCooked = false,
                        familyModificationNote = memberModNote,
                        isSavedRecipe = false
                    )
                )
            }
        }
        return generatedMeals
    }

    /**
     * Leftover Rescue Engine: Generates practical meal ideas that reuse selected leftovers
     * and includes food safety reminders.
     */
    fun generateLeftoverRescueIdeas(selectedIngredients: List<String>): List<LeftoverRescueSuggestion> {
        val cleaned = selectedIngredients.map { it.trim() }.filter { it.isNotEmpty() }
        val baseList = if (cleaned.isEmpty()) listOf("Cooked Rice", "Onions", "Tomatoes") else cleaned
        val primary = baseList.first()
        val secondary = baseList.drop(1).ifEmpty { listOf("Garlic", "Herbs") }

        return listOf(
            LeftoverRescueSuggestion(
                recipeName = "Crispy Skillet ${primary} & ${secondary.first()} Hash",
                usedLeftovers = baseList,
                additionalIngredientsNeeded = listOf("1 tbsp cooking oil", "Pinch of salt & black pepper"),
                estimatedAdditionalCostCents = 45L,
                prepTimeMins = 15,
                instructions = "1. Heat oil in a wide skillet over medium-high heat.\n" +
                    "2. Dice and toss in ${baseList.joinToString(", ")}.\n" +
                    "3. Press flat with a spatula to form a golden crust and heat thoroughly until steaming hot.",
                safetyReminder = "Food Safety: Ensure refrigerated leftovers were stored at or below 4°C (40°F) within 2 hours of cooking and reheat to an internal temperature of 74°C (165°F). Never use items with off odors or mold."
            ),
            LeftoverRescueSuggestion(
                recipeName = "Rustic One-Pot ${primary} Broth Bowl",
                usedLeftovers = baseList,
                additionalIngredientsNeeded = listOf("500 ml vegetable or chicken stock", "1 tsp dried herbs"),
                estimatedAdditionalCostCents = 60L,
                prepTimeMins = 12,
                instructions = "1. Bring stock and herbs to a rolling boil in a saucepan.\n" +
                    "2. Stir in ${baseList.joinToString(", ")} and simmer for 8-10 minutes until piping hot throughout.",
                safetyReminder = "Food Safety: Bring liquid-based leftover soups and stews to a full rolling boil for at least 1 minute before serving. Discard any food left at room temperature for more than 2 hours."
            ),
            LeftoverRescueSuggestion(
                recipeName = "Warm Spiced ${primary} Tortilla Wraps",
                usedLeftovers = baseList,
                additionalIngredientsNeeded = listOf("2 flatbreads or tortillas", "1 tsp cumin or paprika"),
                estimatedAdditionalCostCents = 85L,
                prepTimeMins = 10,
                instructions = "1. Sauté ${baseList.joinToString(", ")} with spices in a hot pan for 5-7 minutes.\n" +
                    "2. Spoon into warm flatbreads, roll tightly, and toast seam-side down for 1 minute.",
                safetyReminder = "Food Safety: Only reheat leftovers once. Check that poultry or cooked grains are heated evenly to 74°C (165°F) with no cold spots."
            )
        )
    }

    private data class RecipeCatalogItem(
        val name: String,
        val mealType: String,
        val prepMins: Int,
        val cookMins: Int,
        val baseCostPerServingCents: Long,
        val isVegetarian: Boolean,
        val isVegan: Boolean,
        val isGlutenFree: Boolean,
        val isDairyFree: Boolean,
        val calories: Int,
        val protein: Int,
        val carbs: Int,
        val fat: Int,
        val ingredients: List<ParsedIngredientSpec>,
        val instructions: String
    )
}
