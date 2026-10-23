package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BudgetSummary
import com.example.data.model.HouseholdMember
import com.example.data.model.PantryItem
import com.example.data.model.PlannedMeal
import com.example.data.model.UserProfile
import com.example.domain.BudgetAndGroceryEngine
import com.example.ui.viewmodel.ActiveModalSheet

@Composable
fun MealPlanScreen(
    profile: UserProfile,
    budgetSummary: BudgetSummary,
    plannedMeals: List<PlannedMeal>,
    pantryItems: List<PantryItem>,
    members: List<HouseholdMember>,
    isGeneratingAi: Boolean,
    onGeneratePlan: () -> Unit,
    onCookMeal: (PlannedMeal) -> Unit,
    onSwapMeal: (PlannedMeal) -> Unit,
    onToggleSaveRecipe: (PlannedMeal) -> Unit,
    onAddMealToGrocery: (PlannedMeal) -> Unit,
    onOpenSheet: (ActiveModalSheet) -> Unit
) {
    var selectedDayFilter by remember { mutableStateOf("All") }
    val dayOptions = listOf("All", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    val filteredMeals = remember(plannedMeals, selectedDayFilter) {
        if (selectedDayFilter == "All") plannedMeals
        else plannedMeals.filter { it.dayName.equals(selectedDayFilter, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "AI Budget-First Meal Planner",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Prioritizing your ${pantryItems.size} pantry items & ${
                    BudgetAndGroceryEngine.formatCurrency(
                        profile.weeklyBudgetCents,
                        profile.currencyCode
                    )
                }/wk target for ${profile.adultsCount + profile.childrenCount} people.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Planner Summary & Action Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Est. Plan Total: ${
                                    BudgetAndGroceryEngine.formatCurrency(
                                        budgetSummary.plannedMealsTotalCostCents,
                                        profile.currencyCode
                                    )
                                }",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Avg ${
                                    BudgetAndGroceryEngine.formatCurrency(
                                        budgetSummary.averageCostPerServingCents,
                                        profile.currencyCode
                                    )
                                }/serving • Diet: ${profile.dietaryRestrictions}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Button(
                            onClick = onGeneratePlan,
                            enabled = !isGeneratingAi,
                            modifier = Modifier.testTag("generate_meal_plan_button")
                        ) {
                            if (isGeneratingAi) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Regenerate Plan")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { onOpenSheet(ActiveModalSheet.FAMILY_MEMBERS) },
                            label = { Text("Family Members (${members.size})") },
                            leadingIcon = {
                                Icon(Icons.Default.FamilyRestroom, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                        AssistChip(
                            onClick = { onOpenSheet(ActiveModalSheet.RECIPE_DISCOVERY) },
                            label = { Text("Cheap Recipe Library") },
                            leadingIcon = {
                                Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                        AssistChip(
                            onClick = { onOpenSheet(ActiveModalSheet.LEFTOVER_RESCUE) },
                            label = { Text("Rescue Leftovers") },
                            leadingIcon = {
                                Icon(Icons.Default.Recycling, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }
        }

        // Day Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dayOptions.forEach { day ->
                    FilterChip(
                        selected = selectedDayFilter == day,
                        onClick = { selectedDayFilter = day },
                        label = { Text(day) }
                    )
                }
            }
        }

        if (filteredMeals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No planned meals for $selectedDayFilter",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onGeneratePlan) {
                            Text("Generate 7-Day Budget-First Plan")
                        }
                    }
                }
            }
        } else {
            items(filteredMeals, key = { it.id.ifEmpty { "${it.dayName}_${it.mealType}_${it.recipeName}" } }) { meal ->
                PlannedMealDetailCard(
                    meal = meal,
                    currencyCode = profile.currencyCode,
                    measurementSystem = profile.measurementSystem,
                    onCookMeal = { onCookMeal(meal) },
                    onSwapMeal = { onSwapMeal(meal) },
                    onToggleSave = { onToggleSaveRecipe(meal) },
                    onAddToGrocery = { onAddMealToGrocery(meal) },
                    onUseLeftovers = { onOpenSheet(ActiveModalSheet.LEFTOVER_RESCUE) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun PlannedMealDetailCard(
    meal: PlannedMeal,
    currencyCode: String,
    measurementSystem: String,
    onCookMeal: () -> Unit,
    onSwapMeal: () -> Unit,
    onToggleSave: () -> Unit,
    onAddToGrocery: () -> Unit,
    onUseLeftovers: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${meal.dayName} • ${meal.mealType}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${BudgetAndGroceryEngine.formatCurrency(meal.estimatedCostCents, currencyCode)} total (${
                            BudgetAndGroceryEngine.formatCurrency(meal.costPerServingCents, currencyCode)
                        }/srv)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onToggleSave) {
                        Icon(
                            imageVector = if (meal.isSavedRecipe) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Recipe",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                text = meal.recipeName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Prep ${meal.prepTimeMins}m • Cook ${meal.cookTimeMins}m • ${meal.servings} servings • ${meal.caloriesPerServing} kcal (${meal.proteinGrams}g protein)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Ingredients: ${BudgetAndGroceryEngine.formatIngredientsReadable(meal.ingredientsSummary, measurementSystem)}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (meal.familyModificationNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = meal.familyModificationNote,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "Cooking Instructions:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = meal.instructions,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onAddToGrocery) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add to Grocery")
                        }
                        OutlinedButton(onClick = onUseLeftovers) {
                            Icon(Icons.Default.Recycling, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use Leftovers")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onCookMeal) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (meal.isCooked) "Cooked ✓" else "Cook This")
                    }
                    OutlinedButton(onClick = onSwapMeal) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Swap Meal")
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse instructions" else "Expand instructions"
                    )
                }
            }
        }
    }
}
