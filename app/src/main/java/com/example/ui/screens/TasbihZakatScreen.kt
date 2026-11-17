package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.TasbihState
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IncomeContainer
import com.example.ui.theme.IncomeGreen
import com.example.util.BengaliFormatters
import kotlin.math.roundToLong

private data class DhikrOption(val nameBn: String, val arabic: String)

private val DHIKR_OPTIONS = listOf(
    DhikrOption("সুবহানাল্লাহ", "سُبْحَانَ اللَّهِ"),
    DhikrOption("আলহামদুলিল্লাহ", "الْحَمْدُ لِلَّهِ"),
    DhikrOption("আল্লাহু আকবার", "اللَّهُ أَكْبَرُ"),
    DhikrOption("লা ইলাহা ইল্লাল্লাহ", "لَا إِلَٰهَ إِلَّا اللَّهُ"),
    DhikrOption("আস্তাগফিরুল্লাহ", "أَسْتَغْفِرُ اللَّهَ"),
    DhikrOption("দরূদ শরীফ", "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ")
)

private val TARGET_OPTIONS = listOf(33L, 99L, 100L, 1000L)

@Composable
fun TasbihZakatScreen(
    tasbihState: TasbihState,
    onUpdateTasbih: (String, Long, Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    var dhikrName by remember(tasbihState.dhikrName) { mutableStateOf(tasbihState.dhikrName) }
    var currentCount by remember(tasbihState.currentCount) { mutableLongStateOf(tasbihState.currentCount) }
    var targetCount by remember(tasbihState.targetCount) { mutableLongStateOf(tasbihState.targetCount) }
    var lifetimeCount by remember(tasbihState.lifetimeCount) { mutableLongStateOf(tasbihState.lifetimeCount) }

    LaunchedEffect(tasbihState) {
        dhikrName = tasbihState.dhikrName
        currentCount = tasbihState.currentCount
        targetCount = tasbihState.targetCount
        lifetimeCount = tasbihState.lifetimeCount
    }

    // Zakat inputs
    var cashText by remember { mutableStateOf("") }
    var goldText by remember { mutableStateOf("") }
    var businessText by remember { mutableStateOf("") }
    var debtText by remember { mutableStateOf("") }
    var nisabText by remember { mutableStateOf("85000") }

    val totalAssets = (cashText.toLongOrNull() ?: 0L) +
        (goldText.toLongOrNull() ?: 0L) +
        (businessText.toLongOrNull() ?: 0L)
    val debt = debtText.toLongOrNull() ?: 0L
    val netZakatable = (totalAssets - debt).coerceAtLeast(0L)
    val nisab = nisabText.toLongOrNull() ?: 85000L
    val zakatDue = if (netZakatable >= nisab) (netZakatable * 0.025).roundToLong() else 0L

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Digital Tasbih Section
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ডিজিটাল তাসবিহ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DHIKR_OPTIONS.forEach { opt ->
                                FilterChip(
                                    selected = dhikrName == opt.nameBn,
                                    onClick = {
                                        dhikrName = opt.nameBn
                                        currentCount = 0L
                                        onUpdateTasbih(dhikrName, 0L, targetCount, lifetimeCount)
                                    },
                                    label = { Text(opt.nameBn) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TARGET_OPTIONS.forEach { t ->
                                FilterChip(
                                    selected = targetCount == t,
                                    onClick = {
                                        targetCount = t
                                        onUpdateTasbih(dhikrName, currentCount, targetCount, lifetimeCount)
                                    },
                                    label = { Text("লক্ষ্য ${BengaliFormatters.toBengaliNumber(t)}") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        val activeDhikr = DHIKR_OPTIONS.firstOrNull { it.nameBn == dhikrName }
                        if (activeDhikr != null) {
                            Text(
                                text = activeDhikr.arabic,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Circular Tap Counter
                        val progress = (currentCount.toFloat() / targetCount.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(190.dp)
                                .clip(CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val nextCount = if (currentCount + 1L > targetCount) 1L else currentCount + 1L
                                    val nextLifetime = lifetimeCount + 1L
                                    currentCount = nextCount
                                    lifetimeCount = nextLifetime
                                    onUpdateTasbih(dhikrName, nextCount, targetCount, nextLifetime)
                                }
                                .testTag("tasbih_tap_button")
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = trackColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = EmeraldDeep,
                                    startAngle = -90f,
                                    sweepAngle = 360f * progress,
                                    useCenter = false,
                                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = BengaliFormatters.toBengaliNumber(currentCount),
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "/ ${BengaliFormatters.toBengaliNumber(targetCount)} • ট্যাপ করুন",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "মোট জিকির: ${BengaliFormatters.toBengaliNumber(lifetimeCount)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = GoldAccent
                            )
                            OutlinedButton(
                                onClick = {
                                    currentCount = 0L
                                    onUpdateTasbih(dhikrName, 0L, targetCount, lifetimeCount)
                                }
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("রিসেট")
                            }
                        }
                    }
                }
            }

            // 2. Zakat Calculator Section
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "যাকাত ক্যালকুলেটর (২.৫%)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "বছরের নিসাব পরিমাণ সম্পদ পূর্ণ হলে ২.৫% হারে যাকাত ফরয হয়।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = cashText,
                            onValueChange = { cashText = it.filter { c -> c.isDigit() } },
                            label = { Text("নগদ টাকা ও ব্যাংক জমা (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = goldText,
                            onValueChange = { goldText = it.filter { c -> c.isDigit() } },
                            label = { Text("স্বর্ণ ও রূপার বাজার মূল্য (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = businessText,
                            onValueChange = { businessText = it.filter { c -> c.isDigit() } },
                            label = { Text("ব্যবসায়িক পণ্য ও বিনিয়োগ (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = debtText,
                                onValueChange = { debtText = it.filter { c -> c.isDigit() } },
                                label = { Text("বাদযোগ্য ঋণ (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = nisabText,
                                onValueChange = { nisabText = it.filter { c -> c.isDigit() } },
                                label = { Text("নিসাব সীমা (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = IncomeContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "নিট যাকাতযোগ্য সম্পদ: ${BengaliFormatters.formatBdt(netZakatable)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IncomeGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (netZakatable >= nisab)
                                        "প্রদেয় যাকাত: ${BengaliFormatters.formatBdt(zakatDue)}"
                                    else
                                        "সম্পদ নিসাব সীমার নিচে (যাকাত ফরয নয়)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
