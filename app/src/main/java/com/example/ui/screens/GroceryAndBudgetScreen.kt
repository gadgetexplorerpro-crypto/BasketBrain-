package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.BasketBrainCatalog
import com.example.data.model.BudgetSummary
import com.example.data.model.GroceryItem
import com.example.data.model.PurchaseRecord
import com.example.data.model.UserProfile
import com.example.domain.BudgetAndGroceryEngine
import com.example.ui.viewmodel.ActiveModalSheet
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroceryAndBudgetScreen(
    profile: UserProfile,
    budgetSummary: BudgetSummary,
    groceryItems: List<GroceryItem>,
    purchases: List<PurchaseRecord>,
    onSyncFromMealPlan: () -> Unit,
    onAddCustomGroceryItem: (String, Double, String, String, Long, Boolean) -> Unit,
    onUpdateGroceryItem: (GroceryItem, Double, Long) -> Unit,
    onMarkPurchased: (GroceryItem, Long?, String) -> Unit,
    onDeleteGroceryItem: (String) -> Unit,
    onDeletePurchase: (String) -> Unit,
    onOpenSheet: (ActiveModalSheet) -> Unit
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Smart Grocery List, 1 = Budget & Spending Engine
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var editingGroceryItem by remember { mutableStateOf<GroceryItem?>(null) }
    var confirmingPurchaseItem by remember { mutableStateOf<GroceryItem?>(null) }

    val currency = profile.currencyCode
    val filteredGroceries = remember(groceryItems, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") groceryItems
        else groceryItems.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
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
                text = "Smart Grocery & Budget Engine",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    label = { Text("Smart Grocery List (${groceryItems.size})") },
                    modifier = Modifier.testTag("subtab_grocery_list")
                )
                FilterChip(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    label = { Text("Budget & Transactions (${purchases.size})") },
                    modifier = Modifier.testTag("subtab_budget_engine")
                )
            }
        }

        if (activeSubTab == 0) {
            // Smart Grocery List Header Card
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
                                    text = "Estimated Shopping Cost",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = BudgetAndGroceryEngine.formatCurrency(
                                        budgetSummary.estimatedShoppingListCostCents,
                                        currency
                                    ),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Duplicates combined & pantry stock automatically subtracted",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAddItemDialog = true },
                                modifier = Modifier.testTag("add_grocery_item_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Item")
                            }
                            OutlinedButton(onClick = onSyncFromMealPlan) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync Plan")
                            }
                            OutlinedButton(
                                onClick = {
                                    val shareText = buildString {
                                        appendLine("BasketBrain Smart Grocery List (${profile.householdName})")
                                        appendLine(
                                            "Estimated Total: ${
                                                BudgetAndGroceryEngine.formatCurrency(
                                                    budgetSummary.estimatedShoppingListCostCents,
                                                    currency
                                                )
                                            }"
                                        )
                                        appendLine("---")
                                        groceryItems.forEach { item ->
                                            val status = if (item.isPurchased) "[x]" else "[ ]"
                                            appendLine(
                                                "$status ${item.name} — ${item.requiredQuantity} ${item.unit} (${
                                                    BudgetAndGroceryEngine.formatCurrency(item.estimatedPriceCents, currency)
                                                })"
                                            )
                                        }
                                    }
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Grocery List"))
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share List")
                            }
                        }
                    }
                }
            }

            // Category Filter Row
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (listOf("All") + BasketBrainCatalog.spendingCategories).forEach { cat ->
                        AssistChip(
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            if (filteredGroceries.isEmpty()) {
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
                            Text("Your grocery list is empty", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Tap 'Sync Plan' to combine ingredients from your planned meals or add custom items.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredGroceries, key = { it.id.ifEmpty { it.name + it.unit } }) { item ->
                    val (dispQty, dispUnit) = BudgetAndGroceryEngine.convertUnitIfNeeded(
                        item.requiredQuantity,
                        item.unit,
                        profile.measurementSystem
                    )
                    val qtyFormatted = if (dispQty % 1.0 == 0.0) dispQty.toInt().toString()
                    else String.format(Locale.US, "%.1f", dispQty)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    if (!item.isPurchased) {
                                        confirmingPurchaseItem = item
                                    } else {
                                        onMarkPurchased(item, null, "")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (item.isPurchased) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = "Mark purchased",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.name} — $qtyFormatted $dispUnit",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (item.isPurchased) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Text(
                                    text = "${item.category} • Source: ${item.fromRecipeName}${if (item.isRecurring) " • Recurring" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = BudgetAndGroceryEngine.formatCurrency(item.estimatedPriceCents, currency),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Row {
                                    IconButton(
                                        onClick = { editingGroceryItem = item },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit price or quantity",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    if (item.id.isNotBlank()) {
                                        IconButton(
                                            onClick = { onDeleteGroceryItem(item.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Delete item",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Budget & Spending Engine View
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Planned vs. Actual Spending (${profile.budgetCycle})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Budget: ${BudgetAndGroceryEngine.formatCurrency(budgetSummary.totalBudgetCents, currency)} • " +
                                "Actual Spent: ${BudgetAndGroceryEngine.formatCurrency(budgetSummary.spentCents, currency)} • " +
                                "Remaining: ${BudgetAndGroceryEngine.formatCurrency(budgetSummary.remainingCents, currency)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onOpenSheet(ActiveModalSheet.ADD_EXPENSE) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Record Purchase")
                            }
                            OutlinedButton(
                                onClick = { onOpenSheet(ActiveModalSheet.SCAN_RECEIPT) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scan Receipt")
                            }
                        }
                    }
                }
            }

            // Category Breakdown Bars
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Spending by Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val totalSpentNonZero = budgetSummary.spentCents.coerceAtLeast(1L)
                        BasketBrainCatalog.spendingCategories.forEach { cat ->
                            val catSpent = budgetSummary.categorySpendingCents[cat] ?: 0L
                            if (catSpent > 0L || cat in listOf("Vegetables", "Grains", "Dairy", "Meat and seafood")) {
                                val ratio = (catSpent.toFloat() / totalSpentNonZero.toFloat()).coerceIn(0f, 1f)
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(cat, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            BudgetAndGroceryEngine.formatCurrency(catSpent, currency),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { ratio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Purchase History
            item {
                Text(
                    text = "Purchase History (${purchases.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (purchases.isEmpty()) {
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
                            Text("No grocery purchases recorded yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Record a purchase or scan a grocery receipt to update your remaining weekly budget.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(purchases, key = { it.id.ifEmpty { it.storeName + it.purchaseDate + it.totalAmountCents } }) { purchase ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = purchase.storeName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (purchase.isFromReceipt) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Receipt OCR",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${purchase.purchaseDate} • ${purchase.category}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = purchase.itemsSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = BudgetAndGroceryEngine.formatCurrency(purchase.totalAmountCents, currency),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (purchase.id.isNotBlank()) {
                                    IconButton(onClick = { onDeletePurchase(purchase.id) }) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete transaction"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Add Custom Grocery Item Dialog
    if (showAddItemDialog) {
        var name by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("1") }
        var unit by remember { mutableStateOf("kg") }
        var category by remember { mutableStateOf("Vegetables") }
        var priceStr by remember { mutableStateOf("2.50") }
        var isRecurring by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Add Grocery Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ingredient Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qty,
                            onValueChange = { qty = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (kg, g, pcs)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Estimated Price (${profile.currencyCode})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it })
                        Text("Save to recurring grocery essentials", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cents = BudgetAndGroceryEngine.parseToCents(priceStr) ?: 250L
                        onAddCustomGroceryItem(
                            name,
                            qty.toDoubleOrNull() ?: 1.0,
                            unit,
                            category,
                            cents,
                            isRecurring
                        )
                        showAddItemDialog = false
                    }
                ) {
                    Text("Add to List")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Edit Grocery Item Estimate Dialog
    editingGroceryItem?.let { item ->
        var qtyInput by remember(item) { mutableStateOf(item.requiredQuantity.toString()) }
        var priceInput by remember(item) {
            mutableStateOf(String.format(Locale.US, "%.2f", item.estimatedPriceCents / 100.0))
        }

        AlertDialog(
            onDismissRequest = { editingGroceryItem = null },
            title = { Text("Edit ${item.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it },
                        label = { Text("Required Quantity (${item.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Estimated Price (${profile.currencyCode})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newQty = qtyInput.toDoubleOrNull() ?: item.requiredQuantity
                        val newCents = BudgetAndGroceryEngine.parseToCents(priceInput) ?: item.estimatedPriceCents
                        onUpdateGroceryItem(item, newQty, newCents)
                        editingGroceryItem = null
                    }
                ) {
                    Text("Save Estimate")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingGroceryItem = null }) { Text("Cancel") }
            }
        )
    }

    // Confirm Purchase & Optional Actual Spending Dialog
    confirmingPurchaseItem?.let { item ->
        var actualPriceInput by remember(item) {
            mutableStateOf(String.format(Locale.US, "%.2f", item.estimatedPriceCents / 100.0))
        }
        var storeInput by remember { mutableStateOf("Local Supermarket") }

        AlertDialog(
            onDismissRequest = { confirmingPurchaseItem = null },
            title = { Text("Mark '${item.name}' Purchased") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Confirm the actual price paid to record this expense in your weekly budget and stock your Pantry, or check off without recording an expense.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = actualPriceInput,
                        onValueChange = { actualPriceInput = it },
                        label = { Text("Actual Price Paid (${profile.currencyCode})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = storeInput,
                        onValueChange = { storeInput = it },
                        label = { Text("Store Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val confirmedCents = BudgetAndGroceryEngine.parseToCents(actualPriceInput) ?: item.estimatedPriceCents
                        onMarkPurchased(item, confirmedCents, storeInput)
                        confirmingPurchaseItem = null
                    }
                ) {
                    Text("Confirm & Log Spend")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onMarkPurchased(item, null, "")
                        confirmingPurchaseItem = null
                    }
                ) {
                    Text("Check Off Only")
                }
            }
        )
    }
}
