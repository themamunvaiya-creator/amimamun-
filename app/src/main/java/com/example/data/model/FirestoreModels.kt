package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val orgRole: String = "সদস্য",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class OrgMember(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val designation: String = "সদস্য",
    val phone: String = "",
    val bloodGroup: String = "O+",
    val ward: String = "পূর্ব হারবাং",
    val monthlyFee: Long = 100L,
    val activeStatus: Boolean = true,
    val joinedDateLabel: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class TransactionType(val labelBn: String) {
    INCOME("আয় / দান"),
    EXPENSE("ব্যয়");

    companion object {
        fun fromRaw(raw: String): TransactionType =
            entries.firstOrNull { it.name == raw } ?: INCOME
    }
}

@Keep
@IgnoreExtraProperties
data class DonationRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val titleOrDonor: String = "",
    val txType: String = TransactionType.INCOME.name,
    val category: String = "মাসিক চাঁদা",
    val amount: Long = 0L,
    val paymentMethod: String = "নগদ",
    val dateLabel: String = "",
    val receiptNumber: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class NoticePriority(val labelBn: String) {
    NORMAL("সাধারণ"),
    URGENT("জরুরি");

    companion object {
        fun fromRaw(raw: String): NoticePriority =
            entries.firstOrNull { it.name == raw } ?: NORMAL
    }
}

@Keep
@IgnoreExtraProperties
data class ActivityNotice(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "সভা",
    val eventDateLabel: String = "",
    val eventTimeLabel: String = "",
    val venue: String = "মুকিয়াঘোনা জামে মসজিদ",
    val priority: String = NoticePriority.NORMAL.name,
    val readStatus: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class QuranBookmark(
    @DocumentId val id: String = "",
    val userId: String = "",
    val surahNumber: Long = 1L,
    val ayahNumber: Long = 1L,
    val surahNameBn: String = "",
    val surahNameAr: String = "",
    val ayahTextAr: String = "",
    val ayahTextBn: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DuaFavorite(
    @DocumentId val id: String = "",
    val userId: String = "",
    val duaId: String = "",
    val titleBn: String = "",
    val category: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TasbihState(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dhikrName: String = "সুবহানাল্লাহ",
    val currentCount: Long = 0L,
    val targetCount: Long = 33L,
    val lifetimeCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
