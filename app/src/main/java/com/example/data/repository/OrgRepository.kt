package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ActivityNotice
import com.example.data.model.DonationRecord
import com.example.data.model.DuaFavorite
import com.example.data.model.NoticePriority
import com.example.data.model.OrgMember
import com.example.data.model.QuranBookmark
import com.example.data.model.TasbihState
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class OrgRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("ব্যবহারকারীকে প্রথমে Google দিয়ে সাইন ইন করতে হবে।")
    }

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    suspend fun ensureUserProfile(displayName: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid"
        return try {
            val safeName = displayName.trim().ifBlank { "সম্মানিত সদস্য" }.take(100)
            val snapshot = userDoc(uid).get().await()
            if (!snapshot.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "orgRole" to "কার্যকরী সদস্য",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userDoc(uid).set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            userDoc(userId).snapshots()
                .map { it.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> logAndRethrowStreamError(error, OperationType.GET, path, userId) }
        )
    }

    // --- MEMBERS ---

    fun observeMembers(userId: String): Flow<List<OrgMember>> = flow {
        val path = "users/$userId/members"
        emitAll(
            userDoc(userId).collection("members")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(OrgMember::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.fullName }
                }
                .catch { error -> logAndRethrowStreamError(error, OperationType.LIST, path, userId) }
        )
    }

    suspend fun saveMember(member: OrgMember): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/members"
        return try {
            val uid = requireUserId()
            val fullName = member.fullName.trim()
            val designation = member.designation.trim()
            val phone = member.phone.trim()
            val bloodGroup = member.bloodGroup.trim()
            val ward = member.ward.trim()
            val joinedLabel = member.joinedDateLabel.trim()
            val notes = member.notes.trim()

            require(fullName.length in 1..100) { "সদস্যের নাম ১ থেকে ১০০ অক্ষরের মধ্যে হতে হবে।" }
            require(designation.length in 1..80) { "পদবী লিখুন।" }
            require(phone.length in 3..25) { "সঠিক মোবাইল নম্বর দিন।" }
            require(bloodGroup.length in 1..20) { "রক্তের গ্রুপ নির্বাচন করুন।" }
            require(ward.length in 1..100) { "এলাকা বা পাড়া উল্লেখ করুন।" }
            require(member.monthlyFee in 0L..1000000L) { "মাসিক চাঁদা ০ থেকে ১০,০০,০০০ টাকার মধ্যে হতে হবে।" }
            require(joinedLabel.length in 1..40) { "যোগদানের তারিখ দিন।" }
            require(notes.length <= 500) { "নোট ৫০০ অক্ষরের বেশি হতে পারবে না।" }

            val colRef = userDoc(uid).collection("members")
            if (member.id.isBlank()) {
                val docRef = colRef.document()
                docPath = docRef.path
                val payload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "fullName" to fullName,
                    "designation" to designation,
                    "phone" to phone,
                    "bloodGroup" to bloodGroup,
                    "ward" to ward,
                    "monthlyFee" to member.monthlyFee,
                    "activeStatus" to member.activeStatus,
                    "joinedDateLabel" to joinedLabel,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (notes.isNotEmpty()) payload["notes"] = notes
                docRef.set(payload).await()
            } else {
                val docRef = colRef.document(member.id)
                docPath = docRef.path
                val updates = mutableMapOf<String, Any>(
                    "fullName" to fullName,
                    "designation" to designation,
                    "phone" to phone,
                    "bloodGroup" to bloodGroup,
                    "ward" to ward,
                    "monthlyFee" to member.monthlyFee,
                    "activeStatus" to member.activeStatus,
                    "joinedDateLabel" to joinedLabel,
                    "notes" to notes,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteMember(memberId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/members/$memberId"
        return try {
            val uid = requireUserId()
            require(memberId.isNotBlank())
            val docRef = userDoc(uid).collection("members").document(memberId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- DONATIONS & EXPENSES ---

    fun observeDonations(userId: String): Flow<List<DonationRecord>> = flow {
        val path = "users/$userId/donations"
        emitAll(
            userDoc(userId).collection("donations")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(DonationRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error -> logAndRethrowStreamError(error, OperationType.LIST, path, userId) }
        )
    }

    suspend fun saveDonation(record: DonationRecord): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/donations"
        return try {
            val uid = requireUserId()
            val titleOrDonor = record.titleOrDonor.trim()
            val txType = TransactionType.fromRaw(record.txType).name
            val category = record.category.trim()
            val paymentMethod = record.paymentMethod.trim()
            val dateLabel = record.dateLabel.trim()
            val receiptNumber = record.receiptNumber.trim()
            val note = record.note.trim()

            require(titleOrDonor.length in 1..120) { "দাতা বা বিবরণের নাম লিখুন।" }
            require(category.length in 1..80) { "খাত নির্বাচন করুন।" }
            require(record.amount in 1L..100000000L) { "টাকার পরিমাণ কমপক্ষে ১ টাকা হতে হবে।" }
            require(paymentMethod.length in 1..40) { "মাধ্যম নির্বাচন করুন।" }
            require(dateLabel.length in 1..40) { "তারিখ লিখুন।" }
            require(receiptNumber.length <= 60) { "রশিদ নম্বর ৬০ অক্ষরের বেশি হতে পারবে না।" }
            require(note.length <= 500) { "মন্তব্য ৫০০ অক্ষরের বেশি হতে পারবে না।" }

            val colRef = userDoc(uid).collection("donations")
            if (record.id.isBlank()) {
                val docRef = colRef.document()
                docPath = docRef.path
                val payload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "titleOrDonor" to titleOrDonor,
                    "txType" to txType,
                    "category" to category,
                    "amount" to record.amount,
                    "paymentMethod" to paymentMethod,
                    "dateLabel" to dateLabel,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (receiptNumber.isNotEmpty()) payload["receiptNumber"] = receiptNumber
                if (note.isNotEmpty()) payload["note"] = note
                docRef.set(payload).await()
            } else {
                val docRef = colRef.document(record.id)
                docPath = docRef.path
                val updates = mutableMapOf<String, Any>(
                    "titleOrDonor" to titleOrDonor,
                    "txType" to txType,
                    "category" to category,
                    "amount" to record.amount,
                    "paymentMethod" to paymentMethod,
                    "dateLabel" to dateLabel,
                    "receiptNumber" to receiptNumber,
                    "note" to note,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteDonation(donationId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/donations/$donationId"
        return try {
            val uid = requireUserId()
            require(donationId.isNotBlank())
            val docRef = userDoc(uid).collection("donations").document(donationId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- ACTIVITIES & NOTIFICATIONS ---

    fun observeActivities(userId: String): Flow<List<ActivityNotice>> = flow {
        val path = "users/$userId/activities"
        emitAll(
            userDoc(userId).collection("activities")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ActivityNotice::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error -> logAndRethrowStreamError(error, OperationType.LIST, path, userId) }
        )
    }

    suspend fun saveActivity(notice: ActivityNotice): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/activities"
        return try {
            val uid = requireUserId()
            val title = notice.title.trim()
            val description = notice.description.trim()
            val category = notice.category.trim()
            val dateLabel = notice.eventDateLabel.trim()
            val timeLabel = notice.eventTimeLabel.trim()
            val venue = notice.venue.trim()
            val priority = NoticePriority.fromRaw(notice.priority).name

            require(title.length in 1..150) { "কার্যক্রমের শিরোনাম লিখুন।" }
            require(description.length in 1..2000) { "বিস্তারিত বিবরণ লিখুন।" }
            require(category.length in 1..60) { "ধরন নির্বাচন করুন।" }
            require(dateLabel.length in 1..50) { "তারিখ লিখুন।" }
            require(timeLabel.length in 1..50) { "সময় লিখুন।" }
            require(venue.length in 1..150) { "স্থান লিখুন।" }

            val colRef = userDoc(uid).collection("activities")
            if (notice.id.isBlank()) {
                val docRef = colRef.document()
                docPath = docRef.path
                val payload = mapOf(
                    "userId" to uid,
                    "title" to title,
                    "description" to description,
                    "category" to category,
                    "eventDateLabel" to dateLabel,
                    "eventTimeLabel" to timeLabel,
                    "venue" to venue,
                    "priority" to priority,
                    "readStatus" to notice.readStatus,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val docRef = colRef.document(notice.id)
                docPath = docRef.path
                val updates = mapOf(
                    "title" to title,
                    "description" to description,
                    "category" to category,
                    "eventDateLabel" to dateLabel,
                    "eventTimeLabel" to timeLabel,
                    "venue" to venue,
                    "priority" to priority,
                    "readStatus" to notice.readStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun markActivityRead(activityId: String, read: Boolean): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/activities/$activityId"
        return try {
            val uid = requireUserId()
            require(activityId.isNotBlank())
            val docRef = userDoc(uid).collection("activities").document(activityId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "readStatus" to read,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteActivity(activityId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/activities/$activityId"
        return try {
            val uid = requireUserId()
            require(activityId.isNotBlank())
            val docRef = userDoc(uid).collection("activities").document(activityId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- QURAN BOOKMARKS ---

    fun observeQuranBookmarks(userId: String): Flow<List<QuranBookmark>> = flow {
        val path = "users/$userId/quran_bookmarks"
        emitAll(
            userDoc(userId).collection("quran_bookmarks")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(QuranBookmark::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error -> logAndRethrowStreamError(error, OperationType.LIST, path, userId) }
        )
    }

    suspend fun toggleQuranBookmark(
        surahNumber: Int,
        ayahNumber: Int,
        surahNameBn: String,
        surahNameAr: String,
        ayahTextAr: String,
        ayahTextBn: String,
        currentlyBookmarked: Boolean
    ): Result<Unit> {
        val docId = "bm_${surahNumber}_${ayahNumber}"
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/quran_bookmarks/$docId"
        return try {
            val uid = requireUserId()
            val docRef = userDoc(uid).collection("quran_bookmarks").document(docId)
            docPath = docRef.path
            if (currentlyBookmarked) {
                docRef.delete().await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "surahNumber" to surahNumber.toLong(),
                    "ayahNumber" to ayahNumber.toLong(),
                    "surahNameBn" to surahNameBn.take(100).ifBlank { "সূরা" },
                    "surahNameAr" to surahNameAr.take(100).ifBlank { "سورة" },
                    "ayahTextAr" to ayahTextAr.take(4000).ifBlank { "آية" },
                    "ayahTextBn" to ayahTextBn.take(4000).ifBlank { "আয়াত" },
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    // --- DUA FAVORITES ---

    fun observeDuaFavorites(userId: String): Flow<List<DuaFavorite>> = flow {
        val path = "users/$userId/dua_favorites"
        emitAll(
            userDoc(userId).collection("dua_favorites")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(DuaFavorite::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error -> logAndRethrowStreamError(error, OperationType.LIST, path, userId) }
        )
    }

    suspend fun toggleDuaFavorite(
        duaId: String,
        titleBn: String,
        category: String,
        currentlyFavorite: Boolean
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/dua_favorites/$duaId"
        return try {
            val uid = requireUserId()
            require(duaId.matches(Regex("^[a-zA-Z0-9_\\-]+$")))
            val docRef = userDoc(uid).collection("dua_favorites").document(duaId)
            docPath = docRef.path
            if (currentlyFavorite) {
                docRef.delete().await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "duaId" to duaId,
                    "titleBn" to titleBn.take(160),
                    "category" to category.take(80),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    // --- TASBIH PROGRESS ---

    fun observeTasbihState(userId: String): Flow<TasbihState?> = flow {
        val path = "users/$userId/tasbih_progress/default"
        emitAll(
            userDoc(userId).collection("tasbih_progress").document("default")
                .snapshots()
                .map { it.toObject(TasbihState::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> logAndRethrowStreamError(error, OperationType.GET, path, userId) }
        )
    }

    suspend fun saveTasbihState(
        dhikrName: String,
        currentCount: Long,
        targetCount: Long,
        lifetimeCount: Long
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/tasbih_progress/default"
        return try {
            val uid = requireUserId()
            val docRef = userDoc(uid).collection("tasbih_progress").document("default")
            docPath = docRef.path
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "dhikrName" to dhikrName.take(120).ifBlank { "সুবহানাল্লাহ" },
                        "currentCount" to currentCount.coerceIn(0L, 100000L),
                        "targetCount" to targetCount.coerceIn(1L, 100000L),
                        "lifetimeCount" to lifetimeCount.coerceIn(0L, 100000000L),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "dhikrName" to dhikrName.take(120).ifBlank { "সুবহানাল্লাহ" },
                        "currentCount" to currentCount.coerceIn(0L, 100000L),
                        "targetCount" to targetCount.coerceIn(1L, 100000L),
                        "lifetimeCount" to lifetimeCount.coerceIn(0L, 100000000L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    private fun logAndRethrowStreamError(
        error: Throwable,
        operationType: OperationType,
        path: String,
        uid: String
    ): Nothing {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }
}
