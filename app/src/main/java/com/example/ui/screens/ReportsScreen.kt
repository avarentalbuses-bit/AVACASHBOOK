package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountTransaction
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AvaBorder
import com.example.ui.theme.AvaCardBg
import com.example.ui.theme.AvaExpenseRed
import com.example.ui.theme.AvaGoldAccent
import com.example.ui.theme.AvaGoldDark
import com.example.ui.theme.AvaGreenDark
import com.example.ui.theme.AvaGreenPrimary
import com.example.ui.theme.AvaIncomeGreen

@Composable
fun ReportsScreen(
    transactions: List<AccountTransaction>
) {
    val context = LocalContext.current

    val months = remember(transactions) {
        val list = transactions.mapNotNull { it.date.takeIf { d -> d.length >= 7 }?.substring(0, 7) }
            .distinct()
            .sortedDescending()
            .toMutableList()
        list
    }

    var selectedMonth by remember(months) {
        mutableStateOf(months.firstOrNull() ?: "All")
    }

    val filtered = remember(transactions, selectedMonth) {
        if (selectedMonth == "All") transactions
        else transactions.filter { it.date.startsWith(selectedMonth) }
    }

    // Calculations
    val incomeTotal = remember(filtered) {
        val directIncome = filtered.filter { it.registerType == "INCOME" }.sumOf { it.amount }
        val bankIn = filtered.filter { it.registerType == "BANK" && it.cashIn > 0 }.sumOf { it.cashIn }
        Math.max(directIncome, bankIn)
    }

    val rentTotal = remember(filtered) {
        filtered.filter { it.registerType == "RENT" }.sumOf { it.amount }
    }

    val salaryTotal = remember(filtered) {
        filtered.filter { it.registerType == "SALARY" }.sumOf { it.amount }
    }

    val fuelTotal = remember(filtered) {
        filtered.filter { it.registerType == "FUEL" }.sumOf { it.amount }
    }

    val expenseTotal = remember(filtered) {
        val ops = rentTotal + salaryTotal + fuelTotal
        val bankOut = filtered.filter { it.registerType in listOf("BANK", "PETTY") }.sumOf { it.cashOut }
        Math.max(ops, bankOut)
    }

    val netPosition = incomeTotal - expenseTotal

    // Grouping by category
    val categorySummary = remember(filtered) {
        val map = mutableMapOf<String, Pair<Double, Double>>() // category -> (In, Out)
        filtered.forEach { tx ->
            val cat = tx.category.ifEmpty { "General & Ops" }
            val current = map.getOrDefault(cat, Pair(0.0, 0.0))
            val cashIn = if (tx.registerType == "INCOME") tx.amount else tx.cashIn
            val cashOut = if (tx.registerType in listOf("RENT", "SALARY", "FUEL")) tx.amount else tx.cashOut
            map[cat] = Pair(current.first + cashIn, current.second + cashOut)
        }
        map.toList().sortedByDescending { it.second.second + it.second.first }
    }

    // Grouping fuel by bus
    val fuelByBus = remember(filtered) {
        filtered.filter { it.registerType == "FUEL" && it.busNo.isNotEmpty() }
            .groupBy { it.busNo }
            .map { (bus, txs) ->
                val liters = txs.sumOf { it.quantityLiters }
                val cost = txs.sumOf { it.amount }
                Triple(bus, liters, cost)
            }
            .sortedByDescending { it.third }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F6F4))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector Bar
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AvaCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, AvaBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Monthly Financial Audit Report",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AvaGreenDark
                        )

                        OutlinedButton(
                            onClick = {
                                shareReportWhatsApp(context, selectedMonth, incomeTotal, expenseTotal, fuelTotal, netPosition)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                label = "All Time",
                                selected = selectedMonth == "All",
                                onClick = { selectedMonth = "All" }
                            )
                        }
                        items(months) { m ->
                            FilterChip(
                                label = m,
                                selected = selectedMonth == m,
                                onClick = { selectedMonth = m }
                            )
                        }
                    }
                }
            }
        }

        // Summary KPI Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReportKpi(
                        label = "Total Revenues",
                        amount = incomeTotal,
                        color = AvaIncomeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    ReportKpi(
                        label = "Total Outflows",
                        amount = expenseTotal,
                        color = AvaExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReportKpi(
                        label = "Fuel Diesel Expense",
                        amount = fuelTotal,
                        color = AvaGoldDark,
                        modifier = Modifier.weight(1f)
                    )
                    ReportKpi(
                        label = "Net Cashflow Position",
                        amount = netPosition,
                        color = if (netPosition >= 0) AvaGreenPrimary else AvaExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Category Breakdown Table Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AvaCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, AvaBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Category Revenue & Expense Breakdown",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AvaGreenDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F2), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text("Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.4f))
                        Text("Inflow", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AvaIncomeGreen, modifier = Modifier.weight(1f))
                        Text("Outflow", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AvaExpenseRed, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (categorySummary.isEmpty()) {
                        Text(
                            text = "No category data available for this timeframe.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        categorySummary.forEach { (cat, inOut) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AvaGreenDark,
                                    modifier = Modifier.weight(1.4f)
                                )
                                Text(
                                    text = if (inOut.first > 0) formatCurrency(inOut.first) else "-",
                                    fontSize = 12.sp,
                                    color = if (inOut.first > 0) AvaIncomeGreen else Color.Gray,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (inOut.second > 0) formatCurrency(inOut.second) else "-",
                                    fontSize = 12.sp,
                                    color = if (inOut.second > 0) AvaExpenseRed else Color.Gray,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fleet Fuel Consumption Analysis Card
        if (fuelByBus.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AvaCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AvaBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = AvaGoldDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bus Fleet Diesel Fuel Consumption",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AvaGreenDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        fuelByBus.forEach { (bus, liters, cost) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Bus Plate: $bus", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AvaGreenDark)
                                    Text(text = "Diesel volume: ${String.format(java.util.Locale.US, "%.1f", liters)} Liters", fontSize = 11.sp, color = Color(0xFF5A6B5F))
                                }
                                Text(text = formatCurrency(cost), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AvaExpenseRed)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ReportKpi(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AvaCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AvaBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontSize = 11.sp, color = Color(0xFF5A6B5F), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = formatCurrency(amount), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

private fun shareReportWhatsApp(
    context: Context,
    month: String,
    income: Double,
    expense: Double,
    fuel: Double,
    net: Double
) {
    val message = """
        📊 AVA ACCOUNTING PRO
        Monthly Accounts Statement: $month
        
        📈 Total Revenues: ${formatCurrency(income)}
        📉 Total Operational Expenses: ${formatCurrency(expense)}
        ⛽ Total Fuel Cost: ${formatCurrency(fuel)}
        💼 Net Operational Cashflow: ${formatCurrency(net)}
        
        Verified in Room ACID Database Store
        AVA Passengers Transport LLC Dubai
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, "Share Report via WhatsApp"))
}
