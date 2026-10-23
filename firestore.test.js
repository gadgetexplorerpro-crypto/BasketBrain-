const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write user profile or pantry items", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("pantry_items").get()
  );
});

test("Authenticated user: cannot read another user's pantry items or purchases", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("pantry_items")
      .doc("item_1")
      .set({
        userId: BOB_UID,
        name: "Basmati Rice",
        quantity: 2.0,
        unit: "kg",
        category: "Grains",
        storageLocation: "Cupboard",
        createdAt: new Date(),
        updatedAt: new Date(),
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("pantry_items")
      .doc("item_1")
      .get()
  );
});

test("Authenticated owner: can create and query own pantry items with valid schema", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("pantry_items")
      .doc("rice_1")
      .set({
        userId: ALICE_UID,
        name: "Brown Rice",
        quantity: 1.5,
        unit: "kg",
        category: "Grains",
        storageLocation: "Cupboard",
        createdAt: now,
        updatedAt: now,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("pantry_items")
      .where("userId", "==", ALICE_UID)
      .get()
  );
});

test("Shadow Update Test: rejects ghost fields on pantry item creation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("pantry_items")
      .doc("rice_ghost")
      .set({
        userId: ALICE_UID,
        name: "Brown Rice",
        quantity: 1.5,
        unit: "kg",
        category: "Grains",
        storageLocation: "Cupboard",
        isAdminBypass: true,
        createdAt: now,
        updatedAt: now,
      })
  );
});

test("Identity Spoofing Test: rejects creating document with mismatched userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("purchases")
      .doc("purchase_spoof")
      .set({
        userId: BOB_UID,
        storeName: "Fresh Mart",
        purchaseDate: "2026-10-22",
        category: "Vegetables",
        totalAmountCents: 1500,
        itemsSummary: "Carrots, Onions",
        isFromReceipt: false,
        createdAt: now,
        updatedAt: now,
      })
  );
});
