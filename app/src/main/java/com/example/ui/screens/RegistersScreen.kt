package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountTransaction
import com.example.ui.components.SyncStatusChip
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AvaBorder
import com.example.ui.theme.AvaCardBg
import com.example.ui.theme.AvaExpenseRed
import com.example.ui.theme.AvaGoldAccent
import com.example.ui.theme.AvaGoldLight
import com.example.ui.theme.AvaGreenDark
import com.example.ui.theme.AvaGreenLight
import com.example.ui.theme.AvaGreenPrimary
import com.example.ui.theme.AvaIncomeGreen

@Composable
fun RegistersScreen(
    currentRegister: String,
    onRegisterChange: (String) -> Unit,
    transactions: List<AccountTransaction>,
    onAddClick: () -> Unit,
    onEditClick: (AccountTransaction) -> Unit,
    onDeleteClick: (AccountTransaction) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf<String?>(null) }
    var txToDelete by remember { mutableStateOf<AccountTransaction?>(null) }

    val registerTabs = listOf(
        "BANK" to "🏦 Bank Account",
        "PETTY" to "💵 Petty Cash",
        "INCOME" to "📈 Income",
        "RENT" to "🏢 Rent",
        "SALARY" to "👥 Salary & Wages",
        "FUEL" to "⛽ Fuel Register"
    )

    val currentTabIndex = registerTabs.indexOfFirst { it.first == currentRegister }.coerceAtLeast(0)

    // Filter transactions for this register
    val registerTransactions = remember(transactions, currentRegister, searchQuery, selectedMonth) {
        transactions.filter { it.registerType == currentRegister }
            .filter { tx ->
                searchQuery.isEmpty() ||
                        tx.description.contains(searchQuery, ignoreCase = true) ||
                        tx.payeeOrCustomer.contains(searchQuery, ignoreCase = true) ||
                        tx.reference.contains(searchQuery, ignoreCase = true) ||
                        tx.category.contains(searchQuery, ignoreCase = true) ||
                        tx.busNo.contains(searchQuery, ignoreCase = true)
            }
            .filter { tx ->
                selectedMonth == null || tx.date.startsWith(selectedMonth!!)
            }
    }

    // Compute running balance or total sum
    val runningBalance = remember(transactions, currentRegister) {
        val allOfRegister = transactions.filter { it.registerType == currentRegister }
        when (currentRegister) {
            "BANK", "PETTY" -> allOfRegister.sumOf { it.cashIn - it.cashOut }
            else -> allOfRegister.sumOf { it.amount }
        }
    }

    // Available months in data
    val availableMonths = remember(transactions) {
        transactions.mapNotNull { it.date.takeIf { d -> d.length >= 7 }?.substring(0, 7) }
            .distinct()
            .sortedDescending()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F6F4))
    ) {
        // Registers Tab Selector
        ScrollableTabRow(
            selectedTabIndex = currentTabIndex,
            containerColor = AvaGreenDark,
            contentColor = Color.White,
            edgePadding = 12.dp
        ) {
            registerTabs.forEachIndexed { idx, (key, label) ->
                Tab(
                    selected = currentTabIndex == idx,
                    onClick = { onRegisterChange(key) },
                    text = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (currentTabIndex == idx) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentTabIndex == idx) AvaGoldAccent else Color(0xFFD0DDD3)
                        )
                    }
                )
            }
        }

        // Header with Live Running Balance
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = AvaCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = when (currentRegister) {
                                "BANK" -> "RAKBANK Current Account Balance"
                                "PETTY" -> "Petty Cash on Hand"
                                "INCOME" -> "Total Registered Inflows"
                                "RENT" -> "Total Rent Disbursed"
                                "SALARY" -> "Total Payroll WPS Disbursed"
                                "FUEL" -> "Total Fuel Expenses"
                                else -> "Running Balance"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF5A6B5F),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatCurrency(runningBalance),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (currentRegister in listOf("BANK", "PETTY", "INCOME")) AvaGreenPrimary else AvaExpenseRed
                        )
                    }

                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AvaGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_to_register_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Entry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by description, reference, driver, or bus...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AvaGreenPrimary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Text("✕", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AvaGreenPrimary,
                        unfocusedBorderColor = AvaBorder
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                // Month filter chips
                if (availableMonths.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            FilterChip(
                                label = "All Months",
                                selected = selectedMonth == null,
                                onClick = { selectedMonth = null }
                            )
                        }
                        items(availableMonths) { m ->
                            FilterChip(
                                label = m,
                                selected = selectedMonth == m,
                                onClick = { selectedMonth = if (selectedMonth == m) null else m }
                            )
                        }
                    }
                }
            }
        }

        // Transactions List
        if (registerTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No transactions found in this register",
                        color = Color(0xFF78909C),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onAddClick) {
                        Text("+ Create First Transaction")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(registerTransactions, key = { it.id }) { tx ->
                    RegisterTransactionCard(
                        transaction = tx,
                        onEdit = { onEditClick(tx) },
                        onDelete = { txToDelete = tx },
                        onShareWhatsApp = {
                            if (tx.phone.isNotEmpty()) {
                                openWhatsApp(context, tx.phone, "Hello ${tx.payeeOrCustomer}, regards AVA Passengers Transport.")
                            }
                        }
                    )
                }
            }
        }
    }

    // Confirm Delete Dialog
    txToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to permanently remove this transaction from the store?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick(tx)
                        txToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AvaExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) AvaGreenPrimary else Color(0xFFECEFF1))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else Color(0xFF37474F)
        )
    }
}

