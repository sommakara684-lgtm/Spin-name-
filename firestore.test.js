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

test("Unauthenticated user: cannot read Alice's wheels", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("wheels").get());
});

test("Authenticated Alice: can create her own wheel", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("wheels").doc("wheel_1").set({
      userId: ALICE_UID,
      title: "Classroom Names",
      themePalette: "RAINBOW",
      spinDurationSeconds: 4.5,
      autoRemoveWinner: false,
      soundEnabled: true,
      hapticsEnabled: true,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Authenticated Bob: cannot read or write Alice's wheel", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("wheels").doc("wheel_1").set({
      userId: ALICE_UID,
      title: "Alice's Wheel",
      themePalette: "RAINBOW",
      spinDurationSeconds: 4.5,
      autoRemoveWinner: false,
      soundEnabled: true,
      hapticsEnabled: true,
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("wheels").doc("wheel_1").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("wheels").doc("wheel_2").set({
      userId: BOB_UID,
      title: "Bob Spoof",
      themePalette: "RAINBOW",
      spinDurationSeconds: 4.0,
      autoRemoveWinner: false,
      soundEnabled: true,
      hapticsEnabled: true,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Authenticated Alice: can add item to her wheel", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("wheels").doc("wheel_1").collection("items").doc("item_1").set({
      wheelId: "wheel_1",
      userId: ALICE_UID,
      name: "Student A",
      isEnabled: true,
      weight: 1,
      orderIndex: 0,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Spin history: immutable once created", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const histDoc = aliceDb.collection("users").doc(ALICE_UID).collection("wheels").doc("wheel_1").collection("history").doc("h_1");

  await assertSucceeds(
    histDoc.set({
      wheelId: "wheel_1",
      userId: ALICE_UID,
      winnerName: "Student A",
      timestamp: new Date()
    })
  );

  await assertFails(
    histDoc.update({
      winnerName: "Hacked Winner"
    })
  );
});
