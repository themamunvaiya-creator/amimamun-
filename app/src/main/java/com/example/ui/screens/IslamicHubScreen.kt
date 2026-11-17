package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DuaFavorite
import com.example.data.model.QuranBookmark
import com.example.data.model.TasbihState
import com.example.data.model.UiState
import com.example.ui.viewmodel.DuaViewModel
import com.example.ui.viewmodel.QuranViewModel

enum class IslamicHubTab(val labelBn: String) {
    QURAN("কোরআন শরীফ"),
    DUAS("প্রতিদিনের দোয়া"),
    TASBIH_ZAKAT("তাসবিহ ও যাকাত")
}

@Composable
fun IslamicHubScreen(
    selectedTab: IslamicHubTab,
    onSelectTab: (IslamicHubTab) -> Unit,
    quranViewModel: QuranViewModel,
    duaViewModel: DuaViewModel,
    bookmarksState: UiState<List<QuranBookmark>>,
    favoritesState: UiState<List<DuaFavorite>>,
    tasbihState: TasbihState,
    onToggleQuranBookmark: (Int, Int, String, String, String, String, Boolean) -> Unit,
    onToggleDuaFavorite: (String, String, String, Boolean) -> Unit,
    onUpdateTasbih: (String, Long, Long, Long) -> Unit,
    onShowFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            IslamicHubTab.entries.forEachIndexed { index, tab ->
                SegmentedButton(
                    selected = selectedTab == tab,
                    onClick = { onSelectTab(tab) },
                    shape = SegmentedButtonDefaults.itemShape(index, IslamicHubTab.entries.size),
                    modifier = Modifier.testTag("islamic_tab_${tab.name.lowercase()}")
                ) {
                    Text(
                        text = tab.labelBn,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        when (selectedTab) {
            IslamicHubTab.QURAN -> QuranScreen(
                quranViewModel = quranViewModel,
                bookmarksState = bookmarksState,
                onToggleBookmark = onToggleQuranBookmark,
                modifier = Modifier.weight(1f)
            )
            IslamicHubTab.DUAS -> DuaScreen(
                duaViewModel = duaViewModel,
                favoritesState = favoritesState,
                onToggleFavorite = onToggleDuaFavorite,
                onShowFeedback = onShowFeedback,
                modifier = Modifier.weight(1f)
            )
            IslamicHubTab.TASBIH_ZAKAT -> TasbihZakatScreen(
                tasbihState = tasbihState,
                onUpdateTasbih = onUpdateTasbih,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
