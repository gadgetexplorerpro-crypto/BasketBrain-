# BasketBrain — Privacy Policy

**Effective Date:** October 23, 2026  
**Application:** BasketBrain — Smart Grocery Budget & Meal Planner (`com.aistudio.basketbrain.kxmprz`)

## 1. Information We Collect
- **Account Identity:** When you sign in with Google via Android Credential Manager and Firebase Authentication, we store your Firebase User ID (UID) and display name to isolate and sync your household data.
- **Household & Grocery Data:** Weekly/monthly grocery budgets, household member profiles, dietary preferences, allergies, digital pantry items, planned meals, shopping lists, and logged purchase records.
- **Receipt Images:** When you use the Receipt Scanner, receipt images are processed securely via Firebase AI Logic to extract line items and totals.

## 2. How We Use & Protect Your Data
- **Strict UID Isolation:** All user records are stored in Google Cloud Firestore under `/users/{userId}` and protected by zero-trust Firestore security rules so only your authenticated Google account can access your records.
- **Enterprise AI Security:** AI meal planning and receipt OCR run through Firebase AI Logic attested by Firebase App Check. No raw Gemini API keys are embedded in the Android APK.
- **No Data Selling:** BasketBrain does not sell, rent, or share your personal or household data with advertisers or data brokers.

## 3. Data Retention, Export & Deletion
You may export your household data as JSON, delete your cloud data while keeping your account active, or permanently delete your account and all associated data at any time:
- **In-App:** Open BasketBrain → **Profile** → **Data & Account Management** → tap **Delete Cloud Data Only** or **Delete Account & All Cloud Data**.
- **Account Deletion Instructions & Form:** [DELETE_ACCOUNT.md](./DELETE_ACCOUNT.md) | [delete-account.html](./delete-account.html)
- **Data-Only Deletion Instructions & Form:** [DELETE_DATA.md](./DELETE_DATA.md) | [delete-data.html](./delete-data.html)

## 4. Contact
Email: [privacy@basketbrain.app](mailto:privacy@basketbrain.app)
