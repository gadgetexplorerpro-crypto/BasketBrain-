# BasketBrain — Delete Account & All Data

**Application:** BasketBrain — Smart Grocery Budget & Meal Planner (`com.aistudio.basketbrain.kxmprz`)

## Option 1: Instant In-App Account Deletion
1. Open the **BasketBrain** Android app and sign in with your Google account.
2. Go to the **Profile** tab → **Data & Account Management**.
3. Tap **Delete Account & All Cloud Data** and confirm. Your Firebase Authentication account and all Cloud Firestore documents under `/users/{userId}` are permanently deleted immediately.

## Option 2: Web / Email Account Deletion Request (No App Required)
If you have uninstalled the app, you can request permanent deletion of your BasketBrain account and all associated data via our web form or direct email:
- **Interactive Web Form (GitHub Pages):** [delete-account.html](./delete-account.html)
- **Direct Email Request:** Send an email from your registered Google Account to **[privacy@basketbrain.app](mailto:privacy@basketbrain.app?subject=BasketBrain%20DELETE_ACCOUNT_AND_ALL_DATA%20Request)** with the subject `BasketBrain DELETE_ACCOUNT_AND_ALL_DATA Request`.

## Data Deleted & Retention Policy
- **Deleted Immediately (0-Day Retention):**
  - Firebase Authentication user record (`uid`, display name, email)
  - User profile document (`/users/{userId}`)
  - Household member profiles (`members`)
  - Planned meals (`planned_meals`), pantry inventory (`pantry_items`), smart grocery list (`grocery_items`), and purchase history (`purchases`)
- **Want to delete data without deleting your account?** See [DELETE_DATA.md](./DELETE_DATA.md).
