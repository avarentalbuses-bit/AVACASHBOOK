package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AccountTransaction
import com.example.data.model.CategoryEntity
import com.example.data.model.DriverEntity
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AvaBorder
import com.example.ui.theme.AvaCardBg
import com.example.ui.theme.AvaGoldLight
import com.example.ui.theme.AvaGreenDark
import com.example.ui.theme.AvaGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDialog(
    initialTransaction: AccountTransaction? = null,
    defaultRegister: String = "BANK",
    drivers: List<DriverEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (AccountTransaction) -> Unit
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    var registerType by remember { mutableStateOf(initialTransaction?.registerType ?: defaultRegister) }
    var date by remember { mutableStateOf(initialTransaction?.date ?: today) }
    var description by remember { mutableStateOf(initialTransaction?.description ?: "") }
    var category by remember { mutableStateOf(initialTransaction?.category ?: "") }
    var subCategory by remember { mutableStateOf(initialTransaction?.subCategory ?: "") }
    var cashInText by remember { mutableStateOf(if ((initialTransaction?.cashIn ?: 0.0) > 0) initialTransaction!!.cashIn.toString() else "") }
    var cashOutText by remember { mutableStateOf(if ((initialTransaction?.cashOut ?: 0.0) > 0) initialTransaction!!.cashOut.toString() else "") }
    var amountText by remember { mutableStateOf(if ((initialTransaction?.amount ?: 0.0) > 0) initialTransaction!!.amount.toString() else "") }
    var reference by remember { mutableStateOf(initialTransaction?.reference ?: "") }
    var payeeOrCustomer by remember { mutableStateOf(initialTransaction?.payeeOrCustomer ?: "") }
    var phone by remember { mutableStateOf(initialTransaction?.phone ?: "") }
    var busNo by remember { mutableStateOf(initialTransaction?.busNo ?: "") }
    var station by remember { mutableStateOf(initialTransaction?.station ?: "ADNOC") }
    var odometerText by remember { mutableStateOf(if ((initialTransaction?.odometer ?: 0.0) > 0) initialTransaction!!.odometer.toString() else "") }
    var litersText by remember { mutableStateOf(if ((initialTransaction?.quantityLiters ?: 0.0) > 0) initialTransaction!!.quantityLiters.toString() else "") }
    var unitPriceText by remember { mutableStateOf(if ((initialTransaction?.unitPrice ?: 0.0) > 0) initialTransaction!!.unitPrice.toString() else "3.20") }
    var designation by remember { mutableStateOf(initialTransaction?.designation ?: "Heavy Bus Driver") }
    var basicSalaryText by remember { mutableStateOf(if ((initialTransaction?.basicSalary ?: 0.0) > 0) initialTransaction!!.basicSalary.toString() else "3500") }
    var otHoursText by remember { mutableStateOf(if ((initialTransaction?.otHours ?: 0.0) > 0) initialTransaction!!.otHours.toString() else "") }
    var otRateText by remember { mutableStateOf(if ((initialTransaction?.otRate ?: 0.0) > 0) initialTransaction!!.otRate.toString() else "25") }
    var allowancesText by remember { mutableStateOf(if ((initialTransaction?.allowances ?: 0.0) > 0) initialTransaction!!.allowances.toString() else "") }
    var deductionsText by remember { mutableStateOf(if ((initialTransaction?.deductions ?: 0.0) > 0) initialTransaction!!.deductions.toString() else "") }
    var propertyLocation by remember { mutableStateOf(initialTransaction?.propertyLocation ?: "") }
    var rentalPeriod by remember { mutableStateOf(initialTransaction?.rentalPeriod ?: "") }
    var receiptNote by remember { mutableStateOf(initialTransaction?.receiptNote ?: "") }

    var registerMenuOpen by remember { mutableStateOf(false) }
    var driverMenuOpen by remember { mutableStateOf(false) }
    var stationMenuOpen by remember { mutableStateOf(false) }

    // Dynamic calculations for Fuel & Income & Salary
    LaunchedEffect(litersText, unitPriceText) {
        if (registerType == "FUEL") {
            val l = litersText.toDoubleOrNull() ?: 0.0
            val p = unitPriceText.toDoubleOrNull() ?: 0.0
            if (l > 0 && p > 0) {
                amountText = String.format(Locale.US, "%.2f", l * p)
            }
        }
    }

    LaunchedEffect(basicSalaryText, otHoursText, otRateText, allowancesText, deductionsText) {
        if (registerType == "SALARY") {
            val basic = basicSalaryText.toDoubleOrNull() ?: 0.0
            val otH = otHoursText.toDoubleOrNull() ?: 0.0
            val otR = otRateText.toDoubleOrNull() ?: 0.0
            val allow = allowancesText.toDoubleOrNull() ?: 0.0
            val ded = deductionsText.toDoubleOrNull() ?: 0.0
            val net = basic + (otH * otR) + allow - ded
            amountText = String.format(Locale.US, "%.2f", net)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AvaCardBg),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Dialog Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (initialTransaction == null) "New $registerType Transaction" else "Edit Transaction",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = AvaGreenDark
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Register Switcher Dropdown
                    Box {
                        OutlinedTextField(
                            value = registerType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Register") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { registerMenuOpen = true })
                            },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AvaGreenPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { registerMenuOpen = true }
                        )

                        DropdownMenu(
                            expanded = registerMenuOpen,
                            onDismissRequest = { registerMenuOpen = false }
                        ) {
                            listOf(
                                "BANK" to "Bank Account",
                                "PETTY" to "Petty Cash",
                                "INCOME" to "Income Register",
                                "RENT" to "Rent Register",
                                "SALARY" to "Salary & Payroll",
                                "FUEL" to "Fuel Register"
                            ).forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        registerType = code
                                        registerMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Date & Reference Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = reference,
                            onValueChange = { reference = it },
                            label = { Text("Ref / Chq / LPO") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description / Details") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_description_input")
                    )

                    // Register-Specific Fields
                    when (registerType) {
                        "BANK", "PETTY" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = cashInText,
                                    onValueChange = { cashInText = it },
                                    label = { Text("Cash In (AED)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("cash_in_input")
                                )
                                OutlinedTextField(
                                    value = cashOutText,
                                    onValueChange = { cashOutText = it },
                                    label = { Text("Cash Out (AED)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("cash_out_input")
                                )
                            }
                            OutlinedTextField(
                                value = payeeOrCustomer,
                                onValueChange = { payeeOrCustomer = it },
                                label = { Text("Payee / Customer / Source") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        "INCOME" -> {
                            OutlinedTextField(
                                value = payeeOrCustomer,
                                onValueChange = { payeeOrCustomer = it },
                                label = { Text("Customer / Client Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = it },
                                label = { Text("Base Revenue Amount (AED)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            val baseAmt = amountText.toDoubleOrNull() ?: 0.0
                            val vat5 = baseAmt * 0.05
                            val totalWithVat = baseAmt + vat5
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AvaGoldLight, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "5% VAT: ${formatCurrency(vat5)}  •  Total Invoiced: ${formatCurrency(totalWithVat)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6D4C00)
                                )
                            }
                        }

                        "RENT" -> {
                            OutlinedTextField(
                                value = payeeOrCustomer,
                                onValueChange = { payeeOrCustomer = it },
                                label = { Text("Landlord / Real Estate Co") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = propertyLocation,
                                onValueChange = { propertyLocation = it },
                                label = { Text("Property / Yard Location") },
                                placeholder = { Text("e.g. Al Qusais Depot, Staff Accommodation") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = rentalPeriod,
                                    onValueChange = { rentalPeriod = it },
                                    label = { Text("Period (e.g. Jul 2026)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = amountText,
                                    onValueChange = { amountText = it },
                                    label = { Text("Rent Amount (AED)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        "FUEL" -> {
                            // Driver Dropdown from Roster
                            Box {
                                OutlinedTextField(
                                    value = payeeOrCustomer,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Select Driver (from Roster)") },
                                    trailingIcon = {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { driverMenuOpen = true })
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { driverMenuOpen = true }
                                )

                                DropdownMenu(
                                    expanded = driverMenuOpen,
                                    onDismissRequest = { driverMenuOpen = false }
                                ) {
                                    drivers.forEach { d ->
                                        DropdownMenuItem(
                                            text = { Text("${d.name} (${d.busNo})") },
                                            onClick = {
                                                payeeOrCustomer = d.name
                                                busNo = d.busNo
                                                phone = d.phone
                                                driverMenuOpen = false
                                            }
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = busNo,
                                    onValueChange = { busNo = it },
                                    label = { Text("Bus Plate No") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = station,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Fuel Station") },
                                        trailingIcon = {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { stationMenuOpen = true })
                                        },
                                        modifier = Modifier.clickable { stationMenuOpen = true }
                                    )
                                    DropdownMenu(
                                        expanded = stationMenuOpen,
                                        onDismissRequest = { stationMenuOpen = false }
                                    ) {
                                        listOf("ADNOC", "ENOC", "Emarat", "Al Bahar", "Other").forEach { st ->
                                            DropdownMenuItem(
                                                text = { Text(st) },
                                                onClick = {
                                                    station = st
                                                    stationMenuOpen = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = litersText,
                                    onValueChange = { litersText = it },
                                    label = { Text("Liters") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = unitPriceText,
                                    onValueChange = { unitPriceText = it },
                                    label = { Text("AED / Liter") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = odometerText,
                                    onValueChange = { odometerText = it },
                                    label = { Text("Odometer (km)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f)
                                )
                            }

                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = it },
                                label = { Text("Total Fuel Expense (AED)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        "SALARY" -> {
                            OutlinedTextField(
                                value = payeeOrCustomer,
                                onValueChange = { payeeOrCustomer = it },
                                label = { Text("Employee / Driver Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = basicSalaryText,
                                    onValueChange = { basicSalaryText = it },
                                    label = { Text("Basic (AED)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = otHoursText,
                                    onValueChange = { otHoursText = it },
                                    label = { Text("OT Hours") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = otRateText,
                                    onValueChange = { otRateText = it },
                                    label = { Text("OT Rate/hr") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = it },
                                label = { Text("Calculated Net Pay (AED)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Category & Sub-Category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            placeholder = { Text(if (registerType == "INCOME") "Income" else "Expense") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = subCategory,
                            onValueChange = { subCategory = it },
                            label = { Text("Sub-Category") },
                            placeholder = { Text("e.g. Fuel, Fleet, ALICO") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Receipt / Attachment note
                    OutlinedTextField(
                        value = receiptNote,
                        onValueChange = { receiptNote = it },
                        label = { Text("Receipt / Tax Invoice Attachment Ref") },
                        placeholder = { Text("e.g. ADNOC-00912.pdf / Scan Attached") },
                        leadingIcon = {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = AvaGreenPrimary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val inVal = cashInText.toDoubleOrNull() ?: 0.0
                            val outVal = cashOutText.toDoubleOrNull() ?: 0.0
                            val amtVal = amountText.toDoubleOrNull() ?: (if (inVal > 0) inVal else outVal)

                            val vatVal = if (registerType == "INCOME") amtVal * 0.05 else 0.0
                            val totalWithVatVal = if (registerType == "INCOME") amtVal + vatVal else amtVal

                            val updated = (initialTransaction ?: AccountTransaction(registerType = registerType, date = date)).copy(
                                registerType = registerType,
                                date = date,
                                description = description.ifEmpty { payeeOrCustomer },
                                category = category.ifEmpty { if (registerType == "INCOME") "Income" else "Expense" },
                                subCategory = subCategory,
                                cashIn = inVal,
                                cashOut = outVal,
                                amount = amtVal,
                                vatAmount = vatVal,
                                totalWithVat = totalWithVatVal,
                                reference = reference,
                                payeeOrCustomer = payeeOrCustomer,
                                phone = phone,
                                busNo = busNo,
                                station = station,
                                odometer = odometerText.toDoubleOrNull() ?: 0.0,
                                quantityLiters = litersText.toDoubleOrNull() ?: 0.0,
                                unitPrice = unitPriceText.toDoubleOrNull() ?: 0.0,
                                designation = designation,
                                basicSalary = basicSalaryText.toDoubleOrNull() ?: 0.0,
                                otHours = otHoursText.toDoubleOrNull() ?: 0.0,
                                otRate = otRateText.toDoubleOrNull() ?: 0.0,
                                otAmount = (otHoursText.toDoubleOrNull() ?: 0.0) * (otRateText.toDoubleOrNull() ?: 0.0),
                                allowances = allowancesText.toDoubleOrNull() ?: 0.0,
                                deductions = deductionsText.toDoubleOrNull() ?: 0.0,
                                netPay = amtVal,
                                propertyLocation = propertyLocation,
                                rentalPeriod = rentalPeriod,
                                receiptNote = receiptNote
                            )

                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AvaGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_transaction_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Sync to Store", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
