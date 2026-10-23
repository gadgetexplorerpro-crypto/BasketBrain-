package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.model.BasketBrainCatalog
import com.example.data.model.UserProfile
import com.example.domain.BudgetAndGroceryEngine
import com.example.ui.auth.GoogleSignInButton
import com.example.ui.auth.attemptAutoSignIn

@Composable
fun AuthGateScreen(
    onAuthSuccess: () -> Unit,
    initialComplianceTab: ComplianceTab? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var authError by remember { mutableStateOf<String?>(null) }
    var activeComplianceTab by remember(initialComplianceTab) { mutableStateOf(initialComplianceTab) }

    LaunchedEffect(Unit) {
        if (initialComplianceTab == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = onAuthSuccess,
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Eat Better. Spend Smarter. Waste Less.",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "BasketBrain",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your grocery budget deserves a smarter plan.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.img_hero_groceries),
                    contentDescription = "BasketBrain fresh groceries and budget planning illustration",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .clip(RoundedCornerShape(22.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(20.dp))

                OnboardingFeatureRow(
                    icon = Icons.Default.Savings,
                    title = "Budget-First Meal Planning",
                    subtitle = "Start with your real weekly budget ($50, $100, $150+) and build practical meals around it."
                )
                Spacer(modifier = Modifier.height(10.dp))
                OnboardingFeatureRow(
                    icon = Icons.Default.RestaurantMenu,
                    title = "Pantry-First & Leftover Rescue",
                    subtitle = "Automatically subtract ingredients you already own and turn leftovers into dinner."
                )
                Spacer(modifier = Modifier.height(10.dp))
                OnboardingFeatureRow(
                    icon = Icons.Default.CheckCircle,
                    title = "Deterministic Grocery Math",
                    subtitle = "Accurate cent-precision expense tracking, smart shopping lists, and receipt scanning."
                )
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (authError != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = authError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                GoogleSignInButton(
                    onAuthSuccess = onAuthSuccess,
                    onAuthError = { msg -> authError = msg },
                    buttonText = "Continue with Google"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LoginLegalAndDataFooter(
                    onOpenTab = { tab -> activeComplianceTab = tab }
                )
            }
        }
    }

    activeComplianceTab?.let { tab ->
        LegalAndComplianceDialog(
            initialTab = tab,
            isSignedIn = false,
            onDismiss = { activeComplianceTab = null }
        )
    }
}

@Composable
private fun OnboardingFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveOnboardingFlow(
    initialProfile: UserProfile,
    onCompleteOnboarding: (UserProfile, String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var householdName by remember { mutableStateOf(initialProfile.householdName) }
    var adultsCount by remember { mutableIntStateOf(initialProfile.adultsCount) }
    var childrenCount by remember { mutableIntStateOf(initialProfile.childrenCount) }
    var childAges by remember { mutableStateOf(initialProfile.childAgeRanges) }
    var mealsPerDay by remember { mutableIntStateOf(initialProfile.mealsPerDay) }
    var planningDays by remember { mutableIntStateOf(initialProfile.planningDays) }

    var currencyCode by remember { mutableStateOf(initialProfile.currencyCode) }
    var budgetAmountInput by remember { mutableStateOf((initialProfile.weeklyBudgetCents / 100L).toString()) }

    var selectedDiets by remember {
        mutableStateOf(
            initialProfile.dietaryRestrictions.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        )
    }
    var allergiesInput by remember { mutableStateOf(initialProfile.allergies) }

    var favoriteCuisines by remember { mutableStateOf(initialProfile.favoriteCuisines) }
    var dislikedIngredients by remember { mutableStateOf(initialProfile.dislikedIngredients) }
    var cookingExperience by remember { mutableStateOf(initialProfile.cookingExperience) }
    var maxCookTime by remember { mutableStateOf(initialProfile.maxCookingTimeMins.toString()) }
    var kitchenEquipment by remember { mutableStateOf(initialProfile.kitchenEquipment) }
    var preferredStores by remember { mutableStateOf(initialProfile.preferredStores) }
    var initialPantryCsv by remember { mutableStateOf("Rice, Lentils, Eggs, Onions, Tomatoes") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.widthIn(max = 560.dp)) {
                Text(
                    text = "Step $step of 5 • Setup BasketBrain",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { step / 5f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(20.dp))

                when (step) {
                    1 -> {
                        Text(
                            text = "Your grocery budget deserves a smarter plan.",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Let's personalize BasketBrain for your household so every meal plan starts with your budget and the food you already own.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_groceries),
                            contentDescription = "Welcome banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = householdName,
                            onValueChange = { householdName = it },
                            label = { Text("Household Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_household_name")
                        )
                    }

                    2 -> {
                        Text(
                            text = "Household Setup",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "How many people are we cooking and budgeting for?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        CounterSelectorRow("Adults in household", adultsCount, 1, 12) { adultsCount = it }
                        Spacer(modifier = Modifier.height(10.dp))
                        CounterSelectorRow("Children in household", childrenCount, 0, 12) { childrenCount = it }

                        if (childrenCount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = childAges,
                                onValueChange = { childAges = it },
                                label = { Text("Child age ranges (optional, e.g. 4-8 yrs)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        CounterSelectorRow("Meals needed per day", mealsPerDay, 1, 3) { mealsPerDay = it }
                        Spacer(modifier = Modifier.height(10.dp))
                        CounterSelectorRow("Number of planning days", planningDays, 3, 7) { planningDays = it }
                    }

                    3 -> {
                        Text(
                            text = "Weekly Grocery Budget",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Choose your currency and target weekly grocery budget.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Currency", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BasketBrainCatalog.currencies.forEach { (code, symbol) ->
                                FilterChip(
                                    selected = currencyCode == code,
                                    onClick = { currencyCode = code },
                                    label = { Text("$code ($symbol)") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Quick Presets", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("50", "80", "100", "150").forEach { preset ->
                                FilterChip(
                                    selected = budgetAmountInput == preset,
                                    onClick = { budgetAmountInput = preset },
                                    label = { Text("$preset/wk") },
                                    modifier = Modifier.testTag("budget_preset_$preset")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = budgetAmountInput,
                            onValueChange = { budgetAmountInput = it },
                            label = { Text("Weekly Budget Amount ($currencyCode)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_budget_input")
                        )
                    }

                    4 -> {
                        Text(
                            text = "Dietary Preferences & Allergies",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BasketBrainCatalog.dietaryOptions.forEach { option ->
                                val isSel = selectedDiets.contains(option)
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        selectedDiets = if (isSel) selectedDiets - option else selectedDiets + option
                                    },
                                    label = { Text(option) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = allergiesInput,
                            onValueChange = { allergiesInput = it },
                            label = { Text("Allergies & ingredients to avoid (comma-separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = "Allergen disclaimer",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Allergen Notice: Always check physical product packaging. BasketBrain cannot guarantee allergen safety from AI-generated recipes or unverified ingredient data.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    5 -> {
                        Text(
                            text = "Food & Pantry Setup",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = initialPantryCsv,
                            onValueChange = { initialPantryCsv = it },
                            label = { Text("Pantry staples you already own (comma-separated)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_pantry_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = favoriteCuisines,
                            onValueChange = { favoriteCuisines = it },
                            label = { Text("Favorite Cuisines") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = dislikedIngredients,
                            onValueChange = { dislikedIngredients = it },
                            label = { Text("Disliked Ingredients") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = maxCookTime,
                            onValueChange = { maxCookTime = it },
                            label = { Text("Maximum Cooking Time (minutes)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = kitchenEquipment,
                            onValueChange = { kitchenEquipment = it },
                            label = { Text("Kitchen Equipment") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = preferredStores,
                            onValueChange = { preferredStores = it },
                            label = { Text("Preferred Grocery Stores") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step -= 1 },
                            modifier = Modifier.testTag("onboarding_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (step < 5) {
                                step += 1
                            } else {
                                val parsedBudgetCents = BudgetAndGroceryEngine.parseToCents(budgetAmountInput) ?: 10000L
                                val updated = initialProfile.copy(
                                    householdName = householdName.ifBlank { "My Household" },
                                    adultsCount = adultsCount,
                                    childrenCount = childrenCount,
                                    childAgeRanges = childAges,
                                    mealsPerDay = mealsPerDay,
                                    planningDays = planningDays,
                                    weeklyBudgetCents = parsedBudgetCents,
                                    monthlyBudgetCents = parsedBudgetCents * 4,
                                    currencyCode = currencyCode,
                                    dietaryRestrictions = selectedDiets.joinToString(", ").ifBlank { "No restrictions" },
                                    allergies = allergiesInput.trim(),
                                    favoriteCuisines = favoriteCuisines.trim(),
                                    dislikedIngredients = dislikedIngredients.trim(),
                                    cookingExperience = cookingExperience,
                                    maxCookingTimeMins = maxCookTime.toIntOrNull()?.coerceIn(10, 180) ?: 35,
                                    kitchenEquipment = kitchenEquipment.trim(),
                                    preferredStores = preferredStores.trim(),
                                    onboardingCompleted = true
                                )
                                onCompleteOnboarding(updated, initialPantryCsv)
                            }
                        },
                        modifier = Modifier.testTag("onboarding_next_button")
                    ) {
                        Text(if (step < 5) "Continue" else "Build My Smart Plan")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterSelectorRow(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { if (value > min) onValueChange(value - 1) }
                ) {
                    Text("-")
                }
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                OutlinedButton(
                    onClick = { if (value < max) onValueChange(value + 1) }
                ) {
                    Text("+")
                }
            }
        }
    }
}
