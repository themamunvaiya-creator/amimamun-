package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DailyDuaCatalog
import com.example.data.model.DailyDua
import com.example.data.model.DuaFavorite
import com.example.data.model.UiState
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.arabicVerseStyle
import com.example.ui.viewmodel.DuaViewModel

@Composable
fun DuaScreen(
    duaViewModel: DuaViewModel,
    favoritesState: UiState<List<DuaFavorite>>,
    onToggleFavorite: (String, String, String, Boolean) -> Unit,
    onShowFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedCategory by duaViewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by duaViewModel.searchQuery.collectAsStateWithLifecycle()
    val onlyFavorites by duaViewModel.onlyFavorites.collectAsStateWithLifecycle()
    val arabicFontSize by duaViewModel.arabicFontSize.collectAsStateWithLifecycle()

    val favoriteIds = ((favoritesState as? UiState.Success)?.data.orEmpty())
        .map { it.duaId }
        .toSet()

    val filteredDuas = duaViewModel.filterDuas(favoriteIds)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "প্রতিদিনের মাসনূন দোয়া",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { duaViewModel.adjustArabicFontSize(-2f) }) {
                        Icon(Icons.Filled.TextDecrease, contentDescription = "আরবি লেখা ছোট করুন")
                    }
                    IconButton(onClick = { duaViewModel.adjustArabicFontSize(2f) }) {
                        Icon(Icons.Filled.TextIncrease, contentDescription = "আরবি লেখা বড় করুন")
                    }
                    FilterChip(
                        selected = onlyFavorites,
                        onClick = { duaViewModel.toggleOnlyFavorites() },
                        label = { Text("প্রিয়") },
                        leadingIcon = {
                            Icon(
                                if (onlyFavorites) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { duaViewModel.setSearchQuery(it) },
                placeholder = { Text("দোয়া খুঁজুন (যেমন: ঘুম, মসজিদ, ভ্রমণ, বিপদ)...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dua_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DailyDuaCatalog.categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { duaViewModel.selectCategory(cat) },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredDuas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "এই বিভাগে কোনো দোয়া পাওয়া যায়নি",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDuas, key = { it.id }) { dua ->
                        val isFav = favoriteIds.contains(dua.id)
                        DuaCard(
                            dua = dua,
                            arabicFontSize = arabicFontSize,
                            isFavorite = isFav,
                            onToggleFavorite = {
                                onToggleFavorite(dua.id, dua.titleBn, dua.category, isFav)
                            },
                            onCopy = {
                                val text = formatDuaText(dua)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText(dua.titleBn, text))
                                onShowFeedback("দোয়াটি কপি করা হয়েছে")
                            },
                            onShare = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, formatDuaText(dua))
                                }
                                context.startActivity(Intent.createChooser(intent, "দোয়া শেয়ার করুন"))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DuaCard(
    dua: DailyDua,
    arabicFontSize: Float,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dua_card_${dua.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = dua.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "কপি করুন", modifier = Modifier.size(19.dp))
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Filled.Share, contentDescription = "শেয়ার করুন", modifier = Modifier.size(19.dp))
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "প্রিয় করুন",
                            tint = if (isFavorite) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dua.titleBn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dua.arabic,
                    style = arabicVerseStyle(arabicFontSize),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "উচ্চারণ: ${dua.pronunciationBn}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "অর্থ: ${dua.meaningBn}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (dua.virtueBn.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ফজিলত: ${dua.virtueBn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "সূত্র: ${dua.referenceBn}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatDuaText(dua: DailyDua): String = buildString {
    appendLine(dua.titleBn)
    appendLine()
    appendLine(dua.arabic)
    appendLine()
    appendLine("উচ্চারণ: ${dua.pronunciationBn}")
    appendLine("অর্থ: ${dua.meaningBn}")
    if (dua.virtueBn.isNotBlank()) {
        appendLine("ফজিলত: ${dua.virtueBn}")
    }
    appendLine("সূত্র: ${dua.referenceBn}")
}
