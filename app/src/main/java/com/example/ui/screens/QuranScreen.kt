package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.SurahMetadata
import com.example.data.model.AyahItem
import com.example.data.model.QuranBookmark
import com.example.data.model.SurahInfo
import com.example.data.model.UiState
import com.example.ui.theme.ArabicQuranFontFamily
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.arabicVerseStyle
import com.example.ui.viewmodel.QuranViewModel
import com.example.util.BengaliFormatters

@Composable
fun QuranScreen(
    quranViewModel: QuranViewModel,
    bookmarksState: UiState<List<QuranBookmark>>,
    onToggleBookmark: (Int, Int, String, String, String, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedSurah by quranViewModel.selectedSurah.collectAsStateWithLifecycle()
    val searchQuery by quranViewModel.searchQuery.collectAsStateWithLifecycle()
    val revelationFilter by quranViewModel.revelationFilter.collectAsStateWithLifecycle()
    val cachedSurahNumbers by quranViewModel.cachedSurahNumbers.collectAsStateWithLifecycle()
    val ayahsState by quranViewModel.ayahsState.collectAsStateWithLifecycle()
    val arabicFontSize by quranViewModel.arabicFontSize.collectAsStateWithLifecycle()

    val bookmarks = (bookmarksState as? UiState.Success)?.data.orEmpty()
    val bookmarkedKeys = bookmarks.map { "${it.surahNumber}_${it.ayahNumber}" }.toSet()

    val currentSurah = selectedSurah
    if (currentSurah != null) {
        BackHandler { quranViewModel.closeSurah() }
        SurahReaderView(
            surah = currentSurah,
            ayahsState = ayahsState,
            arabicFontSize = arabicFontSize,
            bookmarkedKeys = bookmarkedKeys,
            onBack = { quranViewModel.closeSurah() },
            onRetry = { quranViewModel.retryLoadSurah() },
            onFontSizeDelta = { quranViewModel.adjustArabicFontSize(it) },
            onToggleBookmark = { ayah ->
                val isMarked = bookmarkedKeys.contains("${currentSurah.number}_${ayah.numberInSurah}")
                onToggleBookmark(
                    currentSurah.number,
                    ayah.numberInSurah,
                    currentSurah.nameBn,
                    currentSurah.nameAr,
                    ayah.textAr,
                    ayah.textBn,
                    isMarked
                )
            },
            modifier = modifier
        )
    } else {
        SurahListView(
            searchQuery = searchQuery,
            revelationFilter = revelationFilter,
            cachedSurahNumbers = cachedSurahNumbers,
            bookmarks = bookmarks,
            onSearchChange = { quranViewModel.setSearchQuery(it) },
            onFilterChange = { quranViewModel.setRevelationFilter(it) },
            onOpenSurah = { quranViewModel.openSurah(it) },
            onOpenBookmark = { bm -> quranViewModel.openSurahByNumber(bm.surahNumber.toInt()) },
            modifier = modifier
        )
    }
}

@Composable
private fun SurahListView(
    searchQuery: String,
    revelationFilter: String,
    cachedSurahNumbers: Set<Int>,
    bookmarks: List<QuranBookmark>,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onOpenSurah: (SurahInfo) -> Unit,
    onOpenBookmark: (QuranBookmark) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredSurahs = SurahMetadata.allSurahs.filter { surah ->
        val revMatch = revelationFilter == "সব" || surah.revelationTypeBn == revelationFilter
        val q = searchQuery.trim().lowercase()
        val qMatch = q.isEmpty() ||
            surah.nameBn.lowercase().contains(q) ||
            surah.meaningBn.lowercase().contains(q) ||
            surah.number.toString() == q ||
            BengaliFormatters.toBengaliNumber(surah.number) == q
        revMatch && qMatch
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_quran_header),
                            contentDescription = "পবিত্র আল-কোরআন",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            EmeraldDeep.copy(alpha = 0.92f),
                                            EmeraldDeep.copy(alpha = 0.45f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "القرآن الكريم",
                                fontFamily = ArabicQuranFontFamily,
                                color = Color(0xFFFFF3C4),
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "পবিত্র কোরআন শরীফ",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "১১৪টি সূরা • আরবি ও বাংলা অনুবাদ",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            if (bookmarks.isNotEmpty()) {
                item {
                    Text(
                        text = "সংরক্ষিত বুকমার্ক",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bookmarks.forEach { bm ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.clickable { onOpenBookmark(bm) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Bookmark,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${bm.surahNameBn} (${BengaliFormatters.toBengaliNumber(bm.ayahNumber)})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("সূরার নাম, অর্থ বা নম্বর লিখুন...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("surah_search_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("সব", "মক্কী", "মাদানী").forEach { type ->
                        FilterChip(
                            selected = revelationFilter == type,
                            onClick = { onFilterChange(type) },
                            label = { Text(type) }
                        )
                    }
                }
            }

            items(filteredSurahs, key = { it.number }) { surah ->
                val isCached = cachedSurahNumbers.contains(surah.number)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSurah(surah) }
                        .testTag("surah_item_${surah.number}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = BengaliFormatters.toBengaliNumber(surah.number),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = surah.nameBn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isCached) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "অফলাইন সংরক্ষিত",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${surah.meaningBn} • ${surah.revelationTypeBn} • ${BengaliFormatters.toBengaliNumber(surah.ayahCount)} আয়াত",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = surah.nameAr,
                            fontFamily = ArabicQuranFontFamily,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahReaderView(
    surah: SurahInfo,
    ayahsState: UiState<List<AyahItem>>,
    arabicFontSize: Float,
    bookmarkedKeys: Set<String>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onFontSizeDelta: (Float) -> Unit,
    onToggleBookmark: (AyahItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 760.dp)
    ) {
        // Top Reader Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("quran_reader_back")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "সূরা তালিকায় ফিরুন")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${BengaliFormatters.toBengaliNumber(surah.number)}. ${surah.nameBn}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${surah.meaningBn} • ${BengaliFormatters.toBengaliNumber(surah.ayahCount)} আয়াত",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onFontSizeDelta(-2f) }) {
                    Icon(Icons.Filled.TextDecrease, contentDescription = "আরবি ফন্ট ছোট করুন")
                }
                IconButton(onClick = { onFontSizeDelta(2f) }) {
                    Icon(Icons.Filled.TextIncrease, contentDescription = "আরবি ফন্ট বড় করুন")
                }
            }
        }

        when (ayahsState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "সূরা ${surah.nameBn} লোড হচ্ছে...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = ayahsState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = onRetry) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("আবার চেষ্টা করুন")
                        }
                    }
                }
            }

            is UiState.Success -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (surah.number != 1 && surah.number != 9) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = EmeraldDeep),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontFamily = ArabicQuranFontFamily,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = GoldAccent,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp)
                                )
                            }
                        }
                    }

                    items(ayahsState.data, key = { it.numberInSurah }) { ayah ->
                        val isBookmarked = bookmarkedKeys.contains("${surah.number}_${ayah.numberInSurah}")
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
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
                                            text = "আয়াত ${BengaliFormatters.toBengaliNumber(ayah.numberInSurah)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                val shareText = "${surah.nameBn} (${surah.number}:${ayah.numberInSurah})\n\n${ayah.textAr}\n\n${ayah.textBn}"
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                }
                                                context.startActivity(Intent.createChooser(intent, "আয়াত শেয়ার করুন"))
                                            }
                                        ) {
                                            Icon(Icons.Filled.Share, contentDescription = "শেয়ার করুন")
                                        }
                                        IconButton(onClick = { onToggleBookmark(ayah) }) {
                                            Icon(
                                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                                contentDescription = "বুকমার্ক",
                                                tint = if (isBookmarked) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = ayah.textAr,
                                    style = arabicVerseStyle(arabicFontSize),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = ayah.textBn,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
