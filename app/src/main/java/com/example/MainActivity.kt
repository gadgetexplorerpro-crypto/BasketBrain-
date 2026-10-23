package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.UserProfile
import com.example.data.repository.BasketBrainRepository
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOut
import com.example.ui.screens.AuthGateScreen
import com.example.ui.screens.ComplianceTab
import com.example.ui.screens.FeatureModalOverlay
import com.example.ui.screens.GroceryAndBudgetScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.InteractiveOnboardingFlow
import com.example.ui.screens.MealPlanScreen
import com.example.ui.screens.PantryScreen
import com.example.ui.screens.ProfileAndSettingsScreen
import com.example.ui.theme.BasketBrainTheme
import com.example.ui.viewmodel.ActiveModalSheet
import com.example.ui.viewmodel.BasketBrainViewModel
import com.example.ui.viewmodel.MainNavTab
import com.example.data.ai.GeminiMealPlannerService
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeminiMealPlannerService.configureAppCheck(this, intent)
        val deepLinkPath = intent?.data?.path?.lowercase()
        val initialComplianceTab = when {
            deepLinkPath?.contains("privacy") == true -> ComplianceTab.PRIVACY
            deepLinkPath?.contains("terms") == true -> ComplianceTab.TERMS
            deepLinkPath?.contains("delete") == true -> ComplianceTab.DELETE_PORTAL
            else -> null
        }
        enableEdgeToEdge()
        setContent {
            BasketBrainRootApp(initialComplianceTab = initialComplianceTab)
        }
    }
}

@Composable
fun BasketBrainRootApp(
    auth: FirebaseAuth = Firebase.auth,
    initialComplianceTab: ComplianceTab? = null
) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        BasketBrainTheme {
            AuthGateScreen(
                onAuthSuccess = {},
                initialComplianceTab = initialComplianceTab
            )
        }
    } else {
        AuthenticatedBasketBrainApp(
            currentUserId = user.uid,
            displayName = user.displayName ?: "Household Chef"
        )
    }
}

