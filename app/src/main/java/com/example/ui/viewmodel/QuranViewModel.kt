package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SurahMetadata
import com.example.data.model.AyahItem
import com.example.data.model.SurahInfo
import com.example.data.model.UiState
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuranViewModel(
    private val repository: QuranRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _revelationFilter = MutableStateFlow("সব")
    val revelationFilter: StateFlow<String> = _revelationFilter.asStateFlow()

    private val _cachedSurahNumbers = MutableStateFlow<Set<Int>>(emptySet())
    val cachedSurahNumbers: StateFlow<Set<Int>> = _cachedSurahNumbers.asStateFlow()

    private val _selectedSurah = MutableStateFlow<SurahInfo?>(null)
    val selectedSurah: StateFlow<SurahInfo?> = _selectedSurah.asStateFlow()

    private val _ayahsState = MutableStateFlow<UiState<List<AyahItem>>>(UiState.Loading)
    val ayahsState: StateFlow<UiState<List<AyahItem>>> = _ayahsState.asStateFlow()

    private val _arabicFontSize = MutableStateFlow(26f)
    val arabicFontSize: StateFlow<Float> = _arabicFontSize.asStateFlow()

    init {
        refreshCachedSurahs()
    }

    fun refreshCachedSurahs() {
        viewModelScope.launch {
            _cachedSurahNumbers.value = repository.getCachedSurahNumbers()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setRevelationFilter(filter: String) {
        _revelationFilter.value = filter
    }

    fun adjustArabicFontSize(delta: Float) {
        _arabicFontSize.value = (_arabicFontSize.value + delta).coerceIn(18f, 40f)
    }

    fun openSurah(surah: SurahInfo) {
        _selectedSurah.value = surah
        loadSurahAyahs(surah.number)
    }

    fun openSurahByNumber(number: Int) {
        SurahMetadata.findByNumber(number)?.let { openSurah(it) }
    }

    fun closeSurah() {
        _selectedSurah.value = null
    }

    fun retryLoadSurah() {
        _selectedSurah.value?.let { loadSurahAyahs(it.number) }
    }

    private fun loadSurahAyahs(surahNumber: Int) {
        _ayahsState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.getSurahAyahs(surahNumber)
            result.onSuccess { ayahs ->
                _ayahsState.value = UiState.Success(ayahs)
                _cachedSurahNumbers.value = repository.getCachedSurahNumbers()
            }.onFailure { err ->
                _ayahsState.value = UiState.Error(
                    "সূরাটি লোড করা যায়নি। ইন্টারনেট সংযোগ পরীক্ষা করে আবার চেষ্টা করুন। (${err.localizedMessage ?: "Network Error"})"
                )
            }
        }
    }
}
