package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.BasketBrainCatalog
import com.example.data.model.BudgetSummary
import com.example.data.model.HouseholdMember
import com.example.data.model.LeftoverRescueSuggestion
import com.example.data.model.PantryItem
import com.example.data.model.PlannedMeal
import com.example.data.model.PurchaseRecord
import com.example.data.model.ScannedReceiptDraft
import com.example.data.model.SubscriptionTierState
import com.example.data.model.UserProfile
import com.example.domain.BudgetAndGroceryEngine
import com.example.ui.viewmodel.ActiveModalSheet
import com.example.ui.viewmodel.ChatMessage
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeatureModalOverlay(
    sheet: ActiveModalSheet,
    profile: UserProfile,
    budgetSummary: BudgetSummary,
    pantryItems: List<PantryItem>,
    plannedMeals: List<PlannedMeal>,
    purchases: List<PurchaseRecord>,
    members: List<HouseholdMember>,
    leftoverIdeas: List<LeftoverRescueSuggestion>,
    scannedReceiptDraft: ScannedReceiptDraft?,
    assistantMessages: List<ChatMessage>,
    exportedDataJson: String,
    isGeneratingAi: Boolean,
    onClose: () -> Unit,
    onRecordPurchase: (String, String, String, Long, String, String, Boolean) -> Unit,
    onScanReceipt: (android.graphics.Bitmap?, String) -> Unit,
    onClearReceiptDraft: () -> Unit,
    onRunLeftoverRescue: (List<String>) -> Unit,
    onSendAssistantQuestion: (String) -> Unit,
    onAddFamilyMember: (String, String, String, String, String, Double) -> Unit,
    onRemoveFamilyMember: (String) -> Unit,
    onAddRecipeToPlan: (PlannedMeal) -> Unit
) {
    BackHandler(onBack = onClose)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_modal_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (sheet) {
                        ActiveModalSheet.ADD_EXPENSE -> "Record Grocery Purchase"
                        ActiveModalSheet.SCAN_RECEIPT -> "Grocery Receipt Scanner"
                        ActiveModalSheet.LEFTOVER_RESCUE -> "Rescue My Leftovers"
                        ActiveModalSheet.RECIPE_DISCOVERY -> "Affordable Recipe Discovery"
                        ActiveModalSheet.SPENDING_ANALYTICS -> "Spending Analytics"
                        ActiveModalSheet.AI_ASSISTANT -> "BasketBrain Assistant"
                        ActiveModalSheet.SUBSCRIPTION_PLANS -> "BasketBrain Free & Pro Plans"
                        ActiveModalSheet.FAMILY_MEMBERS -> "Family Meal Planning"
                        ActiveModalSheet.DATA_EXPORT_PREVIEW -> "Household Data Export (JSON)"
                        ActiveModalSheet.NONE -> ""
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            when (sheet) {
                ActiveModalSheet.ADD_EXPENSE -> {
                    var storeName by remember { mutableStateOf("Local Supermarket") }
                    var dateStr by remember { mutableStateOf("2026-10-22") }
                    var category by remember { mutableStateOf("Vegetables") }
                    var amountStr by remember { mutableStateOf("") }
                    var itemsSummary by remember { mutableStateOf("") }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("Store Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = dateStr,
                            onValueChange = { dateStr = it },
                            label = { Text("Purchase Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Category", style = MaterialTheme.typography.labelLarge)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BasketBrainCatalog.spendingCategories.forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text("Total Spent (${profile.currencyCode})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expense_amount_input")
                        )
                        OutlinedTextField(
                            value = itemsSummary,
                            onValueChange = { itemsSummary = it },
                            label = { Text("Items Purchased (e.g. Rice, Eggs, Tomatoes)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val cents = BudgetAndGroceryEngine.parseToCents(amountStr) ?: 0L
                                if (cents > 0L) {
                                    onRecordPurchase(storeName, dateStr, category, cents, itemsSummary, "", false)
                                    onClose()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_save_expense_button")
                        ) {
                            Text("Save Purchase & Update Budget")
                        }
                    }
                }

                ActiveModalSheet.SCAN_RECEIPT -> {
                    var storeHint by remember { mutableStateOf("Aldi Supermarket") }
                    val photoPicker = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickVisualMedia()
                    ) { uri ->
                        if (uri != null) {
                            onScanReceipt(null, storeHint)
                        }
                    }
                    val cameraLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.TakePicturePreview()
                    ) { bmp ->
                        if (bmp != null) {
                            onScanReceipt(bmp, storeHint)
                        }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Text(
                                "Photograph or upload a grocery receipt. You can review and edit every extracted field before saving to prevent errors or duplicate transactions.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = storeHint,
                                onValueChange = { storeHint = it },
                                label = { Text("Store Name Hint (optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { cameraLauncher.launch(null) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Camera Scan")
                                }
                                OutlinedButton(
                                    onClick = {
                                        photoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pick Receipt Photo")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { onScanReceipt(null, storeHint) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("extract_sample_receipt_button")
                            ) {
                                Text("Run Receipt OCR Extraction")
                            }
                        }

                        if (isGeneratingAi) {
                            item {
                                CircularProgressIndicator()
                            }
                        }

                        scannedReceiptDraft?.let { draft ->
                            item {
                                var editableStore by remember(draft) { mutableStateOf(draft.storeName) }
                                var editableDate by remember(draft) { mutableStateOf(draft.purchaseDate) }
                                var editableCategory by remember(draft) { mutableStateOf(draft.category) }
                                var editableTotalStr by remember(draft) {
                                    mutableStateOf(String.format(Locale.US, "%.2f", draft.totalCents / 100.0))
                                }
                                var editableItemsStr by remember(draft) {
                                    mutableStateOf(
                                        draft.lineItems.joinToString(", ") {
                                            "${it.name} (${BudgetAndGroceryEngine.formatCurrency(it.totalPriceCents, profile.currencyCode)})"
                                        }
                                    )
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            "Verify & Edit Extracted Receipt",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        OutlinedTextField(
                                            value = editableStore,
                                            onValueChange = { editableStore = it },
                                            label = { Text("Store Name") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = editableDate,
                                            onValueChange = { editableDate = it },
                                            label = { Text("Purchase Date") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = editableItemsStr,
                                            onValueChange = { editableItemsStr = it },
                                            label = { Text("Extracted Items & Prices (Editable)") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = editableTotalStr,
                                            onValueChange = { editableTotalStr = it },
                                            label = { Text("Receipt Total (${profile.currencyCode})") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    val finalCents = BudgetAndGroceryEngine.parseToCents(editableTotalStr) ?: draft.totalCents
                                                    onRecordPurchase(
                                                        editableStore,
                                                        editableDate,
                                                        editableCategory,
                                                        finalCents,
                                                        editableItemsStr,
                                                        draft.receiptHash,
                                                        true
                                                    )
                                                    onClose()
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Confirm & Save Receipt")
                                            }
                                            OutlinedButton(onClick = onClearReceiptDraft) {
                                                Text("Discard")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ActiveModalSheet.LEFTOVER_RESCUE -> {
                    var customLeftovers by remember {
                        mutableStateOf(
                            pantryItems.take(4).joinToString(", ") { it.name }
                                .ifEmpty { "Cooked rice, Chicken, Half an onion, Two tomatoes" }
                        )
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Text(
                                "Select or enter available leftovers and pantry items to generate practical zero-waste meals.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customLeftovers,
                                onValueChange = { customLeftovers = it },
                                label = { Text("Available Leftovers & Ingredients") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    onRunLeftoverRescue(customLeftovers.split(",").map { it.trim() })
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_leftover_ideas_button")
                            ) {
                                Text("Rescue My Leftovers")
                            }
                        }

                        items(leftoverIdeas, key = { it.recipeName }) { idea ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        idea.recipeName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Prep: ${idea.prepTimeMins} mins • Est. Additional Cost: ${
                                            BudgetAndGroceryEngine.formatCurrency(
                                                idea.estimatedAdditionalCostCents,
                                                profile.currencyCode
                                            )
                                        }",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Reuses: ${idea.usedLeftovers.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "Additional needed: ${idea.additionalIngredientsNeeded.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(idea.instructions, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.WarningAmber,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                idea.safetyReminder,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ActiveModalSheet.RECIPE_DISCOVERY -> {
                    var selectedFilter by remember { mutableStateOf("Under $2/srv") }
                    val filterTags = listOf(
                        "Under $2/srv",
                        "Under $5/srv",
                        "High protein",
                        "Vegetarian",
                        "Halal",
                        "15-minute meals",
                        "One-pot meals",
                        "Pantry-only meals"
                    )

                    val discoveryRecipes = remember {
                        BudgetAndGroceryEngine.generateBudgetFirstMealPlan(
                            userId = profile.userId,
                            profile = profile.copy(mealsPerDay = 3, planningDays = 3),
                            pantryItems = pantryItems,
                            members = members,
                            daysCount = 3
                        )
                    }

                    val filtered = remember(discoveryRecipes, selectedFilter) {
                        discoveryRecipes.filter { meal ->
                            when (selectedFilter) {
                                "Under $2/srv" -> meal.costPerServingCents <= 200L
                                "Under $5/srv" -> meal.costPerServingCents <= 500L
                                "High protein" -> meal.proteinGrams >= 18
                                "15-minute meals" -> (meal.prepTimeMins + meal.cookTimeMins) <= 20
                                else -> true
                            }
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filterTags.forEach { tag ->
                                FilterChip(
                                    selected = selectedFilter == tag,
                                    onClick = { selectedFilter = tag },
                                    label = { Text(tag) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(filtered, key = { it.recipeName + it.dayIndex }) { meal ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            meal.recipeName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${BudgetAndGroceryEngine.formatCurrency(meal.costPerServingCents, profile.currencyCode)}/serving • ${meal.prepTimeMins + meal.cookTimeMins} mins • ${meal.proteinGrams}g protein",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(meal.instructions, style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                onAddRecipeToPlan(meal)
                                                onClose()
                                            }
                                        ) {
                                            Text("Add to Meal Calendar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ActiveModalSheet.SPENDING_ANALYTICS -> {
                    val currency = profile.currencyCode
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                        "Household Financial Metrics",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Weekly Grocery Spending: ${BudgetAndGroceryEngine.formatCurrency(budgetSummary.spentCents, currency)}")
                                    Text("Weekly Target Budget: ${BudgetAndGroceryEngine.formatCurrency(budgetSummary.totalBudgetCents, currency)}")
                                    Text("Budget Adherence: ${budgetSummary.usagePercentage}% used (${if (budgetSummary.isOverBudget) "Over budget" else "On track"})")
                                    Text("Average Cost per Planned Meal: ${BudgetAndGroceryEngine.formatCurrency(budgetSummary.averageCostPerMealCents, currency)}")
                                    Text("Estimated Cost per Serving: ${BudgetAndGroceryEngine.formatCurrency(budgetSummary.averageCostPerServingCents, currency)}")
                                    Text("Pantry Items Tracked: ${pantryItems.size}")
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Spending Breakdown by Category",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val denom = budgetSummary.spentCents.coerceAtLeast(1L)
                                    BasketBrainCatalog.spendingCategories.forEach { cat ->
                                        val amount = budgetSummary.categorySpendingCents[cat] ?: 0L
                                        val ratio = (amount.toFloat() / denom.toFloat()).coerceIn(0f, 1f)
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(cat, style = MaterialTheme.typography.bodySmall)
                                                Text(
                                                    BudgetAndGroceryEngine.formatCurrency(amount, currency),
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            }
                                            LinearProgressIndicator(
                                                progress = { ratio },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ActiveModalSheet.AI_ASSISTANT -> {
                    var promptInput by remember { mutableStateOf("") }
                    val quickPrompts = listOf(
                        "What can I cook tonight for under $5?",
                        "Replace chicken with a cheaper protein.",
                        "Give me affordable high-protein lunches.",
                        "How do I use my leftover rice?"
                    )

                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickPrompts.forEach { q ->
                                FilterChip(
                                    selected = false,
                                    onClick = { onSendAssistantQuestion(q) },
                                    label = { Text(q) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(assistantMessages, key = { it.id }) { msg ->
                                val isUser = msg.sender == "USER"
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (isUser) "You" else "BasketBrain Assistant",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = msg.text, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = promptInput,
                                onValueChange = { promptInput = it },
                                placeholder = { Text("Ask BasketBrain Assistant...") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (promptInput.isNotBlank()) {
                                        onSendAssistantQuestion(promptInput)
                                        promptInput = ""
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                            }
                        }
                    }
                }

                ActiveModalSheet.SUBSCRIPTION_PLANS -> {
                    val currentState = SubscriptionTierState.fromCode(profile.subscriptionState)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Current Entitlement: ${currentState.label}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Supported Entitlement States: Free, Active, Trialing, Grace Period, Expired, Canceled (active until expiry).", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("FREE PLAN ($0/month)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("• 1 Household profile & 1 active weekly budget")
                                    Text("• 1 Saved 7-day meal plan & smart consolidated grocery list")
                                    Text("• Manual digital pantry tracking & up to 5 AI generations")
                                    Text("• Basic spending history")
                                }
                            }
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("BASKETBRAIN PRO ($4.99/month)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("• Unlimited saved meal plans & advanced budget optimization")
                                    Text("• Multiple household profiles & household sharing")
                                    Text("• Receipt OCR scanning (up to 50/mo) & Leftover Rescue")
                                    Text("• Advanced spending analytics & Personalized AI Assistant")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Google Play Billing Required in Production Release")
                                    }
                                    Text(
                                        "Compliance Notice: Purchasing is disabled in this build environment because Google Play Billing Library (com.android.billingclient:billing-ktx) and a Play Console merchant account with server-side purchase token verification are required before enabling in-app subscriptions. BasketBrain never marks a subscription as paid without verified payment evidence.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                ActiveModalSheet.FAMILY_MEMBERS -> {
                    var name by remember { mutableStateOf("") }
                    var role by remember { mutableStateOf("ADULT") }
                    var diet by remember { mutableStateOf("High protein") }
                    var dislikes by remember { mutableStateOf("") }
                    var allergies by remember { mutableStateOf("") }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Add Household Member", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Member Name (e.g. Alex, Sam, Maya)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("ADULT", "PARTNER", "CHILD").forEach { r ->
                                            FilterChip(
                                                selected = role == r,
                                                onClick = { role = r },
                                                label = { Text(r) }
                                            )
                                        }
                                    }
                                    OutlinedTextField(
                                        value = diet,
                                        onValueChange = { diet = it },
                                        label = { Text("Dietary Preference (e.g. High protein, Vegetarian, Mild)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = dislikes,
                                        onValueChange = { dislikes = it },
                                        label = { Text("Disliked Foods") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = allergies,
                                        onValueChange = { allergies = it },
                                        label = { Text("Allergies (Optional)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            if (name.isNotBlank()) {
                                                onAddFamilyMember(
                                                    name,
                                                    role,
                                                    diet,
                                                    dislikes,
                                                    allergies,
                                                    if (role == "CHILD") 0.6 else 1.0
                                                )
                                                name = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Add Member to Shared Meal Plan")
                                    }
                                }
                            }
                        }

                        items(members, key = { it.id.ifEmpty { it.name } }) { m ->
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
                                        Text("${m.name} (${m.role})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("Diet: ${m.dietaryPreference} • Portion: ${m.servingMultiplier}x", style = MaterialTheme.typography.bodySmall)
                                        if (m.allergies.isNotBlank()) {
                                            Text("Allergies: ${m.allergies}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    if (m.id.isNotBlank()) {
                                        IconButton(onClick = { onRemoveFamilyMember(m.id) }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Remove member")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ActiveModalSheet.DATA_EXPORT_PREVIEW -> {
                    LazyColumn {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Text(
                                    text = exportedDataJson.ifBlank { "{}" },
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }

                ActiveModalSheet.NONE -> {}
            }
        }
    }
}