@Composable
fun AuthenticatedBasketBrainApp(
    currentUserId: String,
    displayName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: BasketBrainViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                BasketBrainViewModel(BasketBrainRepository(db), currentUserId)
            }
        }
    )

    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val activeSheet by viewModel.activeSheet.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val isGeneratingAi by viewModel.isGeneratingAi.collectAsStateWithLifecycle()

    val userProfileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val plannedMeals by viewModel.plannedMeals.collectAsStateWithLifecycle()
    val pantryItems by viewModel.pantryItems.collectAsStateWithLifecycle()
    val groceryItems by viewModel.groceryItems.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val budgetSummary by viewModel.budgetSummary.collectAsStateWithLifecycle()
    val leftoverIdeas by viewModel.leftoverIdeas.collectAsStateWithLifecycle()
    val scannedReceiptDraft by viewModel.scannedReceiptDraft.collectAsStateWithLifecycle()
    val assistantMessages by viewModel.assistantMessages.collectAsStateWithLifecycle()
    val exportedDataJson by viewModel.exportedDataJson.collectAsStateWithLifecycle()

    val effectiveProfile = userProfileState ?: UserProfile(
        id = currentUserId,
        userId = currentUserId,
        displayName = displayName,
        onboardingCompleted = false
    )

    BasketBrainTheme(darkTheme = isDarkTheme) {
        if (userProfileState != null && !effectiveProfile.onboardingCompleted && plannedMeals.isEmpty()) {
            InteractiveOnboardingFlow(
                initialProfile = effectiveProfile,
                onCompleteOnboarding = { updatedProfile, initialPantryCsv ->
                    viewModel.completeOnboarding(updatedProfile, initialPantryCsv)
                }
            )
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (activeSheet == ActiveModalSheet.NONE) {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == MainNavTab.HOME,
                                onClick = { viewModel.selectTab(MainNavTab.HOME) },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") },
                                modifier = Modifier.testTag("nav_tab_home")
                            )
                            NavigationBarItem(
                                selected = selectedTab == MainNavTab.MEAL_PLAN,
                                onClick = { viewModel.selectTab(MainNavTab.MEAL_PLAN) },
                                icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Meal Plan") },
                                label = { Text("Meal Plan") },
                                modifier = Modifier.testTag("nav_tab_meal_plan")
                            )
                            NavigationBarItem(
                                selected = selectedTab == MainNavTab.GROCERY,
                                onClick = { viewModel.selectTab(MainNavTab.GROCERY) },
                                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Grocery") },
                                label = { Text("Grocery") },
                                modifier = Modifier.testTag("nav_tab_grocery")
                            )
                            NavigationBarItem(
                                selected = selectedTab == MainNavTab.PANTRY,
                                onClick = { viewModel.selectTab(MainNavTab.PANTRY) },
                                icon = { Icon(Icons.Default.Kitchen, contentDescription = "Pantry") },
                                label = { Text("Pantry") },
                                modifier = Modifier.testTag("nav_tab_pantry")
                            )
                            NavigationBarItem(
                                selected = selectedTab == MainNavTab.PROFILE,
                                onClick = { viewModel.selectTab(MainNavTab.PROFILE) },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("Profile") },
                                modifier = Modifier.testTag("nav_tab_profile")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    statusBanner?.let { msg ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(end = 64.dp)
                                )
                                TextButton(
                                    onClick = { viewModel.clearStatusBanner() },
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }

                    if (activeSheet != ActiveModalSheet.NONE) {
                        FeatureModalOverlay(
                            sheet = activeSheet,
                            profile = effectiveProfile,
                            budgetSummary = budgetSummary,
                            pantryItems = pantryItems,
                            plannedMeals = plannedMeals,
                            purchases = purchases,
                            members = members,
                            leftoverIdeas = leftoverIdeas,
                            scannedReceiptDraft = scannedReceiptDraft,
                            assistantMessages = assistantMessages,
                            exportedDataJson = exportedDataJson,
                            isGeneratingAi = isGeneratingAi,
                            onClose = { viewModel.closeSheet() },
                            onRecordPurchase = { store, date, cat, cents, summary, hash, isReceipt ->
                                viewModel.recordGroceryPurchase(store, date, cat, cents, summary, hash, isReceipt)
                            },
                            onScanReceipt = { bmp, hint -> viewModel.scanReceiptImage(bmp, hint) },
                            onClearReceiptDraft = { viewModel.clearScannedReceiptDraft() },
                            onRunLeftoverRescue = { items -> viewModel.runLeftoverRescue(items) },
                            onSendAssistantQuestion = { q -> viewModel.sendAssistantQuestion(q) },
                            onAddFamilyMember = { name, role, diet, dislikes, allergies, mult ->
                                viewModel.addFamilyMember(name, role, diet, dislikes, allergies, mult)
                            },
                            onRemoveFamilyMember = { id -> viewModel.removeFamilyMember(id) },
                            onAddRecipeToPlan = { meal ->
                                viewModel.addSingleMealIngredientsToGrocery(meal)
                            }
                        )
                    } else {
                        when (selectedTab) {
                            MainNavTab.HOME -> HomeDashboardScreen(
                                profile = effectiveProfile,
                                budgetSummary = budgetSummary,
                                plannedMeals = plannedMeals,
                                pantryItems = pantryItems,
                                groceryItems = groceryItems,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleDarkTheme() },
                                onNavigateTab = { viewModel.selectTab(it) },
                                onOpenSheet = { sheet ->
                                    if (sheet == ActiveModalSheet.LEFTOVER_RESCUE && leftoverIdeas.isEmpty()) {
                                        viewModel.runLeftoverRescue(pantryItems.map { it.name })
                                    }
                                    viewModel.openSheet(sheet)
                                },
                                onGeneratePlan = { viewModel.generateBudgetFirstMealPlanNow() },
                                onCookMeal = { viewModel.markMealCookedAndDeductPantry(it) },
                                onSwapMeal = { viewModel.swapMealWithAffordableAlternative(it) }
                            )

                            MainNavTab.MEAL_PLAN -> MealPlanScreen(
                                profile = effectiveProfile,
                                budgetSummary = budgetSummary,
                                plannedMeals = plannedMeals,
                                pantryItems = pantryItems,
                                members = members,
                                isGeneratingAi = isGeneratingAi,
                                onGeneratePlan = { viewModel.generateBudgetFirstMealPlanNow() },
                                onCookMeal = { viewModel.markMealCookedAndDeductPantry(it) },
                                onSwapMeal = { viewModel.swapMealWithAffordableAlternative(it) },
                                onToggleSaveRecipe = { viewModel.toggleSaveRecipe(it) },
                                onAddMealToGrocery = { viewModel.addSingleMealIngredientsToGrocery(it) },
                                onOpenSheet = { sheet ->
                                    if (sheet == ActiveModalSheet.LEFTOVER_RESCUE && leftoverIdeas.isEmpty()) {
                                        viewModel.runLeftoverRescue(pantryItems.map { it.name })
                                    }
                                    viewModel.openSheet(sheet)
                                }
                            )

                            MainNavTab.GROCERY -> GroceryAndBudgetScreen(
                                profile = effectiveProfile,
                                budgetSummary = budgetSummary,
                                groceryItems = groceryItems,
                                purchases = purchases,
                                onSyncFromMealPlan = { viewModel.syncConsolidatedGroceryListNow() },
                                onAddCustomGroceryItem = { n, q, u, c, p, rec ->
                                    viewModel.addCustomGroceryItem(n, q, u, c, p, rec)
                                },
                                onUpdateGroceryItem = { item, q, p ->
                                    viewModel.updateGroceryItemPriceOrQty(item, q, p)
                                },
                                onMarkPurchased = { item, confirmedCents, store ->
                                    viewModel.markGroceryItemPurchased(item, confirmedCents, store)
                                },
                                onDeleteGroceryItem = { viewModel.deleteGroceryItem(it) },
                                onDeletePurchase = { viewModel.deletePurchaseRecord(it) },
                                onOpenSheet = { viewModel.openSheet(it) }
                            )

                            MainNavTab.PANTRY -> PantryScreen(
                                profile = effectiveProfile,
                                pantryItems = pantryItems,
                                onAddPantryItem = { n, q, u, c, s, exp, p ->
                                    viewModel.addPantryItem(n, q, u, c, s, exp, p)
                                },
                                onAdjustQuantity = { item, delta ->
                                    viewModel.adjustPantryItemQuantity(item, delta)
                                },
                                onDeleteItem = { viewModel.deletePantryItem(it) },
                                onOpenLeftoverRescue = {
                                    viewModel.runLeftoverRescue(pantryItems.map { it.name })
                                    viewModel.openSheet(ActiveModalSheet.LEFTOVER_RESCUE)
                                }
                            )

                            MainNavTab.PROFILE -> ProfileAndSettingsScreen(
                                profile = effectiveProfile,
                                members = members,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleDarkTheme() },
                                onSaveProfile = { viewModel.updateProfileSettings(it) },
                                onOpenSheet = { viewModel.openSheet(it) },
                                onExportData = { viewModel.prepareDataExport() },
                                onDeleteDataOnly = { viewModel.deleteCloudDataOnly() },
                                onDeleteAccount = {
                                    viewModel.deleteAccountData {
                                        signOut(context, credentialManager, {}, scope)
                                    }
                                },
                                onSignOut = {
                                    signOut(context, credentialManager, {}, scope)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
