package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.BasketBrainCatalog
import com.example.data.model.HouseholdMember
import com.example.data.model.PantryItem
import com.example.data.model.SubscriptionTierState
import com.example.data.model.UserProfile
import com.example.domain.BudgetAndGroceryEngine
import com.example.ui.viewmodel.ActiveModalSheet

@Composable
fun PantryScreen(
    profile: UserProfile,
    pantryItems: List<PantryItem>,
    onAddPantryItem: (String, Double, String, String, String, String, Long) -> Unit,
    onAdjustQuantity: (PantryItem, Double) -> Unit,
    onDeleteItem: (String) -> Unit,
    onOpenLeftoverRescue: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredPantry = remember(pantryItems, searchQuery, selectedLocation) {
        pantryItems.filter { item ->
            (selectedLocation == "All" || item.storageLocation.equals(selectedLocation, ignoreCase = true)) &&
                (searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = "Pantry & Food Inventory",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${pantryItems.size} items tracked across Refrigerator, Freezer & Cupboard",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_pantry_item_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item")
                }
            }
        }

        // Leftover Rescue Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rescue My Leftovers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Turn cooked rice, half an onion, or extra vegetables into tonight's meal.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onOpenLeftoverRescue) {
                        Icon(Icons.Default.Recycling, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rescue")
                    }
                }
            }
        }

        // Search & Location Filters
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search pantry ingredients...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pantry_search_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (listOf("All") + BasketBrainCatalog.storageLocations).forEach { loc ->
                    FilterChip(
                        selected = selectedLocation == loc,
                        onClick = { selectedLocation = loc },
                        label = { Text(loc) }
                    )
                }
            }
        }

        // Date Label Guidance Note
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Best-before & expiry dates are helpful reminders, not automatic safety guarantees. Always check appearance, smell, and storage temperature before consuming.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (filteredPantry.isEmpty()) {
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
                        Text("No pantry items found", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Add ingredients you own so BasketBrain can subtract them from your grocery list.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredPantry, key = { it.id.ifEmpty { it.name + it.storageLocation } }) { item ->
                val isLowStock = item.quantity <= item.lowStockThreshold
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
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = if (isLowStock) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isLowStock) "Low Stock" else item.storageLocation,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${item.category} • Best before: ${item.expiryDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onAdjustQuantity(item, -0.5) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Consume 0.5")
                            }
                            Text(
                                text = "${item.quantity} ${item.unit}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { onAdjustQuantity(item, 0.5) }) {
                                Icon(Icons.Default.Add, contentDescription = "Add 0.5")
                            }
                            if (item.id.isNotBlank()) {
                                IconButton(onClick = { onDeleteItem(item.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete pantry item")
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("1.0") }
        var unit by remember { mutableStateOf("kg") }
        var category by remember { mutableStateOf("Grains") }
        var storage by remember { mutableStateOf("Cupboard") }
        var expiry by remember { mutableStateOf("2026-11-30") }
        var price by remember { mutableStateOf("0.00") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Pantry Ingredient") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ingredient Name (e.g. Rice, Eggs)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qty,
                            onValueChange = { qty = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (kg, pcs, L)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BasketBrainCatalog.storageLocations.forEach { loc ->
                            FilterChip(
                                selected = storage == loc,
                                onClick = { storage = loc },
                                label = { Text(loc) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = it },
                        label = { Text("Best-Before / Expiry Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddPantryItem(
                            name,
                            qty.toDoubleOrNull() ?: 1.0,
                            unit,
                            category,
                            storage,
                            expiry,
                            BudgetAndGroceryEngine.parseToCents(price) ?: 0L
                        )
                        showAddDialog = false
                    }
                ) {
                    Text("Save to Pantry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileAndSettingsScreen(
    profile: UserProfile,
    members: List<HouseholdMember>,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSaveProfile: (UserProfile) -> Unit,
    onOpenSheet: (ActiveModalSheet) -> Unit,
    onExportData: () -> Unit,
    onDeleteDataOnly: () -> Unit = {},
    onDeleteAccount: () -> Unit,
    onSignOut: () -> Unit
) {
    var householdName by remember(profile) { mutableStateOf(profile.householdName) }
    var budgetStr by remember(profile) {
        mutableStateOf((profile.weeklyBudgetCents / 100L).toString())
    }
    var currencyCode by remember(profile) { mutableStateOf(profile.currencyCode) }
    var measurementSystem by remember(profile) { mutableStateOf(profile.measurementSystem) }
    var dietaryRestrictions by remember(profile) { mutableStateOf(profile.dietaryRestrictions) }
    var allergies by remember(profile) { mutableStateOf(profile.allergies) }
    var notificationsEnabled by remember(profile) { mutableStateOf(profile.notificationsEnabled) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDeleteDataOnlyConfirm by remember { mutableStateOf(false) }
    var activeComplianceTab by remember { mutableStateOf<ComplianceTab?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = granted
    }

    val tierState = SubscriptionTierState.fromCode(profile.subscriptionState)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Settings & Household Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // BasketBrain Pro Subscription Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tierState.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AI Generations Used: ${profile.aiGenerationsUsed}/5 Free • View BasketBrain Pro ($4.99/mo) benefits & entitlement status",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onOpenSheet(ActiveModalSheet.SUBSCRIPTION_PLANS) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Text("Plans")
                    }
                }
            }
        }

        // Family Meal Planning Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Family Members (${members.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configure individual diets, dislikes, and child portions for shared base meals.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = { onOpenSheet(ActiveModalSheet.FAMILY_MEMBERS) }) {
                        Icon(Icons.Default.FamilyRestroom, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Manage")
                    }
                }
            }
        }

        // Household, Budget, Currency & Units Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Budget, Currency & Dietary Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = householdName,
                        onValueChange = { householdName = it },
                        label = { Text("Household Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = budgetStr,
                        onValueChange = { budgetStr = it },
                        label = { Text("Weekly Grocery Budget ($currencyCode)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Currency", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BasketBrainCatalog.currencies.forEach { (code, symbol) ->
                            FilterChip(
                                selected = currencyCode == code,
                                onClick = { currencyCode = code },
                                label = { Text("$code ($symbol)") }
                            )
                        }
                    }

                    Text("Measurement Units", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("METRIC", "IMPERIAL").forEach { sys ->
                            FilterChip(
                                selected = measurementSystem == sys,
                                onClick = { measurementSystem = sys },
                                label = { Text(sys) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = dietaryRestrictions,
                        onValueChange = { dietaryRestrictions = it },
                        label = { Text("Dietary Preferences") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = { Text("Allergies & Ingredients to Avoid") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Smart Reminders & Alerts", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Weekly budget reset, shopping & pantry expiry reminders",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    notificationsEnabled = enabled
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Theme (Charcoal Green)", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = isDarkTheme, onCheckedChange = { onToggleTheme() })
                    }

                    Button(
                        onClick = {
                            val newCents = BudgetAndGroceryEngine.parseToCents(budgetStr) ?: profile.weeklyBudgetCents
                            onSaveProfile(
                                profile.copy(
                                    householdName = householdName.ifBlank { "My Household" },
                                    weeklyBudgetCents = newCents,
                                    monthlyBudgetCents = newCents * 4,
                                    currencyCode = currencyCode,
                                    measurementSystem = measurementSystem,
                                    dietaryRestrictions = dietaryRestrictions,
                                    allergies = allergies,
                                    notificationsEnabled = notificationsEnabled
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_profile_settings_button")
                    ) {
                        Text("Save Household & Budget Settings")
                    }
                }
            }
        }

        // Data Export, Legal & Account Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Data & Account Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = onExportData,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Household Data (JSON)")
                    }
                    OutlinedButton(
                        onClick = { activeComplianceTab = ComplianceTab.PRIVACY },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_privacy_terms_button")
                    ) {
                        Text("Privacy Policy & Terms of Service")
                    }
                    OutlinedButton(
                        onClick = { activeComplianceTab = ComplianceTab.DELETE_PORTAL },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_delete_urls_button")
                    ) {
                        Text("Delete Account & Delete Data URLs")
                    }
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_out_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign Out")
                    }
                    OutlinedButton(
                        onClick = { showDeleteDataOnlyConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_data_only_button")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Cloud Data Only (Keep Account)")
                    }
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_account_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Account & All Cloud Data")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showDeleteDataOnlyConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteDataOnlyConfirm = false },
            title = { Text("Delete Cloud Data Only?") },
            text = {
                Text("This permanently deletes your meal plans, pantry inventory, grocery list, household members, and purchase records from Firestore while keeping your Google account profile active.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDataOnlyConfirm = false
                        onDeleteDataOnly()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Data Only")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDataOnlyConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account & All Data?") },
            text = {
                Text("This permanently deletes your household profile, meal plans, pantry inventory, grocery list, purchase history, and account from BasketBrain.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    activeComplianceTab?.let { tab ->
        LegalAndComplianceDialog(
            initialTab = tab,
            isSignedIn = true,
            userEmailOrName = profile.displayName,
            onDismiss = { activeComplianceTab = null },
            onDeleteCloudDataOnly = onDeleteDataOnly,
            onDeleteFullAccount = onDeleteAccount
        )
    }
}