@Composable
fun RegisterTransactionCard(
    transaction: AccountTransaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AvaCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AvaBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Date, Category & Sync Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.date,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AvaGreenDark
                    )
                    if (transaction.category.isNotEmpty()) {
                        Text(
                            text = " • ${transaction.category}",
                            fontSize = 11.sp,
                            color = Color(0xFF5A6B5F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (transaction.subCategory.isNotEmpty()) {
                        Text(
                            text = " (${transaction.subCategory})",
                            fontSize = 10.sp,
                            color = Color(0xFF78909C)
                        )
                    }
                }

                SyncStatusChip(status = transaction.syncStatus)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Description & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description.ifEmpty { transaction.payeeOrCustomer.ifEmpty { "Transaction" } },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AvaGreenDark
                    )

                    if (transaction.payeeOrCustomer.isNotEmpty()) {
                        Text(
                            text = "Party: ${transaction.payeeOrCustomer}",
                            fontSize = 12.sp,
                            color = Color(0xFF455A64)
                        )
                    }

                    if (transaction.reference.isNotEmpty()) {
                        Text(
                            text = "Ref / Voucher: ${transaction.reference}",
                            fontSize = 11.sp,
                            color = Color(0xFF78909C)
                        )
                    }
                }

                // Amount Section
                Column(horizontalAlignment = Alignment.End) {
                    when (transaction.registerType) {
                        "BANK", "PETTY" -> {
                            if (transaction.cashIn > 0) {
                                Text(
                                    text = "+${formatCurrency(transaction.cashIn)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AvaIncomeGreen
                                )
                            }
                            if (transaction.cashOut > 0) {
                                Text(
                                    text = "-${formatCurrency(transaction.cashOut)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AvaExpenseRed
                                )
                            }
                        }
                        "INCOME" -> {
                            Text(
                                text = "+${formatCurrency(transaction.totalWithVat.takeIf { it > 0 } ?: transaction.amount)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AvaIncomeGreen
                            )
                            if (transaction.vatAmount > 0) {
                                Text(
                                    text = "incl 5% VAT (${formatCurrency(transaction.vatAmount)})",
                                    fontSize = 10.sp,
                                    color = Color(0xFF5A6B5F)
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = "-${formatCurrency(transaction.amount)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AvaExpenseRed
                            )
                        }
                    }
                }
            }

            // Register specific details (Fuel, Salary, etc.)
            when (transaction.registerType) {
                "FUEL" -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFBE9E7), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bus: ${transaction.busNo.ifEmpty { "-" }}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Station: ${transaction.station.ifEmpty { "ADNOC" }}", fontSize = 11.sp)
                            Text("Vol: ${transaction.quantityLiters} L @ ${transaction.unitPrice} AED", fontSize = 11.sp)
                        }
                    }
                }
                "SALARY" -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEDE7F6), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Role: ${transaction.designation.ifEmpty { "Staff" }}", fontSize = 11.sp)
                            Text("OT: ${transaction.otHours}h (${formatCurrency(transaction.otAmount)})", fontSize = 11.sp)
                            Text("Net: ${formatCurrency(transaction.netPay.takeIf { it > 0 } ?: transaction.amount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                "RENT" -> {
                    if (transaction.propertyLocation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Property: ${transaction.propertyLocation} (${transaction.rentalPeriod})",
                            fontSize = 11.sp,
                            color = Color(0xFF607D8B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action row: Signature, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SHA: ${transaction.hashSignature.take(12)}",
                    fontSize = 9.sp,
                    color = Color(0xFF90A4AE),
                    fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (transaction.phone.isNotEmpty()) {
                        IconButton(
                            onClick = onShareWhatsApp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "WhatsApp",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = AvaGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = AvaExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun openWhatsApp(context: Context, phone: String, message: String) {
    val cleanPhone = phone.replace(Regex("[^0-9]"), "")
    val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        context.startActivity(intent)
    } catch (_: Exception) {}
}
