package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActivityNotice
import com.example.data.model.DonationRecord
import com.example.data.model.DuaFavorite
import com.example.data.model.OrgMember
import com.example.data.model.QuranBookmark
import com.example.data.model.TasbihState
import com.example.data.model.TransactionType
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.repository.OrgRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinancialSummary(
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val balance: Long = 0L,
    val categoryTotals: Map<String, Long> = emptyMap()
)

class OrgViewModel(
    private val repository: OrgRepository,
    private val currentUserId: String,
    private val currentUserDisplayName: String
) : ViewModel() {

    private companion object {
        const val TAG = "OrgViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }

    private val _feedbackMessage = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val feedbackMessage = _feedbackMessage.asSharedFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureUserProfile(currentUserDisplayName)
        }
    }

    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
        .catch { error ->
            if (isPureCancellation(error)) throw error
            Log.w(TAG, "Profile stream error", unwrapFirestoreError(error))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    val membersState: StateFlow<UiState<List<OrgMember>>> = repository.observeMembers(currentUserId)
        .toUiStateFlow("সদস্য তালিকা লোড করতে সমস্যা হয়েছে")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val donationsState: StateFlow<UiState<List<DonationRecord>>> = repository.observeDonations(currentUserId)
        .toUiStateFlow("দানের হিসাব লোড করতে সমস্যা হয়েছে")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val financialSummary: StateFlow<FinancialSummary> = donationsState
        .map { state ->
            when (state) {
                is UiState.Success -> {
                    val income = state.data
                        .filter { TransactionType.fromRaw(it.txType) == TransactionType.INCOME }
                        .sumOf { it.amount }
                    val expense = state.data
                        .filter { TransactionType.fromRaw(it.txType) == TransactionType.EXPENSE }
                        .sumOf { it.amount }
                    val byCategory = state.data
                        .filter { TransactionType.fromRaw(it.txType) == TransactionType.INCOME }
                        .groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }
                    FinancialSummary(
                        totalIncome = income,
                        totalExpense = expense,
                        balance = income - expense,
                        categoryTotals = byCategory
                    )
                }
                else -> FinancialSummary()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), FinancialSummary())

    val activitiesState: StateFlow<UiState<List<ActivityNotice>>> = repository.observeActivities(currentUserId)
        .toUiStateFlow("কার্যক্রম লোড করতে সমস্যা হয়েছে")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val unreadNoticeCount: StateFlow<Int> = activitiesState
        .map { state ->
            when (state) {
                is UiState.Success -> state.data.count { !it.readStatus }
                else -> 0
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), 0)

    val quranBookmarksState: StateFlow<UiState<List<QuranBookmark>>> = repository.observeQuranBookmarks(currentUserId)
        .toUiStateFlow("কোরআন বুকমার্ক লোড করতে সমস্যা হয়েছে")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val duaFavoritesState: StateFlow<UiState<List<DuaFavorite>>> = repository.observeDuaFavorites(currentUserId)
        .toUiStateFlow("প্রিয় দোয়া লোড করতে সমস্যা হয়েছে")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val tasbihState: StateFlow<TasbihState> = repository.observeTasbihState(currentUserId)
        .map { it ?: TasbihState(userId = currentUserId) }
        .catch { error ->
            if (isPureCancellation(error)) throw error
            Log.w(TAG, "Tasbih stream error", unwrapFirestoreError(error))
            emit(TasbihState(userId = currentUserId))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), TasbihState(userId = currentUserId))

    fun saveMember(member: OrgMember, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.saveMember(member)
            _isSaving.value = false
            result.onSuccess {
                _feedbackMessage.tryEmit(
                    if (member.id.isBlank()) "নতুন সদস্য সফলভাবে যোগ করা হয়েছে" else "সদস্যের তথ্য আপডেট করা হয়েছে"
                )
                onSuccess()
            }.onFailure { err ->
                _feedbackMessage.tryEmit(err.message ?: "সদস্য সংরক্ষণ ব্যর্থ হয়েছে")
            }
        }
    }

    fun deleteMember(memberId: String) {
        viewModelScope.launch {
            repository.deleteMember(memberId)
                .onSuccess { _feedbackMessage.tryEmit("সদস্য তালিকা থেকে সরানো হয়েছে") }
                .onFailure { _feedbackMessage.tryEmit(it.message ?: "মুছে ফেলতে ব্যর্থ হয়েছে") }
        }
    }

    fun saveDonation(record: DonationRecord, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.saveDonation(record)
            _isSaving.value = false
            result.onSuccess {
                _feedbackMessage.tryEmit("দানের হিসাব সফলভাবে সংরক্ষিত হয়েছে")
                onSuccess()
            }.onFailure { err ->
                _feedbackMessage.tryEmit(err.message ?: "হিসাব সংরক্ষণ ব্যর্থ হয়েছে")
            }
        }
    }

    fun deleteDonation(donationId: String) {
        viewModelScope.launch {
            repository.deleteDonation(donationId)
                .onSuccess { _feedbackMessage.tryEmit("হিসাবটি মুছে ফেলা হয়েছে") }
                .onFailure { _feedbackMessage.tryEmit(it.message ?: "মুছে ফেলতে ব্যর্থ হয়েছে") }
        }
    }

    fun saveActivity(notice: ActivityNotice, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.saveActivity(notice)
            _isSaving.value = false
            result.onSuccess {
                _feedbackMessage.tryEmit("কার্যক্রম ও নোটিফিকেশন প্রকাশিত হয়েছে")
                onSuccess()
            }.onFailure { err ->
                _feedbackMessage.tryEmit(err.message ?: "কার্যক্রম সংরক্ষণ ব্যর্থ হয়েছে")
            }
        }
    }

    fun markActivityRead(activityId: String, read: Boolean) {
        viewModelScope.launch {
            repository.markActivityRead(activityId, read)
                .onFailure { _feedbackMessage.tryEmit(it.message ?: "অবস্থা পরিবর্তন ব্যর্থ হয়েছে") }
        }
    }

    fun deleteActivity(activityId: String) {
        viewModelScope.launch {
            repository.deleteActivity(activityId)
                .onSuccess { _feedbackMessage.tryEmit("কার্যক্রমটি মুছে ফেলা হয়েছে") }
                .onFailure { _feedbackMessage.tryEmit(it.message ?: "মুছে ফেলতে ব্যর্থ হয়েছে") }
        }
    }

    fun toggleQuranBookmark(
        surahNumber: Int,
        ayahNumber: Int,
        surahNameBn: String,
        surahNameAr: String,
        ayahTextAr: String,
        ayahTextBn: String,
        currentlyBookmarked: Boolean
    ) {
        viewModelScope.launch {
            repository.toggleQuranBookmark(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                surahNameBn = surahNameBn,
                surahNameAr = surahNameAr,
                ayahTextAr = ayahTextAr,
                ayahTextBn = ayahTextBn,
                currentlyBookmarked = currentlyBookmarked
            ).onSuccess {
                _feedbackMessage.tryEmit(
                    if (currentlyBookmarked) "বুকমার্ক সরানো হয়েছে" else "আয়াত বুকমার্ক করা হয়েছে"
                )
            }.onFailure {
                _feedbackMessage.tryEmit(it.message ?: "বুকমার্ক পরিবর্তন ব্যর্থ হয়েছে")
            }
        }
    }

    fun toggleDuaFavorite(duaId: String, titleBn: String, category: String, currentlyFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleDuaFavorite(duaId, titleBn, category, currentlyFavorite)
                .onSuccess {
                    _feedbackMessage.tryEmit(
                        if (currentlyFavorite) "প্রিয় তালিকা থেকে সরানো হয়েছে" else "প্রিয় দোয়ায় যোগ করা হয়েছে"
                    )
                }.onFailure {
                    _feedbackMessage.tryEmit(it.message ?: "পছন্দ পরিবর্তন ব্যর্থ হয়েছে")
                }
        }
    }

    fun updateTasbih(dhikrName: String, currentCount: Long, targetCount: Long, lifetimeCount: Long) {
        viewModelScope.launch {
            repository.saveTasbihState(dhikrName, currentCount, targetCount, lifetimeCount)
        }
    }

    private fun <T> Flow<List<T>>.toUiStateFlow(fallbackMessage: String): Flow<UiState<List<T>>> =
        this.map<List<T>, UiState<List<T>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = unwrapFirestoreError(error)
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, fallbackMessage, root)
                emit(UiState.Error(root.message ?: fallbackMessage))
            }

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? =
        generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()

    private fun isPureCancellation(error: Throwable): Boolean =
        unwrapFirestoreError(error) == null && error is CancellationException
}
