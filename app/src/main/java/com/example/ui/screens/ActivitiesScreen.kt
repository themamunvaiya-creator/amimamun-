package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ActivityNotice
import com.example.data.model.NoticePriority
import com.example.data.model.UiState
import com.example.ui.theme.UrgentAmber
import com.example.ui.theme.UrgentContainer
import com.example.util.OrgNotificationHelper

private val ACTIVITY_CATEGORIES = listOf(
    "সভা",
    "দাওয়াহ ও ওয়াজ",
    "সমাজসেবা ও ত্রাণ",
    "রক্তদান কর্মসূচি",
    "ইফতার ও রমজান",
    "জরুরি ঘোষণা",
    "পরিচ্ছন্নতা অভিযান"
)

@Composable
fun ActivitiesScreen(
    activitiesState: UiState<List<ActivityNotice>>,
    isSaving: Boolean,
    onSaveActivity: (ActivityNotice, () -> Unit) -> Unit,
    onMarkRead: (String, Boolean) -> Unit,
    onDeleteActivity: (String) -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(OrgNotificationHelper.hasNotificationPermission(context))
    }
    var pendingNotificationNotice by remember { mutableStateOf<ActivityNotice?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
        if (granted) {
            pendingNotificationNotice?.let { notice ->
                OrgNotificationHelper.showActivityNotification(
                    context = context,
                    title = notice.title,
                    body = "${notice.eventDateLabel} (${notice.eventTimeLabel}) • ${notice.venue}\n${notice.description}"
                )
                onShowToast("ফোনের নোটিফিকেশনে বিজ্ঞপ্তি পাঠানো হয়েছে")
            }
            pendingNotificationNotice = null
        } else {
            onShowToast("নোটিফিকেশন অনুমতি দেওয়া হয়নি")
            pendingNotificationNotice = null
        }
    }

    var filterMode by remember { mutableStateOf("ALL") }
    var editingNotice by remember { mutableStateOf<ActivityNotice?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var noticeToDelete by remember { mutableStateOf<ActivityNotice?>(null) }

    fun triggerLocalNotification(notice: ActivityNotice) {
        if (OrgNotificationHelper.hasNotificationPermission(context)) {
            OrgNotificationHelper.showActivityNotification(
                context = context,
                title = notice.title,
                body = "${notice.eventDateLabel} (${notice.eventTimeLabel}) • ${notice.venue}\n${notice.description}"
            )
            onShowToast("ফোনের নোটিফিকেশন পাঠানো হয়েছে")
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingNotificationNotice = notice
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingNotice = null
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_activity_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "নতুন কার্যক্রম বা নোটিশ প্রকাশ করুন")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "কার্যক্রম ও নোটিফিকেশন",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = UrgentContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Filled.NotificationsActive,
                                    contentDescription = null,
                                    tint = UrgentAmber
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "নোটিফিকেশন চালু করুন",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = UrgentAmber
                                    )
                                    Text(
                                        text = "সংগঠনের সভার রিমাইন্ডার ও জরুরি ঘোষণা পেতে অনুমতি দিন।",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                ) {
                                    Text("চালু করুন")
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterMode == "ALL",
                            onClick = { filterMode = "ALL" },
                            label = { Text("সব কার্যক্রম") }
                        )
                        FilterChip(
                            selected = filterMode == "UNREAD",
                            onClick = { filterMode = "UNREAD" },
                            label = { Text("অপঠিত") }
                        )
                        FilterChip(
                            selected = filterMode == "URGENT",
                            onClick = { filterMode = "URGENT" },
                            label = { Text("জরুরি ঘোষণা") }
                        )
                    }
                }

                when (activitiesState) {
                    is UiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    is UiState.Error -> {
                        item {
                            Text(
                                text = activitiesState.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    is UiState.Success -> {
                        val filtered = activitiesState.data.filter { notice ->
                            when (filterMode) {
                                "UNREAD" -> !notice.readStatus
                                "URGENT" -> NoticePriority.fromRaw(notice.priority) == NoticePriority.URGENT
                                else -> true
                            }
                        }

                        if (filtered.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.NotificationsNone,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "কোনো কার্যক্রম বা নোটিশ নেই",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "নিচের '+' বাটনে ট্যাপ করে সংগঠনের আসন্ন সভা, ওয়াজ মাহফিল বা ত্রাণ কর্মসূচি ঘোষণা করুন।",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filtered, key = { it.id }) { notice ->
                                ActivityNoticeCard(
                                    notice = notice,
                                    onSendNotification = { triggerLocalNotification(notice) },
                                    onToggleRead = { onMarkRead(notice.id, !notice.readStatus) },
                                    onEdit = {
                                        editingNotice = notice
                                        showFormDialog = true
                                    },
                                    onDelete = { noticeToDelete = notice }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        ActivityFormDialog(
            initial = editingNotice,
            isSaving = isSaving,
            onDismiss = { showFormDialog = false },
            onSubmit = { notice, sendPushNow ->
                onSaveActivity(notice) {
                    showFormDialog = false
                    if (sendPushNow) {
                        triggerLocalNotification(notice)
                    }
                }
            }
        )
    }

    noticeToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { noticeToDelete = null },
            title = { Text("কার্যক্রম মুছে ফেলুন") },
            text = { Text("আপনি কি '${target.title}' মুছে ফেলতে চান?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteActivity(target.id)
                        noticeToDelete = null
                    }
                ) {
                    Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { noticeToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun ActivityNoticeCard(
    notice: ActivityNotice,
    onSendNotification: () -> Unit,
    onToggleRead: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isUrgent = NoticePriority.fromRaw(notice.priority) == NoticePriority.URGENT
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (notice.readStatus) 1.dp else 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_card_${notice.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isUrgent) UrgentContainer else MaterialTheme.colorScheme.primaryContainer),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = if (isUrgent) UrgentContainer else MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (isUrgent) "জরুরি" else notice.category,
                                color = if (isUrgent) UrgentAmber else MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        if (!notice.readStatus) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "নতুন",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notice.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notice.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${notice.eventDateLabel} • ${notice.eventTimeLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = notice.venue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onSendNotification) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("অ্যালার্ট পাঠান")
                }

                Row {
                    IconButton(onClick = onToggleRead) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = if (notice.readStatus) "অপঠিত করুন" else "পঠিত হিসেবে চিহ্নিত করুন",
                            tint = if (notice.readStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "সম্পাদনা")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = "মুছে ফেলুন",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityFormDialog(
    initial: ActivityNotice?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (ActivityNotice, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: ACTIVITY_CATEGORIES.first()) }
    var eventDateLabel by remember { mutableStateOf(initial?.eventDateLabel ?: "২০ নভেম্বর ২০২৬") }
    var eventTimeLabel by remember { mutableStateOf(initial?.eventTimeLabel ?: "বাদ আসর") }
    var venue by remember { mutableStateOf(initial?.venue ?: "মুকিয়াঘোনা জামে মসজিদ") }
    var priority by remember { mutableStateOf(NoticePriority.fromRaw(initial?.priority ?: NoticePriority.NORMAL.name)) }
    var sendPushNow by remember { mutableStateOf(initial == null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var catExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "নতুন কার্যক্রম / নোটিশ" else "কার্যক্রম সম্পাদনা") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    NoticePriority.entries.forEachIndexed { idx, pri ->
                        SegmentedButton(
                            selected = priority == pri,
                            onClick = { priority = pri },
                            shape = SegmentedButtonDefaults.itemShape(idx, NoticePriority.entries.size)
                        ) {
                            Text(pri.labelBn)
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("শিরোনাম *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activity_title_input")
                )

                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("বিভাগ *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        ACTIVITY_CATEGORIES.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    category = item
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = eventDateLabel,
                        onValueChange = { eventDateLabel = it },
                        label = { Text("তারিখ *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = eventTimeLabel,
                        onValueChange = { eventTimeLabel = it },
                        label = { Text("সময় *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("স্থান *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("বিস্তারিত বিবরণ *") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activity_desc_input")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = sendPushNow,
                        onCheckedChange = { sendPushNow = it }
                    )
                    Text(
                        text = "প্রকাশের সাথে সাথে ফোনে নোটিফিকেশন পাঠান",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        title.isBlank() -> validationError = "শিরোনাম লিখুন।"
                        description.isBlank() -> validationError = "বিস্তারিত বিবরণ লিখুন।"
                        venue.isBlank() -> validationError = "স্থান লিখুন।"
                        else -> {
                            validationError = null
                            onSubmit(
                                ActivityNotice(
                                    id = initial?.id ?: "",
                                    userId = initial?.userId ?: "",
                                    title = title.trim(),
                                    description = description.trim(),
                                    category = category.trim().ifBlank { "সভা" },
                                    eventDateLabel = eventDateLabel.trim().ifBlank { "শীঘ্রই" },
                                    eventTimeLabel = eventTimeLabel.trim().ifBlank { "বাদ আসর" },
                                    venue = venue.trim(),
                                    priority = priority.name,
                                    readStatus = initial?.readStatus ?: false,
                                    createdAt = initial?.createdAt
                                ),
                                sendPushNow
                            )
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.testTag("save_activity_button")
            ) {
                Text(if (isSaving) "প্রকাশ হচ্ছে..." else "প্রকাশ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
