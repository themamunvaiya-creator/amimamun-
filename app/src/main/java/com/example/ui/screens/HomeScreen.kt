package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ActivityNotice
import com.example.data.model.NoticePriority
import com.example.data.model.OrgMember
import com.example.data.model.UiState
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.UrgentAmber
import com.example.ui.theme.UrgentContainer
import com.example.ui.theme.arabicVerseStyle
import com.example.ui.viewmodel.FinancialSummary
import com.example.util.BengaliFormatters

@Composable
fun HomeScreen(
    userName: String,
    userEmail: String,
    membersState: UiState<List<OrgMember>>,
    financialSummary: FinancialSummary,
    activitiesState: UiState<List<ActivityNotice>>,
    unreadCount: Int,
    onNavigateMembers: () -> Unit,
    onNavigateDonations: () -> Unit,
    onNavigateActivities: () -> Unit,
    onNavigateQuran: () -> Unit,
    onNavigateDuas: () -> Unit,
    onNavigateTasbih: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showProfileDialog by remember { mutableStateOf(false) }

    val memberCount = (membersState as? UiState.Success)?.data?.size ?: 0
    val latestNotices = (activitiesState as? UiState.Success)?.data?.take(3).orEmpty()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Organization Hero Banner
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_org_banner),
                            contentDescription = "সংগঠনের ব্যানার",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            EmeraldDeep.copy(alpha = 0.28f),
                                            EmeraldDeep.copy(alpha = 0.92f)
                                        )
                                    )
                                )
                        )

                        // Profile action button top right
                        IconButton(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.35f))
                                .testTag("profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "প্রোফাইল ও লগআউট",
                                tint = Color.White
                            )
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(18.dp)
                        ) {
                            Surface(
                                color = GoldAccent.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "আস-সালামু আলাইকুম, $userName",
                                    color = Color(0xFFFFF3C4),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "পূর্ব হারবাং মুকিয়াঘোনা ইসলামিক যুব সংগঠন",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ঐক্য, মানবসেবা ও ইসলামী আদর্শে যুব সমাজ",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // 2. Live Organization Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeStatCard(
                        title = "মোট সদস্য",
                        value = BengaliFormatters.toBengaliNumber(memberCount),
                        icon = Icons.Filled.Groups,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = onNavigateMembers,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_stat_members")
                    )
                    HomeStatCard(
                        title = "বর্তমান তহবিল",
                        value = BengaliFormatters.formatBdt(financialSummary.balance),
                        icon = Icons.Filled.AccountBalanceWallet,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = onNavigateDonations,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_stat_balance")
                    )
                    HomeStatCard(
                        title = "নতুন নোটিশ",
                        value = BengaliFormatters.toBengaliNumber(unreadCount),
                        icon = Icons.Filled.NotificationsActive,
                        containerColor = if (unreadCount > 0) UrgentContainer else MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = if (unreadCount > 0) UrgentAmber else MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = onNavigateActivities,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_stat_notices")
                    )
                }
            }

            // 3. Featured Quranic Reminder Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mosque,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "আজকের কোরআনের বাণী",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "وَتَعَاوَنُوا عَلَى الْبِرِّ وَالتَّقْوَىٰ ۖ وَلَا تَعَاوَنُوا عَلَى الْإِثْمِ وَالْعُدْوَانِ",
                            style = arabicVerseStyle(22f),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "“সৎকর্ম ও তাকওয়ার কাজে তোমরা পরস্পরকে সহযোগিতা করো; আর গুনাহ ও সীমালঙ্ঘনের কাজে সহযোগিতা করো না।” — সূরা আল-মায়েদা (৫:২)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 4. Quick Access Islamic & Org Grid
            item {
                Text(
                    text = "দ্রুত সেবা ও ইসলামিক কর্নার",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionTile(
                            title = "কোরআন শরীফ",
                            subtitle = "১১৪ সূরা ও বাংলা অনুবাদ",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            onClick = onNavigateQuran,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_quran")
                        )
                        QuickActionTile(
                            title = "প্রতিদিনের দোয়া",
                            subtitle = "সকাল-সন্ধ্যা ও মাসনূন",
                            icon = Icons.Filled.VolunteerActivism,
                            onClick = onNavigateDuas,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_duas")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionTile(
                            title = "ডিজিটাল তাসবিহ",
                            subtitle = "জিকির ও যাকাত হিসাব",
                            icon = Icons.Filled.RadioButtonChecked,
                            onClick = onNavigateTasbih,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_tasbih")
                        )
                        QuickActionTile(
                            title = "জরুরি রক্তদাতা",
                            subtitle = "রক্তের গ্রুপ অনুযায়ী খুঁজুন",
                            icon = Icons.Filled.Bloodtype,
                            onClick = onNavigateMembers,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_blood")
                        )
                    }
                }
            }

            // 5. Latest Activity & Notifications Preview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সাম্প্রতিক কার্যক্রম ও নোটিশ",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onNavigateActivities) {
                        Text("সব দেখুন")
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (latestNotices.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateActivities)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "এখনও কোনো কার্যক্রম বা নোটিশ যোগ করা হয়নি",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "কার্যক্রম ট্যাবে গিয়ে সংগঠনের আসন্ন সভা বা কর্মসূচি প্রকাশ করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        latestNotices.forEach { notice ->
                            val isUrgent = NoticePriority.fromRaw(notice.priority) == NoticePriority.URGENT
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onNavigateActivities)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isUrgent) UrgentContainer else MaterialTheme.colorScheme.primaryContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.NotificationsActive,
                                            contentDescription = null,
                                            tint = if (isUrgent) UrgentAmber else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notice.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${notice.eventDateLabel} • ${notice.venue}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            icon = {
                Icon(Icons.Filled.Person, contentDescription = null)
            },
            title = { Text("ব্যবহারকারীর প্রোফাইল") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (userEmail.isNotBlank()) {
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "সংগঠন: পূর্ব হারবাং মুকিয়াঘোনা ইসলামিক যুব সংগঠন",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showProfileDialog = false
                        onSignOut()
                    },
                    modifier = Modifier.testTag("sign_out_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("সাইন আউট", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}

@Composable
private fun HomeStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
