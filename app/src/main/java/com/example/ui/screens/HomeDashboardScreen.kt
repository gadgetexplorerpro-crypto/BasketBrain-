package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BudgetSummary
import com.example.data.model.GroceryItem
import com.example.data.model.PantryItem
import com.example.data.model.PlannedMeal
import com.example.data.model.UserProfile
import com.example.domain.BudgetAndGroceryEngine
import com.example.ui.theme.CitrusOrangeAccent
import com.example.ui.theme.SageGreenSecondary
import com.example.ui.viewmodel.ActiveModalSheet
import com.example.ui.viewmodel.MainNavTab

@Composable
fun HomeDashboardScreen(
    profile: UserProfile,
    budgetSummary: BudgetSummary,
    plannedMeals: List<PlannedMeal>,
    pantryItems: List<PantryItem>,
    groceryItems: List<GroceryItem>,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateTab: (MainNavTab) -> Unit,
    onOpenSheet: (ActiveModalSheet) -> Unit,
    onGeneratePlan: () -> Unit,
    onCookMeal: (PlannedMeal) -> Unit,
    onSwapMeal: (PlannedMeal) -> Unit
) {
    val currency = profile.currencyCode
    val todayMeals = plannedMeals.filter { it.dayIndex == 1 }.ifEmpty { plannedMeals.take(3) }
    val lowStockItems = pantryItems.filter { it.quantity <= it.lowStockThreshold }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BasketBrain • ${profile.householdName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Good morning! Let's make your groceries go further.",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onOpenSheet(ActiveModalSheet.AI_ASSISTANT) },
                        modifier = Modifier.testTag("open_ai_assistant_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "BasketBrain Assistant",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme"
                        )
                    }
                }
            }
        }

        // Hero Fintech Grocery Budget Ring Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_budget_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WEEKLY GROCERY BUDGET",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = BudgetAndGroceryEngine.formatCurrency(budgetSummary.totalBudgetCents, currency),
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            BudgetStatLine(
                                label = "Spent",
                                value = BudgetAndGroceryEngine.formatCurrency(budgetSummary.spentCents, currency),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            BudgetStatLine(
                                label = "Remaining",
                                value = BudgetAndGroceryEngine.formatCurrency(budgetSummary.remainingCents, currency),
                                color = if (budgetSummary.isOverBudget) Color(0xFFFF8A80) else SageGreenSecondary
                            )
                            BudgetStatLine(
                                label = "Est. Shopping List",
                                value = BudgetAndGroceryEngine.formatCurrency(
                                    budgetSummary.estimatedShoppingListCostCents,
                                    currency
                                ),
                                color = CitrusOrangeAccent
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Budget Ring Progress Chart
                        Box(
                            modifier = Modifier.size(116.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val progress = (budgetSummary.usagePercentage / 100f).coerceIn(0f, 1f)
                            val ringColor = when {
                                budgetSummary.isOverBudget -> Color(0xFFFF5252)
                                budgetSummary.usagePercentage >= 80 -> CitrusOrangeAccent
                                else -> SageGreenSecondary
                            }
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 12.dp.toPx()
                                drawArc(
                                    color = Color.White.copy(alpha = 0.2f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = ringColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * progress,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${budgetSummary.usagePercentage}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Used",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Mini-Pill Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiniMetricPill("${budgetSummary.daysRemainingInWeek}d left in week")
                        MiniMetricPill("${plannedMeals.size} meals planned")
                        MiniMetricPill("${pantryItems.size} pantry items")
                    }
                }
            }
        }

        // Alerts Section (Overrun or Pantry Low Stock / Expiry)
        if (budgetSummary.isOverBudget || lowStockItems.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Alert",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (budgetSummary.isOverBudget) {
                                Text(
                                    text = "Budget Alert: You are over your target budget by ${
                                        BudgetAndGroceryEngine.formatCurrency(
                                            -budgetSummary.remainingCents,
                                            currency
                                        )
                                    }.",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            if (lowStockItems.isNotEmpty()) {
                                Text(
                                    text = "Pantry Alert: ${lowStockItems.joinToString { it.name }} running low. Check expiry dates & rescue leftovers!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Grid (6 Required Actions + Analytics)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge
                )
                AssistChip(
                    onClick = { onOpenSheet(ActiveModalSheet.SPENDING_ANALYTICS) },
                    label = { Text("Analytics") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("quick_action_analytics")
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.AutoAwesome,
                        title = "Generate Meal Plan",
                        subtitle = "AI Budget-First",
                        tag = "quick_action_generate_plan",
                        modifier = Modifier.weight(1f),
                        onClick = onGeneratePlan
                    )
                    QuickActionCard(
                        icon = Icons.Default.AddShoppingCart,
                        title = "Add Expense",
                        subtitle = "Log spending",
                        tag = "quick_action_add_expense",
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenSheet(ActiveModalSheet.ADD_EXPENSE) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.DocumentScanner,
                        title = "Scan Receipt",
                        subtitle = "AI Vision OCR",
                        tag = "quick_action_scan_receipt",
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenSheet(ActiveModalSheet.SCAN_RECEIPT) }
                    )
                    QuickActionCard(
                        icon = Icons.Default.Kitchen,
                        title = "Add Pantry Item",
                        subtitle = "${pantryItems.size} in stock",
                        tag = "quick_action_add_pantry",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(MainNavTab.PANTRY) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.LocalOffer,
                        title = "Find Cheap Meals",
                        subtitle = "Under $2/serving",
                        tag = "quick_action_cheap_meals",
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenSheet(ActiveModalSheet.RECIPE_DISCOVERY) }
                    )
                    QuickActionCard(
                        icon = Icons.Default.Recycling,
                        title = "Use My Leftovers",
                        subtitle = "Zero food waste",
                        tag = "quick_action_leftovers",
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenSheet(ActiveModalSheet.LEFTOVER_RESCUE) }
                    )
                }
            }
        }

        // Today's Planned Meals Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Planned Meals",
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedButton(
                    onClick = { onNavigateTab(MainNavTab.MEAL_PLAN) }
                ) {
                    Text("Full Week (${plannedMeals.size})")
                }
            }
        }

        if (todayMeals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No meals planned yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Generate an affordable 7-day plan tailored to your budget and pantry.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onGeneratePlan) {
                            Text("Generate 7-Day Budget Plan")
                        }
                    }
                }
            }
        } else {
            items(todayMeals, key = { it.id.ifEmpty { it.recipeName + it.mealType } }) { meal ->
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
                                    text = "${meal.mealType} • ${meal.prepTimeMins + meal.cookTimeMins} mins",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "${BudgetAndGroceryEngine.formatCurrency(meal.costPerServingCents, currency)}/serving",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = meal.recipeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = BudgetAndGroceryEngine.formatIngredientsReadable(
                                meal.ingredientsSummary,
                                profile.measurementSystem
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onCookMeal(meal) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (meal.isCooked) "Cooked ✓" else "Cook This")
                            }
                            OutlinedButton(
                                onClick = { onSwapMeal(meal) }
                            ) {
                                Icon(
                                    Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Swap")
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun BudgetStatLine(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MiniMetricPill(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
