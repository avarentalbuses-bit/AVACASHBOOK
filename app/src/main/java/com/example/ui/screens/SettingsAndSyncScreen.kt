package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuthUser
import com.example.data.model.CompanySettingsEntity
import com.example.data.model.StoreAuditReport
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
fun SettingsAndSyncScreen(
    currentUser: AuthUser?,
    settings: CompanySettingsEntity?,
    auditReport: StoreAuditReport?,
    isSyncing: Boolean,
    onSaveSettings: (CompanySettingsEntity) -> Unit,
    onSyncStoreNow: () -> Unit,
    onVerifyStoreIntegrity: () -> Unit,
    onExportStoreJson: ((String) -> Unit) -> Unit,
    onLockApp: () -> Unit,
    onLogout: () -> Unit,
    onUpdatePin: (String) -> Unit
) {
    val context = LocalContext.current

    var companyName by remember(settings) { mutableStateOf(settings?.companyName ?: "AVA PASSENGERS TRANSPORT BY RENTAL BUSES LLC") }
    var address by remember(settings) { mutableStateOf(settings?.address ?: "P.O. Box 91977, Dubai, UAE") }
    var phone by remember(settings) { mutableStateOf(settings?.phone ?: "00971 4 3439666") }
    var email by remember(settings) { mutableStateOf(settings?.email ?: "Contact@avarental.ae") }
    var trn by remember(settings) { mutableStateOf(settings?.trn ?: "100364388700003") }
    var bank by remember(settings) { mutableStateOf(settings?.bank ?: "RAKBANK") }
    var iban by remember(settings) { mutableStateOf(settings?.iban ?: "AE110400008371527349901") }
    var sheetsUrl by remember(settings) { mutableStateOf(settings?.sheetsUrl ?: "") }
    var sheetsSyncEnabled by remember(settings) { mutableStateOf(settings?.sheetsSyncEnabled ?: false) }

    var showPinDialog by remember { mutableStateOf(false) }
    var newPinText by remember { mutableStateOf("") }
    var exportSuccessJson by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F6F4))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // STORE SYNC & ACCURACY AUDIT SECTION
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AvaCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, AvaBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AvaGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = AvaGreenPrimary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Accurate Store Sync Hub",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AvaGreenDark
                            )
                            Text(
                                text = "Local Room DB • SHA-256 Checksums",
                                fontSize = 11.sp,
                                color = Color(0xFF5A6B5F)
                            )
                        }
                    }

                    if (auditReport?.integrityVerified == true) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("100% ACCURATE", color = AvaIncomeGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Audit Details Box
                auditReport?.let { report ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAF8), RoundedCornerShape(12.dp))
                            .border(1.dp, AvaBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Transactions:", fontSize = 12.sp, color = Color(0xFF435247))
                                Text("${report.totalTransactions} records", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Queue Status:", fontSize = 12.sp, color = Color(0xFF435247))
                                Text(
                                    if (report.pendingSyncCount == 0) "All Synced to Store" else "${report.pendingSyncCount} Pending Sync",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (report.pendingSyncCount == 0) AvaIncomeGreen else Color(0xFFE65100)
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Calculated Bank Balance:", fontSize = 12.sp, color = Color(0xFF435247))
                                Text(formatCurrency(report.bankBalance), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Calculated Petty Cash:", fontSize = 12.sp, color = Color(0xFF435247))
                                Text(formatCurrency(report.pettyBalance), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Store Checksum Signature:", fontSize = 12.sp, color = Color(0xFF435247))
                                Text(report.checksumHash, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AvaGreenDark)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSyncStoreNow,
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = AvaGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Force Sync", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onVerifyStoreIntegrity,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Audit Store", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            onExportStoreJson { jsonStr ->
                                exportSuccessJson = jsonStr
                                shareJsonBackup(context, jsonStr)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // GOOGLE SHEETS / CLOUD STORE ENDPOINT
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AvaCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, AvaBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "External Google Sheets / Cloud Sync",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AvaGreenDark
                )
                Text(
                    text = "Post transactions directly to Google Apps Script Web App",
                    fontSize = 11.sp,
                    color = Color(0xFF5A6B5F)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = sheetsUrl,
                    onValueChange = { sheetsUrl = it },
                    label = { Text("Web App Execution URL") },
                    placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Auto-sync transactions on save",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AvaGreenDark
                    )
                    Switch(
                        checked = sheetsSyncEnabled,
                        onCheckedChange = { sheetsSyncEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AvaGreenPrimary)
                    )
                }
            }
        }

        // COMPANY PROFILE & TRN SETTINGS
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AvaCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, AvaBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Company Profile & Banking",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AvaGreenDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = trn,
                        onValueChange = { trn = it },
                        label = { Text("Tax Registration No (TRN)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Office Phone") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Office Depot") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bank,
                        onValueChange = { bank = it },
                        label = { Text("Primary Bank") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = iban,
                        onValueChange = { iban = it },
                        label = { Text("Corporate IBAN") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val s = (settings ?: CompanySettingsEntity()).copy(
                            companyName = companyName,
                            address = address,
                            phone = phone,
                            email = email,
                            trn = trn,
                            bank = bank,
                            iban = iban,
                            sheetsUrl = sheetsUrl.trim(),
                            sheetsSyncEnabled = sheetsSyncEnabled
                        )
                        onSaveSettings(s)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AvaGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Company Settings", fontWeight = FontWeight.Bold)
                }
            }
        }

        // SECURITY & AUTHENTICATED SESSION CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AvaCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, AvaBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Security & Active Session",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AvaGreenDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                currentUser?.let { user ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AvaGreenLight, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = user.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AvaGreenDark)
                                Text(text = "Logged in as @${user.username} • ${user.role}", fontSize = 11.sp, color = Color(0xFF435247))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AvaGoldAccent)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(user.role, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3E2723))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showPinDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change PIN", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onLockApp,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lock Screen", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = AvaExpenseRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logout", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Change PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Update Security PIN") },
            text = {
                OutlinedTextField(
                    value = newPinText,
                    onValueChange = { if (it.length <= 4) newPinText = it },
                    label = { Text("New 4-Digit PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinText.length == 4) {
                            onUpdatePin(newPinText)
                            showPinDialog = false
                            newPinText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AvaGreenPrimary)
                ) {
                    Text("Update PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private fun shareJsonBackup(context: Context, jsonStr: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, jsonStr)
        type = "application/json"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Export AVA Store Backup"))
}
