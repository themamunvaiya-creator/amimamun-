package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.OrgMember
import com.example.data.model.UiState
import com.example.ui.theme.ExpenseContainer
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeContainer
import com.example.ui.theme.IncomeGreen
import com.example.util.BengaliFormatters

private val BLOOD_GROUPS = listOf("সব", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
private val DESIGNATIONS = listOf(
    "সভাপতি",
    "সহ-সভাপতি",
    "সাধারণ সম্পাদক",
    "সহ-সাধারণ সম্পাদক",
    "সাংগঠনিক সম্পাদক",
    "কোষাধ্যক্ষ",
    "দপ্তর সম্পাদক",
    "ধর্ম ও দাওয়াহ সম্পাদক",
    "সমাজকল্যাণ সম্পাদক",
    "কার্যকরী সদস্য",
    "সাধারণ সদস্য",
    "উপদেষ্টা"
)

@Composable
fun MembersScreen(
    membersState: UiState<List<OrgMember>>,
    isSaving: Boolean,
    onSaveMember: (OrgMember, () -> Unit) -> Unit,
    onDeleteMember: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("সব") }
    var editingMember by remember { mutableStateOf<OrgMember?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<OrgMember?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingMember = null
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_member_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "নতুন সদস্য যোগ করুন")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "সদস্যদের তালিকা ও রক্তদাতা ডিরেক্টরি",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("নাম, পদবী, পাড়া বা ফোন দিয়ে খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BLOOD_GROUPS.forEach { bg ->
                        FilterChip(
                            selected = selectedBloodGroup == bg,
                            onClick = { selectedBloodGroup = bg },
                            label = {
                                Text(if (bg == "সব") "সকল গ্রুপ" else bg)
                            },
                            leadingIcon = if (bg != "সব") {
                                {
                                    Icon(
                                        Icons.Filled.Bloodtype,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (membersState) {
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is UiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = membersState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    is UiState.Success -> {
                        val filtered = remember(membersState.data, searchQuery, selectedBloodGroup) {
                            val q = searchQuery.trim().lowercase()
                            membersState.data.filter { member ->
                                val bgMatch = selectedBloodGroup == "সব" || member.bloodGroup == selectedBloodGroup
                                val qMatch = q.isEmpty() ||
                                    member.fullName.lowercase().contains(q) ||
                                    member.designation.lowercase().contains(q) ||
                                    member.ward.lowercase().contains(q) ||
                                    member.phone.contains(q)
                                bgMatch && qMatch
                            }
                        }

                        if (filtered.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Groups,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (membersState.data.isEmpty())
                                            "এখনও কোনো সদস্য যুক্ত করা হয়নি"
                                        else
                                            "কোনো সদস্য খুঁজে পাওয়া যায়নি",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "নিচের '+' বাটনে ট্যাপ করে সংগঠনের সদস্যদের নাম, পদবী, মোবাইল ও রক্তের গ্রুপ সংরক্ষণ করুন।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filtered, key = { it.id }) { member ->
                                    MemberCard(
                                        member = member,
                                        onCall = {
                                            val dialIntent = Intent(
                                                Intent.ACTION_DIAL,
                                                Uri.parse("tel:${member.phone}")
                                            )
                                            context.startActivity(dialIntent)
                                        },
                                        onEdit = {
                                            editingMember = member
                                            showFormDialog = true
                                        },
                                        onDelete = { memberToDelete = member }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        MemberFormDialog(
            initial = editingMember,
            isSaving = isSaving,
            onDismiss = { showFormDialog = false },
            onSubmit = { member ->
                onSaveMember(member) {
                    showFormDialog = false
                }
            }
        )
    }

    memberToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("সদস্য অপসারণ") },
            text = { Text("আপনি কি নিশ্চিতভাবে '${target.fullName}'-কে তালিকা থেকে মুছে ফেলতে চান?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteMember(target.id)
                        memberToDelete = null
                    }
                ) {
                    Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun MemberCard(
    member: OrgMember,
    onCall: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("member_card_${member.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.fullName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = member.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = if (member.activeStatus) IncomeContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (member.activeStatus) "সক্রিয়" else "বিরতি",
                                color = if (member.activeStatus) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = member.designation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    color = ExpenseContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Bloodtype,
                            contentDescription = "রক্তের গ্রুপ",
                            tint = ExpenseRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = member.bloodGroup,
                            color = ExpenseRed,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${member.ward} • মাসিক চাঁদা: ${BengaliFormatters.formatBdt(member.monthlyFee)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "মোবাইল: ${BengaliFormatters.toBengaliDigits(member.phone)} • যোগদান: ${member.joinedDateLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (member.notes.isNotBlank()) {
                        Text(
                            text = member.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCall) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("কল করুন")
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "সম্পাদনা করুন")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberFormDialog(
    initial: OrgMember?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (OrgMember) -> Unit
) {
    var fullName by remember { mutableStateOf(initial?.fullName ?: "") }
    var designation by remember { mutableStateOf(initial?.designation ?: "কার্যকরী সদস্য") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var bloodGroup by remember { mutableStateOf(initial?.bloodGroup ?: "O+") }
    var ward by remember { mutableStateOf(initial?.ward ?: "মুকিয়াঘোনা, পূর্ব হারবাং") }
    var monthlyFeeText by remember { mutableStateOf((initial?.monthlyFee ?: 100L).toString()) }
    var joinedLabel by remember { mutableStateOf(initial?.joinedDateLabel ?: "নভেম্বর ২০২৬") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var activeStatus by remember { mutableStateOf(initial?.activeStatus ?: true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    var designationExpanded by remember { mutableStateOf(false) }
    var bloodExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initial == null) "নতুন সদস্য যোগ করুন" else "সদস্যের তথ্য সম্পাদনা")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("পূর্ণ নাম *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input")
                )

                ExposedDropdownMenuBox(
                    expanded = designationExpanded,
                    onExpandedChange = { designationExpanded = it }
                ) {
                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("পদবী *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = designationExpanded) },
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = designationExpanded,
                        onDismissRequest = { designationExpanded = false }
                    ) {
                        DESIGNATIONS.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    designation = item
                                    designationExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_phone_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = bloodExpanded,
                        onExpandedChange = { bloodExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("রক্তের গ্রুপ") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = bloodExpanded,
                            onDismissRequest = { bloodExpanded = false }
                        ) {
                            BLOOD_GROUPS.drop(1).forEach { bg ->
                                DropdownMenuItem(
                                    text = { Text(bg) },
                                    onClick = {
                                        bloodGroup = bg
                                        bloodExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = monthlyFeeText,
                        onValueChange = { monthlyFeeText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("মাসিক চাঁদা (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = ward,
                    onValueChange = { ward = it },
                    label = { Text("পাড়া / এলাকা *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = joinedLabel,
                    onValueChange = { joinedLabel = it },
                    label = { Text("যোগদানের তারিখ *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("বিশেষ নোট (ঐচ্ছিক)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("সক্রিয় সদস্য", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = activeStatus, onCheckedChange = { activeStatus = it })
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
                    val fee = monthlyFeeText.toLongOrNull() ?: -1L
                    when {
                        fullName.isBlank() -> validationError = "সদস্যের নাম লিখুন।"
                        phone.trim().length < 3 -> validationError = "সঠিক মোবাইল নম্বর দিন।"
                        ward.isBlank() -> validationError = "এলাকা লিখুন।"
                        fee < 0L -> validationError = "সঠিক চাঁদার পরিমাণ লিখুন।"
                        else -> {
                            validationError = null
                            onSubmit(
                                OrgMember(
                                    id = initial?.id ?: "",
                                    userId = initial?.userId ?: "",
                                    fullName = fullName.trim(),
                                    designation = designation.trim().ifBlank { "সদস্য" },
                                    phone = phone.trim(),
                                    bloodGroup = bloodGroup,
                                    ward = ward.trim(),
                                    monthlyFee = fee,
                                    activeStatus = activeStatus,
                                    joinedDateLabel = joinedLabel.trim().ifBlank { "আজ" },
                                    notes = notes.trim(),
                                    createdAt = initial?.createdAt
                                )
                            )
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.testTag("save_member_button")
            ) {
                Text(if (isSaving) "সংরক্ষণ হচ্ছে..." else "সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
