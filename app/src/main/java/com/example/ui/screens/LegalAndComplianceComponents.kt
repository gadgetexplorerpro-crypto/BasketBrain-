package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

object BasketBrainComplianceUrls {
    const val DEFAULT_GITHUB_USER = "gadgetexplorerpro"
    const val DEFAULT_GITHUB_REPO = "basketbrain"

    // GitHub Pages Interactive HTML Form URLs (from /docs/*.html in repo)
    fun githubPagesPrivacyUrl(user: String = DEFAULT_GITHUB_USER, repo: String = DEFAULT_GITHUB_REPO): String =
        "https://${user.trim()}.github.io/${repo.trim()}/privacy-policy.html"

    fun githubPagesTermsUrl(user: String = DEFAULT_GITHUB_USER, repo: String = DEFAULT_GITHUB_REPO): String =
        "https://${user.trim()}.github.io/${repo.trim()}/terms-of-service.html"

    fun githubPagesDeleteAccountUrl(user: String = DEFAULT_GITHUB_USER, repo: String = DEFAULT_GITHUB_REPO): String =
        "https://${user.trim()}.github.io/${repo.trim()}/delete-account.html"

    fun githubPagesDeleteDataUrl(user: String = DEFAULT_GITHUB_USER, repo: String = DEFAULT_GITHUB_REPO): String =
        "https://${user.trim()}.github.io/${repo.trim()}/delete-data.html"

    // Direct GitHub Repository Markdown URLs (from /docs/*.md in repo — works immediately without GitHub Pages setup)
    fun githubRepoPrivacyUrl(user: String = DEFAULT_GITHUB_USER, repo: String = DEFAULT_GITHUB_REPO): String =
        "https://github.com/${user.trim()}/${repo.trim()}/blob/main/docs/PRIVACY_POLICY.md"

    fun githubRepoTermsUrl(user: String = DEFAULT_GITHUB_USER, repo: String = DEFAULT_GITHUB_REPO): String =
        "https://github.com/${user.trim()}/${repo.trim()}/blob/main/docs/TERMS_OF_SERVICE.md"

    fun githubRepoDeleteAccountUrl(user: String = DEFAULT_GITHUB_USER, repo: String = DEFAULT_GITHUB_REPO): String =
        "https://github.com/${user.trim()}/${repo.trim()}/blob/main/docs/DELETE_ACCOUNT.md"

    fun githubRepoDeleteDataUrl(user: String = DEFAULT_GITHUB_USER, repo: String = DEFAULT_GITHUB_REPO): String =
        "https://github.com/${user.trim()}/${repo.trim()}/blob/main/docs/DELETE_DATA.md"

    val PRIVACY_POLICY_URL: String = githubPagesPrivacyUrl()
    val TERMS_OF_SERVICE_URL: String = githubPagesTermsUrl()
    val DELETE_ACCOUNT_URL: String = githubPagesDeleteAccountUrl()
    val DELETE_DATA_URL: String = githubPagesDeleteDataUrl()
    const val SUPPORT_EMAIL = "privacy@basketbrain.app"
}

