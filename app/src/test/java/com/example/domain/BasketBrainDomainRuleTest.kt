package com.example.domain

import com.example.data.model.GroceryItem
import com.example.data.model.HouseholdMember
import com.example.data.model.PantryItem
import com.example.data.model.PlannedMeal
import com.example.data.model.PurchaseRecord
import com.example.data.model.UserProfile
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BasketBrainDomainRuleTest {

    @Test
    fun formatCurrency_and_parseToCents_areDeterministic() {
        assertThat(BudgetAndGroceryEngine.formatCurrency(12000L, "USD")).isEqualTo("$120.00")
        assertThat(BudgetAndGroceryEngine.formatCurrency(7340L, "USD")).isEqualTo("$73.40")
        assertThat(BudgetAndGroceryEngine.formatCurrency(-460L, "GBP")).isEqualTo("-£4.60")
        assertThat(BudgetAndGroceryEngine.formatCurrency(150000L, "PKR")).isEqualTo("Rs 1500.00")

        assertThat(BudgetAndGroceryEngine.parseToCents("73.40")).isEqualTo(7340L)
        assertThat(BudgetAndGroceryEngine.parseToCents("100")).isEqualTo(10000L)
    }

    @Test
    fun calculateBudgetSummary_matchesDeterministicMinorUnits() {
        val profile = UserProfile(weeklyBudgetCents = 12000L, budgetCycle = "WEEKLY")
        val purchases = listOf(
            PurchaseRecord(category = "Vegetables", totalAmountCents = 4340L),
            PurchaseRecord(category = "Grains", totalAmountCents = 3000L)
        )
        val meals = listOf(
            PlannedMeal(estimatedCostCents = 600L, servings = 3),
            PlannedMeal(estimatedCostCents = 900L, servings = 3)
        )
        val groceries = listOf(
            GroceryItem(estimatedPriceCents = 1200L, isPurchased = false),
            GroceryItem(estimatedPriceCents = 500L, isPurchased = true)
        )

        val summary = BudgetAndGroceryEngine.calculateBudgetSummary(
            profile = profile,
            purchases = purchases,
            plannedMeals = meals,
            groceryItems = groceries
        )

        assertThat(summary.totalBudgetCents).isEqualTo(12000L)
        assertThat(summary.spentCents).isEqualTo(7340L)
        assertThat(summary.remainingCents).isEqualTo(4660L)
        assertThat(summary.usagePercentage).isEqualTo(61)
        assertThat(summary.isOverBudget).isFalse()
        assertThat(summary.estimatedShoppingListCostCents).isEqualTo(1200L)
        assertThat(summary.averageCostPerMealCents).isEqualTo(750L)
        assertThat(summary.averageCostPerServingCents).isEqualTo(250L)
    }

    @Test
    fun consolidateGroceryList_combinesDuplicatesAndSubtractsPantry() {
        val meals = listOf(
            PlannedMeal(
                recipeName = "Meal 1",
                isCooked = false,
                ingredientsSummary = "Rice|1.0|kg|Grains|200;Tomatoes|0.5|kg|Vegetables|150"
            ),
            PlannedMeal(
                recipeName = "Meal 2",
                isCooked = false,
                ingredientsSummary = "Rice|1.0|kg|Grains|200;Onions|0.5|kg|Vegetables|100"
            )
        )
        val pantry = listOf(
            PantryItem(name = "Rice", quantity = 0.5, unit = "kg", category = "Grains")
        )

        val consolidated = BudgetAndGroceryEngine.consolidateGroceryList(
            userId = "u1",
            plannedMeals = meals,
            pantryItems = pantry
        )

        val riceItem = consolidated.firstOrNull { it.name == "Rice" }
        assertThat(riceItem).isNotNull()
        // 2.0 kg needed - 0.5 kg in pantry = 1.5 kg to buy
        assertThat(riceItem!!.requiredQuantity).isEqualTo(1.5)
        assertThat(consolidated.map { it.name }).containsExactly("Onions", "Rice", "Tomatoes")
    }

    @Test
    fun deductPantryForCookedMeal_reducesPantryQuantitiesSafely() {
        val meal = PlannedMeal(
            recipeName = "Lentil Soup",
            ingredientsSummary = "Lentils|0.4|kg|Grains|150;Onions|0.2|kg|Vegetables|50"
        )
        val pantry = listOf(
            PantryItem(id = "p1", name = "Lentils", quantity = 1.0, unit = "kg"),
            PantryItem(id = "p2", name = "Onions", quantity = 0.1, unit = "kg")
        )

        val deducted = BudgetAndGroceryEngine.deductPantryForCookedMeal(meal, pantry)
        assertThat(deducted).hasSize(2)
        assertThat(deducted.first { it.name == "Lentils" }.quantity).isEqualTo(0.6)
        assertThat(deducted.first { it.name == "Onions" }.quantity).isEqualTo(0.0)
    }

    @Test
    fun subscriptionAccessRules_enforceFreeAndProTiers() {
        assertThat(BudgetAndGroceryEngine.canAccessProFeature("FREE", "ADVANCED_ANALYTICS", 0)).isFalse()
        assertThat(BudgetAndGroceryEngine.canAccessProFeature("ACTIVE", "ADVANCED_ANALYTICS", 0)).isTrue()
        assertThat(BudgetAndGroceryEngine.canAccessProFeature("FREE", "AI_GENERATION", 5)).isFalse()
        assertThat(BudgetAndGroceryEngine.canAccessProFeature("TRIALING", "AI_GENERATION", 5)).isTrue()
    }

    @Test
    fun generateBudgetFirstMealPlan_respectsHouseholdAndAllergies() {
        val profile = UserProfile(
            adultsCount = 2,
            childrenCount = 1,
            mealsPerDay = 3,
            planningDays = 7,
            weeklyBudgetCents = 10000L,
            allergies = "peanut"
        )
        val members = listOf(
            HouseholdMember(name = "Alex", role = "ADULT", dietaryPreference = "High protein"),
            HouseholdMember(name = "Sam", role = "PARTNER", dietaryPreference = "Vegetarian")
        )
        val pantry = listOf(PantryItem(name = "Rice", quantity = 2.0, unit = "kg"))

        val plan = BudgetAndGroceryEngine.generateBudgetFirstMealPlan("u1", profile, pantry, members)
        assertThat(plan).hasSize(21) // 7 days * 3 meals
        assertThat(plan.none { it.ingredientsSummary.contains("Peanut", ignoreCase = true) }).isTrue()
    }
}
