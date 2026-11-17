const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_001";
const BOB_UID = "bob_uid_002";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
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

test("Unauthenticated access is denied across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("members").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("donations").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("activities").get()
  );
});

test("UserProfile create, update, and cross-user denial", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "আব্দুল্লাহ আল মামুন",
      orgRole: "সদস্য",
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "মাওলানা আব্দুল্লাহ",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user read/write blocked
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Future timestamp rejected
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      orgRole: "সদস্য",
      createdAt: new Date(Date.now() + 60000),
      updatedAt: past,
    })
  );
});

test("OrgMember Kotlin payload create, partial update, list, and security bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 10000);

  const memberRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("members")
    .doc("member_01");

  await assertSucceeds(
    memberRef.set({
      userId: ALICE_UID,
      fullName: "মোহাম্মদ রাশেদুল ইসলাম",
      designation: "সভাপতি",
      phone: "01812345678",
      bloodGroup: "O+",
      ward: "মুকিয়াঘোনা মধ্যপাড়া",
      monthlyFee: 200,
      activeStatus: true,
      joinedDateLabel: "১৭ নভেম্বর ২০২৬",
      notes: "প্রতিষ্ঠাতা সদস্য",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Partial update
  await assertSucceeds(
    memberRef.update({
      designation: "সাধারণ সম্পাদক",
      monthlyFee: 300,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // List own members succeeds (including empty/filtered)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("members").get()
  );

  // Reject ghost `id` field
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("members")
      .doc("member_02")
      .set({
        id: "member_02",
        userId: ALICE_UID,
        fullName: "Ghost ID",
        designation: "সদস্য",
        phone: "01700000000",
        bloodGroup: "A+",
        ward: "পূর্ব হারবাং",
        monthlyFee: 100,
        activeStatus: true,
        joinedDateLabel: "আজ",
        createdAt: past,
        updatedAt: past,
      })
  );

  // Reject cross-user read/write
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("members").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("members")
      .doc("member_01")
      .delete()
  );
});

test("DonationRecord Kotlin payload create, update, and validation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const past = new Date(Date.now() - 10000);

  const donRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("donations")
    .doc("don_01");

  await assertSucceeds(
    donRef.set({
      userId: ALICE_UID,
      titleOrDonor: "হাজী আব্দুর রহমান",
      txType: "INCOME",
      category: "মসজিদ ও মাদ্রাসা তহবিল",
      amount: 5000,
      paymentMethod: "নগদ",
      dateLabel: "১৭ নভেম্বর ২০২৬",
      receiptNumber: "REC-101",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Reject invalid txType or negative amount
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("donations")
      .doc("don_bad")
      .set({
        userId: ALICE_UID,
        titleOrDonor: "Invalid",
        txType: "STEAL",
        category: "অন্যান্য",
        amount: 0,
        paymentMethod: "নগদ",
        dateLabel: "আজ",
        createdAt: past,
        updatedAt: past,
      })
  );

  // Update mutable fields only
  await assertSucceeds(
    donRef.update({
      amount: 5500,
      note: "আপডেট করা হয়েছে",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("ActivityNotice, QuranBookmark, DuaFavorite, TasbihState parity checks", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const past = new Date(Date.now() - 10000);

  const actRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("activities")
    .doc("act_01");

  await assertSucceeds(
    actRef.set({
      userId: ALICE_UID,
      title: "মাসিক ইসলামিক আলোচনা সভা",
      description: "সকল সদস্যের উপস্থিতি একান্ত কাম্য।",
      category: "সভা",
      eventDateLabel: "২০ নভেম্বর ২০২৬",
      eventTimeLabel: "বাদ আসর",
      venue: "মুকিয়াঘোনা জামে মসজিদ",
      priority: "URGENT",
      readStatus: false,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Action: mark read only
  await assertSucceeds(
    actRef.update({
      readStatus: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Quran bookmark
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("quran_bookmarks")
      .doc("bm_1_1")
      .set({
        userId: ALICE_UID,
        surahNumber: 1,
        ayahNumber: 1,
        surahNameBn: "আল-ফাতিহা",
        surahNameAr: "الفاتحة",
        ayahTextAr: "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        ayahTextBn: "পরম করুণাময় অসীম দয়ালু আল্লাহর নামে শুরু করছি।",
        createdAt: past,
        updatedAt: past,
      })
  );

  // Dua favorite
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("dua_favorites")
      .doc("dua_morning_1")
      .set({
        userId: ALICE_UID,
        duaId: "dua_morning_1",
        titleBn: "ঘুম থেকে জাগ্রত হওয়ার দোয়া",
        category: "ঘুম ও জাগ্রত",
        createdAt: past,
        updatedAt: past,
      })
  );

  // Tasbih state
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("tasbih_progress")
      .doc("default")
      .set({
        userId: ALICE_UID,
        dhikrName: "সুবহানাল্লাহ",
        currentCount: 15,
        targetCount: 33,
        lifetimeCount: 150,
        createdAt: past,
        updatedAt: past,
      })
  );
});