enum class ComplianceTab(val title: String) {
    PRIVACY("Privacy Policy"),
    TERMS("Terms of Service"),
    DELETE_PORTAL("Delete Account & Data")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginLegalAndDataFooter(
    onOpenTab: (ComplianceTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copiedLabel by remember { mutableStateOf<String?>(null) }
    var useGithubPagesFormat by remember { mutableStateOf(true) }
    var githubUser by remember { mutableStateOf(BasketBrainComplianceUrls.DEFAULT_GITHUB_USER) }
    var githubRepo by remember { mutableStateOf(BasketBrainComplianceUrls.DEFAULT_GITHUB_REPO) }
    var showCustomizeGithub by remember { mutableStateOf(false) }

    val privacyUrl = if (useGithubPagesFormat) {
        BasketBrainComplianceUrls.githubPagesPrivacyUrl(githubUser, githubRepo)
    } else {
        BasketBrainComplianceUrls.githubRepoPrivacyUrl(githubUser, githubRepo)
    }
    val termsUrl = if (useGithubPagesFormat) {
        BasketBrainComplianceUrls.githubPagesTermsUrl(githubUser, githubRepo)
    } else {
        BasketBrainComplianceUrls.githubRepoTermsUrl(githubUser, githubRepo)
    }
    val deleteAccountUrl = if (useGithubPagesFormat) {
        BasketBrainComplianceUrls.githubPagesDeleteAccountUrl(githubUser, githubRepo)
    } else {
        BasketBrainComplianceUrls.githubRepoDeleteAccountUrl(githubUser, githubRepo)
    }
    val deleteDataUrl = if (useGithubPagesFormat) {
        BasketBrainComplianceUrls.githubPagesDeleteDataUrl(githubUser, githubRepo)
    } else {
        BasketBrainComplianceUrls.githubRepoDeleteDataUrl(githubUser, githubRepo)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "By continuing with Google, you agree to our Terms of Service and acknowledge our Privacy Policy.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AssistChip(
                onClick = { onOpenTab(ComplianceTab.PRIVACY) },
                label = { Text("Privacy Policy") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.testTag("login_privacy_policy_button")
            )

            AssistChip(
                onClick = { onOpenTab(ComplianceTab.TERMS) },
                label = { Text("Terms of Service") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.testTag("login_terms_of_service_button")
            )

            AssistChip(
                onClick = { onOpenTab(ComplianceTab.DELETE_PORTAL) },
                label = { Text("Delete Account & Data") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.testTag("login_delete_account_data_button")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GitHub Legal & Data Safety URLs",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(onClick = { showCustomizeGithub = !showCustomizeGithub }) {
                        Text(
                            text = if (showCustomizeGithub) "Hide Repo Config" else "Edit GitHub Repo",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = useGithubPagesFormat,
                        onClick = { useGithubPagesFormat = true },
                        label = { Text("GitHub Pages (.github.io)", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = !useGithubPagesFormat,
                        onClick = { useGithubPagesFormat = false },
                        label = { Text("GitHub Repo (/docs/*.md)", style = MaterialTheme.typography.labelSmall) }
                    )
                }

                if (showCustomizeGithub) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = githubUser,
                            onValueChange = { githubUser = it },
                            label = { Text("GitHub User") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = githubRepo,
                            onValueChange = { githubRepo = it },
                            label = { Text("Repo Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                copiedLabel?.let { label ->
                    Text(
                        text = "Copied $label to clipboard!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                ComplianceUrlItemRow(
                    label = "Privacy Policy URL (GitHub)",
                    url = privacyUrl,
                    onCopy = {
                        copyUrlToClipboard(context, "Privacy Policy URL", privacyUrl)
                        copiedLabel = "Privacy URL"
                    },
                    onOpen = { onOpenTab(ComplianceTab.PRIVACY) }
                )

                ComplianceUrlItemRow(
                    label = "Terms of Service URL (GitHub)",
                    url = termsUrl,
                    onCopy = {
                        copyUrlToClipboard(context, "Terms of Service URL", termsUrl)
                        copiedLabel = "Terms URL"
                    },
                    onOpen = { onOpenTab(ComplianceTab.TERMS) }
                )

                ComplianceUrlItemRow(
                    label = "Delete Account Form URL (GitHub)",
                    url = deleteAccountUrl,
                    onCopy = {
                        copyUrlToClipboard(context, "Delete Account URL", deleteAccountUrl)
                        copiedLabel = "Delete Account URL"
                    },
                    onOpen = { onOpenTab(ComplianceTab.DELETE_PORTAL) }
                )

                ComplianceUrlItemRow(
                    label = "Delete Data Form URL (GitHub)",
                    url = deleteDataUrl,
                    onCopy = {
                        copyUrlToClipboard(context, "Delete Data URL", deleteDataUrl)
                        copiedLabel = "Delete Data URL"
                    },
                    onOpen = { onOpenTab(ComplianceTab.DELETE_PORTAL) }
                )
            }
        }
    }
}

@Composable
fun ComplianceUrlItemRow(
    label: String,
    url: String,
    onCopy: () -> Unit,
    onOpen: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy $label",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onOpen,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "View $label",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LegalAndComplianceDialog(
    initialTab: ComplianceTab = ComplianceTab.PRIVACY,
    isSignedIn: Boolean = false,
    userEmailOrName: String = "",
    onDismiss: () -> Unit,
    onDeleteCloudDataOnly: (() -> Unit)? = null,
    onDeleteFullAccount: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(initialTab) }
    var copiedNotice by remember { mutableStateOf<String?>(null) }
    var requestEmail by remember { mutableStateOf(userEmailOrName) }
    var requestScope by remember { mutableStateOf("DELETE_ACCOUNT_AND_DATA") }
    var requestSubmitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Legal, Privacy & GitHub Data Safety URLs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ComplianceTab.entries.forEach { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(tab.title, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                copiedNotice?.let { msg ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                when (selectedTab) {
                    ComplianceTab.PRIVACY -> {
                        ComplianceUrlBox(
                            title = "GitHub Pages Privacy Policy URL",
                            url = BasketBrainComplianceUrls.githubPagesPrivacyUrl(),
                            onCopy = {
                                copyUrlToClipboard(context, "GitHub Pages Privacy URL", BasketBrainComplianceUrls.githubPagesPrivacyUrl())
                                copiedNotice = "GitHub Pages Privacy Policy URL copied."
                            },
                            onOpenBrowser = {
                                openExternalUrl(context, BasketBrainComplianceUrls.githubPagesPrivacyUrl())
                            }
                        )
                        ComplianceUrlBox(
                            title = "GitHub Repo Privacy Policy Markdown URL",
                            url = BasketBrainComplianceUrls.githubRepoPrivacyUrl(),
                            onCopy = {
                                copyUrlToClipboard(context, "GitHub Repo Privacy URL", BasketBrainComplianceUrls.githubRepoPrivacyUrl())
                                copiedNotice = "GitHub Repo Privacy Policy URL copied."
                            },
                            onOpenBrowser = {
                                openExternalUrl(context, BasketBrainComplianceUrls.githubRepoPrivacyUrl())
                            }
                        )
                        Text(
                            text = "Effective Date: October 23, 2026\n\n" +
                                "1. Information We Collect\n" +
                                "• Account Identity: When you sign in with Google via Jetpack Credential Manager and Firebase Authentication, we receive your unique Firebase User ID (UID) and display name.\n" +
                                "• Household & Budget Data: Weekly/monthly grocery budget targets, household size, dietary preferences, digital pantry items, meal plans, shopping lists, and logged purchase receipts.\n\n" +
                                "2. How Your Data Is Used & Protected\n" +
                                "• Strict UID Isolation: All documents in Cloud Firestore (/users/{userId}) are enforced by zero-trust security rules so only your authenticated Google account can read or write your records.\n" +
                                "• AI Meal Planning & Receipt Scanning: AI requests are processed through Firebase AI Logic protected by Firebase App Check. No raw Gemini API key is stored in the APK.\n\n" +
                                "3. Data Sharing & Retention\n" +
                                "• We never sell your personal or household data to third-party advertisers or data brokers.\n" +
                                "• You can export your data as JSON, delete your cloud data while keeping your account, or permanently delete your entire account at any time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    ComplianceTab.TERMS -> {
                        ComplianceUrlBox(
                            title = "GitHub Pages Terms of Service URL",
                            url = BasketBrainComplianceUrls.githubPagesTermsUrl(),
                            onCopy = {
                                copyUrlToClipboard(context, "GitHub Pages Terms URL", BasketBrainComplianceUrls.githubPagesTermsUrl())
                                copiedNotice = "GitHub Pages Terms of Service URL copied."
                            },
                            onOpenBrowser = {
                                openExternalUrl(context, BasketBrainComplianceUrls.githubPagesTermsUrl())
                            }
                        )
                        ComplianceUrlBox(
                            title = "GitHub Repo Terms of Service Markdown URL",
                            url = BasketBrainComplianceUrls.githubRepoTermsUrl(),
                            onCopy = {
                                copyUrlToClipboard(context, "GitHub Repo Terms URL", BasketBrainComplianceUrls.githubRepoTermsUrl())
                                copiedNotice = "GitHub Repo Terms of Service URL copied."
                            },
                            onOpenBrowser = {
                                openExternalUrl(context, BasketBrainComplianceUrls.githubRepoTermsUrl())
                            }
                        )
                        Text(
                            text = "Last Updated: October 23, 2026\n\n" +
                                "1. Service Scope\n" +
                                "BasketBrain is a grocery budgeting, meal planning, and pantry management tool designed to help households plan affordable meals and reduce food waste.\n\n" +
                                "2. Grocery Price Estimates Disclaimer\n" +
                                "Recipe costs and ingredient prices shown in BasketBrain are estimates or user-entered receipt values. BasketBrain does not guarantee real-time supermarket pricing or store inventory availability.\n\n" +
                                "3. Allergen, Nutrition & Food Safety Disclaimer\n" +
                                "AI-generated and algorithmic meal plans, leftover rescue suggestions, and nutritional figures are informational estimates only. Always inspect physical product packaging for allergens and verify safe food storage temperatures (reheat leftovers to 74°C / 165°F).\n\n" +
                                "4. Account Control\n" +
                                "You retain full ownership of your household records and may export or delete them at any time via the Profile tab or our Account & Data Deletion URLs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    ComplianceTab.DELETE_PORTAL -> {
                        ComplianceUrlBox(
                            title = "Delete Account Form URL (GitHub Pages)",
                            url = BasketBrainComplianceUrls.githubPagesDeleteAccountUrl(),
                            onCopy = {
                                copyUrlToClipboard(context, "Delete Account URL", BasketBrainComplianceUrls.githubPagesDeleteAccountUrl())
                                copiedNotice = "GitHub Pages Delete Account URL copied."
                            },
                            onOpenBrowser = {
                                openExternalUrl(context, BasketBrainComplianceUrls.githubPagesDeleteAccountUrl())
                            }
                        )

                        ComplianceUrlBox(
                            title = "Delete Data Only Form URL (GitHub Pages)",
                            url = BasketBrainComplianceUrls.githubPagesDeleteDataUrl(),
                            onCopy = {
                                copyUrlToClipboard(context, "Delete Data URL", BasketBrainComplianceUrls.githubPagesDeleteDataUrl())
                                copiedNotice = "GitHub Pages Delete Data URL copied."
                            },
                            onOpenBrowser = {
                                openExternalUrl(context, BasketBrainComplianceUrls.githubPagesDeleteDataUrl())
                            }
                        )

                        ComplianceUrlBox(
                            title = "Delete Account URL (GitHub Repo Markdown)",
                            url = BasketBrainComplianceUrls.githubRepoDeleteAccountUrl(),
                            onCopy = {
                                copyUrlToClipboard(context, "GitHub Repo Delete Account URL", BasketBrainComplianceUrls.githubRepoDeleteAccountUrl())
                                copiedNotice = "GitHub Repo Delete Account URL copied."
                            },
                            onOpenBrowser = {
                                openExternalUrl(context, BasketBrainComplianceUrls.githubRepoDeleteAccountUrl())
                            }
                        )

                        ComplianceUrlBox(
                            title = "Delete Data Only URL (GitHub Repo Markdown)",
                            url = BasketBrainComplianceUrls.githubRepoDeleteDataUrl(),
                            onCopy = {
                                copyUrlToClipboard(context, "GitHub Repo Delete Data URL", BasketBrainComplianceUrls.githubRepoDeleteDataUrl())
                                copiedNotice = "GitHub Repo Delete Data URL copied."
                            },
                            onOpenBrowser = {
                                openExternalUrl(context, BasketBrainComplianceUrls.githubRepoDeleteDataUrl())
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        if (isSignedIn) {
                            Text(
                                text = "Instant Self-Service Deletion (Signed In)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Choose whether to clear only your cloud data (keeping your Google login active) or permanently delete your entire account and all cloud records:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (onDeleteCloudDataOnly != null) {
                                OutlinedButton(
                                    onClick = {
                                        onDeleteCloudDataOnly()
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("dialog_delete_data_only_button")
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Delete My Cloud Data Only (Keep Account)")
                                }
                            }
                            if (onDeleteFullAccount != null) {
                                Button(
                                    onClick = {
                                        onDeleteFullAccount()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("dialog_delete_full_account_button")
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Delete My Account & All Cloud Data")
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                        Text(
                            text = "Unauthenticated Deletion Request Form",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "In compliance with Google Play Data Safety policy, you can also request data-only deletion or full account deletion without signing in:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = requestScope == "DELETE_DATA_ONLY",
                                onClick = { requestScope = "DELETE_DATA_ONLY" },
                                label = { Text("Delete Data Only") }
                            )
                            FilterChip(
                                selected = requestScope == "DELETE_ACCOUNT_AND_DATA",
                                onClick = { requestScope = "DELETE_ACCOUNT_AND_DATA" },
                                label = { Text("Delete Account + Data") }
                            )
                        }

                        OutlinedTextField(
                            value = requestEmail,
                            onValueChange = {
                                requestEmail = it
                                requestSubmitted = false
                            },
                            label = { Text("Google Account Email") },
                            placeholder = { Text("you@example.com") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("deletion_request_email_input")
                        )

                        if (requestSubmitted) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Deletion request recorded for ${requestEmail.ifBlank { "your account" }} ($requestScope). To complete instantly, sign in and tap Delete in Profile, or send the pre-filled email.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                requestSubmitted = true
                                launchDeletionEmailIntent(
                                    context = context,
                                    accountEmail = requestEmail,
                                    deletionScope = requestScope
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_deletion_request_button")
                        ) {
                            Text("Submit Deletion Request")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun ComplianceUrlBox(
    title: String,
    url: String,
    onCopy: () -> Unit,
    onOpenBrowser: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy URL", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onOpenBrowser,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open URL", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private fun copyUrlToClipboard(context: Context, label: String, url: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, url))
}

private fun openExternalUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        copyUrlToClipboard(context, "URL", url)
    }
}

private fun launchDeletionEmailIntent(
    context: Context,
    accountEmail: String,
    deletionScope: String
) {
    try {
        val subject = Uri.encode("BasketBrain $deletionScope Request")
        val body = Uri.encode(
            "Please process a $deletionScope request for my BasketBrain account.\n" +
                "Account Email: ${accountEmail.ifBlank { "[Enter Google Account Email]" }}\n" +
                "Deletion URL: ${if (deletionScope == "DELETE_DATA_ONLY") BasketBrainComplianceUrls.DELETE_DATA_URL else BasketBrainComplianceUrls.DELETE_ACCOUNT_URL}"
        )
        val mailtoUri = Uri.parse("mailto:${BasketBrainComplianceUrls.SUPPORT_EMAIL}?subject=$subject&body=$body")
        val intent = Intent(Intent.ACTION_SENDTO, mailtoUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Safe fallback if no email client is installed on emulator
    }
}
