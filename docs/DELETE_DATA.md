# BasketBrain — Delete Cloud Data Only (Keep Account Active)

**Application:** BasketBrain — Smart Grocery Budget & Meal Planner (`com.aistudio.basketbrain.kxmprz`)

## Option 1: Instant In-App Data Deletion
1. Open the **BasketBrain** Android app.
2. Go to the **Profile** tab → **Data & Account Management**.
3. Tap **Delete Cloud Data Only (Keep Account)** and confirm. All meal plans, pantry items, grocery lists, household members, and purchase records are permanently erased while your Google account login stays active.

## Option 2: Web / Email Data-Only Deletion Request (No App Required)
- **Interactive Web Form (GitHub Pages):** [delete-data.html](./delete-data.html)
- **Direct Email Request:** Send an email from your registered Google Account to **[privacy@basketbrain.app](mailto:privacy@basketbrain.app?subject=BasketBrain%20DELETE_DATA_ONLY%20Request)** specifying whether you want to delete:
  - All Cloud Data (`ALL_CLOUD_DATA_KEEP_ACCOUNT`)
  - Purchase & Receipt History Only (`PURCHASE_AND_RECEIPT_HISTORY`)
  - Pantry Inventory & Meal Plans Only (`PANTRY_AND_MEAL_PLANS`)

## Data Retention
Requested data categories are permanently erased from Cloud Firestore within **7 business days** (or **immediately** when triggered in-app) with **0 days** post-deletion retention.
