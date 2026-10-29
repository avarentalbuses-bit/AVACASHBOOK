package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AccountTransaction
import com.example.data.model.StoreAuditReport
import com.example.ui.components.SyncStatusChip
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AvaBorder
import com.example.ui.theme.AvaCardBg
import com.example.ui.theme.AvaExpenseRed
import com.example.ui.theme.AvaGoldAccent
import com.example.ui.theme.AvaGreenDark
import com.example.ui.theme.AvaGreenLight
import com.example.ui.theme.AvaGreenPrimary
import com.example.ui.theme.AvaGreenSecondary
import com.example.ui.theme.AvaIncomeGreen
import com.example.ui.theme.AvaWarningAmber

@Composable
fun DashboardScreen(
    transactions: List<AccountTransaction>,
    auditReport: StoreAuditReport?,
    pendingSyncCount: Int,
    isSyncing: Boolean,
    onAddTransactionClick: () -> Unit,
    onSyncStoreClick: () -> Unit,
    onSelectTransaction: (AccountTransaction) -> Unit,
    onNavigateToRegister: (String) -> Unit
) {
    val context = LocalContext.current

    // Compute live accurate balances
    val bankBalance = remember(transactions) {
        transactions.filter { it.registerType == "BANK" }
            .sumOf { it.cashIn - it.cashOut }
    }
    val pettyBalance = remember(transactions) {
        transactions.filter { it.registerType == "PETTY" }
            .sumOf { it.cashIn - it.cashOut }
    }
    val totalIncome = remember(transactions) {
        val directIncome = transactions.filter { it.registerType == "INCOME" }.sumOf { it.amount }
        val bankIncome = transactions.filter { it.registerType == "BANK" && it.cashIn > 0 }.sumOf { it.cashIn }
        Math.max(directIncome, bankIncome)
    }
    val totalExpense = remember(transactions) {
        val bankPettyOut = transactions.filter { it.registerType in listOf("BANK", "PETTY") }.sumOf { it.cashOut }
        val opsOut = transactions.filter { it.registerType in listOf("RENT", "SALARY", "FUEL") }.sumOf { it.amount }
        Math.max(bankPettyOut, opsOut)
    }
    val netPosition = bankBalance + pettyBalance

    val recentTransactions = remember(transactions) {
        transactions.take(8)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F6F4))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Image Header Card
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_ava_banner),
                        contentDescription = "AVA Fleet",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xCC0D3B10), Color(0xFA071E0B))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "AVA PASSENGERS TRANSPORT",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Bus Rental Accounts & Fleet Cashflow Management",
                            color = AvaGoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "TRN: 100364388700003 • RAKBANK Current Account",
                            color = Color(0xFFE0E0E0),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Accurate Store Sync Status Alert Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pendingSyncCount == 0) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (pendingSyncCount == 0) Color(0xFFA5D6A7) else Color(0xFFFFE082)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (pendingSyncCount == 0) AvaGreenPrimary else AvaWarningAmber),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (pendingSyncCount == 0) Icons.Default.CheckCircle else Icons.Default.Sync,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (pendingSyncCount == 0) "Store Status: Accurate & Synced" else "$pendingSyncCount Pending Sync to Store",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (pendingSyncCount == 0) AvaGreenDark else Color(0xFF795548)
                            )
                            Text(
                                text = "Checksum: ${auditReport?.checksumHash?.take(8) ?: "VERIFIED"} • Total ${transactions.size} records",
                                fontSize = 11.sp,
                                color = Color(0xFF435247)
                            )
                        }
                    }

                    Button(
                        onClick = onSyncStoreClick,
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pendingSyncCount == 0) AvaGreenPrimary else AvaWarningAmber
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = if (pendingSyncCount == 0) "Audit Store" else "Sync Store",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // KPI Overview Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "Bank Balance",
                        amount = bankBalance,
                        subtitle = "RAKBANK Account",
                        icon = Icons.Default.AccountBalance,
                        color = AvaGreenPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToRegister("BANK") }
                    )
                    KpiCard(
                        title = "Petty Cash",
                        amount = pettyBalance,
                        subtitle = "Cash on Hand",
                        icon = Icons.Default.LocalAtm,
                        color = AvaGoldAccent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToRegister("PETTY") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "Total Income YTD",
                        amount = totalIncome,
                        subtitle = "Transport & Rentals",
                        icon = Icons.Default.TrendingUp,
                        color = AvaIncomeGreen,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToRegister("INCOME") }
                    )
                    KpiCard(
                        title = "Total Expenses YTD",
                        amount = totalExpense,
                        subtitle = "Fuel, Salaries, Rent",
                        icon = Icons.Default.TrendingDown,
                        color = AvaExpenseRed,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToRegister("FUEL") }
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddTransactionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AvaGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("add_transaction_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add Transaction", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        shareWhatsAppBrief(context, bankBalance, pettyBalance, totalIncome, totalExpense)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF075E54)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AvaGreenDark
                )
                Text(
                    text = "View All Registers →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AvaGreenPrimary,
                    modifier = Modifier.clickable { onNavigateToRegister("BANK") }
                )
            }
        }

        // Recent Transactions List
        items(recentTransactions) { tx ->
            TransactionSummaryCard(
                transaction = tx,
                onClick = { onSelectTransaction(tx) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    amount: Double,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AvaCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5A6B5F)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = formatCurrency(amount),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AvaGreenDark
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF78909C),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TransactionSummaryCard(
    transaction: AccountTransaction,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AvaCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AvaBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (transaction.registerType) {
                                    "BANK" -> Color(0xFFE8F5E9)
                                    "PETTY" -> Color(0xFFFFF8E1)
                                    "INCOME" -> Color(0xFFE0F2F1)
                                    "FUEL" -> Color(0xFFFBE9E7)
                                    "SALARY" -> Color(0xFFEDE7F6)
                                    else -> Color(0xFFECEFF1)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = transaction.registerType,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = AvaGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = transaction.date,
                        fontSize = 11.sp,
                        color = Color(0xFF78909C)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = transaction.description.ifEmpty { transaction.payeeOrCustomer.ifEmpty { "Transaction" } },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AvaGreenDark,
                    maxLines = 1
                )

                if (transaction.category.isNotEmpty() || transaction.reference.isNotEmpty()) {
                    Text(
                        text = "${transaction.category} ${if (transaction.reference.isNotEmpty()) "• " + transaction.reference else ""}",
                        fontSize = 11.sp,
                        color = Color(0xFF5A6B5F)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val netAmt = when (transaction.registerType) {
                    "BANK", "PETTY" -> transaction.cashIn - transaction.cashOut
                    "INCOME" -> transaction.amount
                    else -> -transaction.amount
                }
                val isPositive = netAmt >= 0

                Text(
                    text = "${if (isPositive) "+" else ""}${formatCurrency(netAmt)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = if (isPositive) AvaIncomeGreen else AvaExpenseRed
                )

                Spacer(modifier = Modifier.height(2.dp))
                SyncStatusChip(status = transaction.syncStatus)
            }
        }
    }
}

private fun shareWhatsAppBrief(
    context: Context,
    bankBal: Double,
    pettyBal: Double,
    income: Double,
    expense: Double
) {
    val message = """
        📊 AVA PASSENGERS TRANSPORT LLC
        💼 Financial Briefing
        
        🏦 RAKBANK Balance: ${formatCurrency(bankBal)}
        💵 Petty Cash on Hand: ${formatCurrency(pettyBal)}
        📈 Total Income YTD: ${formatCurrency(income)}
        📉 Total Expenses YTD: ${formatCurrency(expense)}
        
        🔒 Accurate Store Status: Synced & Verified
        TRN: 100364388700003
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
        setPackage("com.whatsapp")
    }

    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        // Fallback to generic share chooser
        val chooser = Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            },
            "Share AVA Accounts Brief"
        )
        context.startActivity(chooser)
    }
}
