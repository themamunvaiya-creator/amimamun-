package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.DonationRecord
import com.example.data.model.TransactionType
import com.example.data.model.UiState
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.ExpenseContainer
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.FinancialSummary
import com.example.util.BengaliFormatters

private val DONATION_CATEGORIES = listOf(
    "মাসিক চাঁদা",
    "এককালীন দান",
    "সদকা ও লিল্লাহ",
    "যাকাত তহবিল",
    "মসজিদ ও মাদ্রাসা",
    "দরিদ্র ও অসহায় সাহায্য",
    "রোগী ও চিকিৎসা সহায়তা",
    "ইফতার ও রমজান",
    "দাওয়াহ ও ওয়াজ মাহফিল",
    "সাংগঠনিক ব্যয়"
)

private val PAYMENT_METHODS = listOf("নগদ", "বিকাশ", "নগদ অ্যাপ", "রকেট", "ব্যাংক")

@Composable
fun DonationsScreen(
    donationsState: UiState<List<DonationRecord>>,
    summary: FinancialSummary,
    isSaving: Boolean,
    onSaveDonation: (DonationRecord, () -> Unit) -> Unit,
    onDeleteDonation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var filterType by remember { mutableStateOf("ALL") }
    var editingRecord by remember { mutableStateOf<DonationRecord?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<DonationRecord?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRecord = null
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_donation_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "নতুন দান বা ব্যয়ের হিসাব যোগ করুন")
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "দানের হিসাব ও তহবিল",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        IconButton(
                            onClick = {
                                val report = buildString {
                                    appendLine("পূর্ব হারবাং মুকিয়াঘোনা ইসলামিক যুব সংগঠন")
                                    appendLine("তহবিলের সারসংক্ষেপ:")
                                    appendLine("মোট আয়/দান: ${BengaliFormatters.formatBdt(summary.totalIncome)}")
                                    appendLine("মোট ব্যয়: ${BengaliFormatters.formatBdt(summary.totalExpense)}")
                                    appendLine("বর্তমান তহবিল: ${BengaliFormatters.formatBdt(summary.balance)}")
                                }
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, report)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "হিসাব শেয়ার করুন"))
                            }
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "তহবিলের হিসাব শেয়ার করুন")
                        }
                    }
                }

                // Financial Summary Card + Canvas Bar Ratio
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldDeep),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "বর্তমান নিট তহবিল",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = BengaliFormatters.formatBdt(summary.balance),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Canvas visual bar comparing income vs expense
                            val totalVolume = (summary.totalIncome + summary.totalExpense).coerceAtLeast(1L)
                            val incomeFraction = (summary.totalIncome.toFloat() / totalVolume.toFloat()).coerceIn(0f, 1f)
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                            ) {
                                drawRoundRect(
                                    color = Color(0xFFEF9A9A),
                                    size = size,
                                    cornerRadius = CornerRadius(12f, 12f)
                                )
                                drawRoundRect(
                                    color = Color(0xFF69F0AE),
                                    size = Size(width = size.width * incomeFraction, height = size.height),
                                    cornerRadius = CornerRadius(12f, 12f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "মোট আয় / দান",
                                        color = Color(0xFFA7F3D0),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = BengaliFormatters.formatBdt(summary.totalIncome),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "মোট ব্যয়",
                                        color = Color(0xFFFECACA),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = BengaliFormatters.formatBdt(summary.totalExpense),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterType == "ALL",
                            onClick = { filterType = "ALL" },
                            label = { Text("সব লেনদেন") }
                        )
                        FilterChip(
                            selected = filterType == TransactionType.INCOME.name,
                            onClick = { filterType = TransactionType.INCOME.name },
                            label = { Text("আয় / দান") }
                        )
                        FilterChip(
                            selected = filterType == TransactionType.EXPENSE.name,
                            onClick = { filterType = TransactionType.EXPENSE.name },
                            label = { Text("ব্যয়") }
                        )
                    }
                }

                when (donationsState) {
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
                                text = donationsState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    is UiState.Success -> {
                        val filtered = donationsState.data.filter {
                            filterType == "ALL" || it.txType == filterType
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
                                            imageVector = Icons.Filled.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "এখনও কোনো দানের হিসাব যুক্ত হয়নি",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "নিচের '+' বাটনে ক্লিক করে মাসিক চাঁদা, দান বা ব্যয়ের রশিদ সংরক্ষণ করুন।",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filtered, key = { it.id }) { record ->
                                DonationCard(
                                    record = record,
                                    onShareReceipt = {
                                        val isIncome = TransactionType.fromRaw(record.txType) == TransactionType.INCOME
                                        val text = buildString {
                                            appendLine("পূর্ব হারবাং মুকিয়াঘোনা ইসলামিক যুব সংগঠন")
                                            appendLine(if (isIncome) "দানের রশিদ" else "ব্যয়ের ভাউচার")
                                            appendLine("------------------")
                                            appendLine("বিবরণ/দাতা: ${record.titleOrDonor}")
                                            appendLine("খাত: ${record.category}")
                                            appendLine("পরিমাণ: ${BengaliFormatters.formatBdt(record.amount)}")
                                            appendLine("মাধ্যম: ${record.paymentMethod}")
                                            appendLine("তারিখ: ${record.dateLabel}")
                                            if (record.receiptNumber.isNotBlank()) {
                                                appendLine("রশিদ নং: ${record.receiptNumber}")
                                            }
                                            appendLine("জাযাকাল্লাহু খাইরান।")
                                        }
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, text)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "রশিদ শেয়ার করুন"))
                                    },
                                    onEdit = {
                                        editingRecord = record
                                        showFormDialog = true
                                    },
                                    onDelete = { recordToDelete = record }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        DonationFormDialog(
            initial = editingRecord,
            isSaving = isSaving,
            onDismiss = { showFormDialog = false },
            onSubmit = { record ->
                onSaveDonation(record) {
                    showFormDialog = false
                }
            }
        )
    }

    recordToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("হিসাব মুছে ফেলুন") },
            text = { Text("আপনি কি '${target.titleOrDonor}' এর হিসাবটি মুছে ফেলতে চান?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDonation(target.id)
                        recordToDelete = null
                    }
                ) {
                    Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun DonationCard(
    record: DonationRecord,
    onShareReceipt: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = TransactionType.fromRaw(record.txType) == TransactionType.INCOME
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("donation_card_${record.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isIncome) IncomeContainer else ExpenseContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        tint = if (isIncome) IncomeGreen else ExpenseRed
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.titleOrDonor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${record.category} • ${record.paymentMethod}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = (if (isIncome) "+ " else "- ") + BengaliFormatters.formatBdt(record.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) IncomeGreen else ExpenseRed
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = buildString {
                    append("তারিখ: ${record.dateLabel}")
                    if (record.receiptNumber.isNotBlank()) append(" • রশিদ: ${record.receiptNumber}")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (record.note.isNotBlank()) {
                Text(
                    text = record.note,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onShareReceipt) {
                    Icon(Icons.Filled.Share, contentDescription = "রশিদ শেয়ার করুন")
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
private fun DonationFormDialog(
    initial: DonationRecord?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (DonationRecord) -> Unit
) {
    var txType by remember { mutableStateOf(TransactionType.fromRaw(initial?.txType ?: TransactionType.INCOME.name)) }
    var titleOrDonor by remember { mutableStateOf(initial?.titleOrDonor ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: DONATION_CATEGORIES.first()) }
    var amountText by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var paymentMethod by remember { mutableStateOf(initial?.paymentMethod ?: PAYMENT_METHODS.first()) }
    var dateLabel by remember { mutableStateOf(initial?.dateLabel ?: "১৭ নভেম্বর ২০২৬") }
    var receiptNumber by remember { mutableStateOf(initial?.receiptNumber ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }

    var catExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "নতুন দান / ব্যয়ের হিসাব" else "হিসাব সম্পাদনা") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TransactionType.entries.forEachIndexed { idx, type ->
                        SegmentedButton(
                            selected = txType == type,
                            onClick = { txType = type },
                            shape = SegmentedButtonDefaults.itemShape(idx, TransactionType.entries.size)
                        ) {
                            Text(type.labelBn)
                        }
                    }
                }

                OutlinedTextField(
                    value = titleOrDonor,
                    onValueChange = { titleOrDonor = it },
                    label = { Text(if (txType == TransactionType.INCOME) "দাতার নাম *" else "ব্যয়ের বিবরণ *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("donation_title_input")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("টাকার পরিমাণ (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("donation_amount_input")
                )

                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("খাত *") },
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
                        DONATION_CATEGORIES.forEach { item ->
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
                    ExposedDropdownMenuBox(
                        expanded = methodExpanded,
                        onExpandedChange = { methodExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("মাধ্যম") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = methodExpanded,
                            onDismissRequest = { methodExpanded = false }
                        ) {
                            PAYMENT_METHODS.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    onClick = {
                                        paymentMethod = method
                                        methodExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dateLabel,
                        onValueChange = { dateLabel = it },
                        label = { Text("তারিখ *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = receiptNumber,
                    onValueChange = { receiptNumber = it },
                    label = { Text("রশিদ / ভাউচার নং (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("মন্তব্য (ঐচ্ছিক)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

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
                    val amount = amountText.toLongOrNull() ?: 0L
                    when {
                        titleOrDonor.isBlank() -> validationError = "দাতা বা ব্যয়ের বিবরণ লিখুন।"
                        amount < 1L -> validationError = "সঠিক টাকার পরিমাণ লিখুন।"
                        dateLabel.isBlank() -> validationError = "তারিখ লিখুন।"
                        else -> {
                            validationError = null
                            onSubmit(
                                DonationRecord(
                                    id = initial?.id ?: "",
                                    userId = initial?.userId ?: "",
                                    titleOrDonor = titleOrDonor.trim(),
                                    txType = txType.name,
                                    category = category.trim().ifBlank { "অন্যান্য" },
                                    amount = amount,
                                    paymentMethod = paymentMethod,
                                    dateLabel = dateLabel.trim(),
                                    receiptNumber = receiptNumber.trim(),
                                    note = note.trim(),
                                    createdAt = initial?.createdAt
                                )
                            )
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.testTag("save_donation_button")
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
