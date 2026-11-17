package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.local.DailyDuaCatalog
import com.example.data.model.DailyDua
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DuaViewModel : ViewModel() {
    private val _selectedCategory = MutableStateFlow("সবগুলো")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    private val _arabicFontSize = MutableStateFlow(23f)
    val arabicFontSize: StateFlow<Float> = _arabicFontSize.asStateFlow()

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleOnlyFavorites() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    fun adjustArabicFontSize(delta: Float) {
        _arabicFontSize.value = (_arabicFontSize.value + delta).coerceIn(18f, 36f)
    }

    fun filterDuas(favoriteIds: Set<String>): List<DailyDua> {
        val q = _searchQuery.value.trim().lowercase()
        val cat = _selectedCategory.value
        val favOnly = _onlyFavorites.value
        return DailyDuaCatalog.allDuas.filter { dua ->
            val catMatch = cat == "সবগুলো" || dua.category == cat
            val favMatch = !favOnly || favoriteIds.contains(dua.id)
            val queryMatch = q.isEmpty() ||
                dua.titleBn.lowercase().contains(q) ||
                dua.meaningBn.lowercase().contains(q) ||
                dua.pronunciationBn.lowercase().contains(q) ||
                dua.category.lowercase().contains(q)
            catMatch && favMatch && queryMatch
        }
    }
}
