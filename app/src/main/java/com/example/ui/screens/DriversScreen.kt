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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.DriverEntity
import com.example.ui.theme.AvaBorder
import com.example.ui.theme.AvaCardBg
import com.example.ui.theme.AvaExpenseRed
import com.example.ui.theme.AvaGoldAccent
import com.example.ui.theme.AvaGreenDark
import com.example.ui.theme.AvaGreenLight
import com.example.ui.theme.AvaGreenPrimary

@Composable
fun DriversScreen(
    drivers: List<DriverEntity>,
    onSaveDriver: (DriverEntity) -> Unit,
    onDeleteDriver: (DriverEntity) -> Unit,
    onAddFuelForDriver: (DriverEntity) -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var editingDriver by remember { mutableStateOf<DriverEntity?>(null) }
    var driverToDelete by remember { mutableStateOf<DriverEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F6F4))
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Fleet Drivers Roster",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AvaGreenDark
                )
                Text(
                    text = "${drivers.size} Assigned Bus Drivers",
                    fontSize = 12.sp,
                    color = Color(0xFF5A6B5F)
                )
            }

            Button(
                onClick = {
                    editingDriver = null
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = AvaGreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Driver", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(drivers, key = { it.id }) { driver ->
                DriverCard(
                    driver = driver,
                    onEdit = {
                        editingDriver = driver
                        showDialog = true
                    },
                    onDelete = {
                        driverToDelete = driver
                    },
                    onWhatsApp = {
                        if (driver.phone.isNotEmpty()) {
                            openWhatsAppChat(context, driver.phone, "Hello ${driver.name}, instructions from AVA Transport office.")
                        }
                    },
                    onAddFuel = {
                        onAddFuelForDriver(driver)
                    }
                )
            }
        }
    }

    // Add / Edit Driver Dialog
    if (showDialog) {
        DriverEditDialog(
            driver = editingDriver,
            onDismiss = { showDialog = false },
            onSave = { saved ->
                onSaveDriver(saved)
                showDialog = false
            }
        )
    }

    // Delete confirmation
    driverToDelete?.let { d ->
        AlertDialog(
            onDismissRequest = { driverToDelete = null },
            title = { Text("Delete Driver") },
            text = { Text("Remove ${d.name} from company fleet roster?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDriver(d)
                        driverToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AvaExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { driverToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DriverCard(
    driver: DriverEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onWhatsApp: () -> Unit,
    onAddFuel: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AvaCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AvaBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AvaGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = AvaGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = driver.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AvaGreenDark
                    )
                    Text(
                        text = "Bus: ${driver.busNo.ifEmpty { "Unassigned" }} (${driver.seats} Seats)",
                        fontSize = 12.sp,
                        color = Color(0xFF455A64),
                        fontWeight = FontWeight.Medium
                    )
                    if (driver.phone.isNotEmpty()) {
                        Text(
                            text = driver.phone,
                            fontSize = 11.sp,
                            color = Color(0xFF78909C)
                        )
                    }
                }
            }

            // Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (driver.phone.isNotEmpty()) {
                    IconButton(
                        onClick = onWhatsApp,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onAddFuel,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = "Add Fuel",
                        tint = AvaGoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp)
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
                    modifier = Modifier.size(34.dp)
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

@Composable
fun DriverEditDialog(
    driver: DriverEntity?,
    onDismiss: () -> Unit,
    onSave: (DriverEntity) -> Unit
) {
    var name by remember { mutableStateOf(driver?.name ?: "") }
    var phone by remember { mutableStateOf(driver?.phone ?: "") }
    var busNo by remember { mutableStateOf(driver?.busNo ?: "") }
    var seatsText by remember { mutableStateOf(driver?.seats?.toString() ?: "60") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (driver == null) "Add Driver" else "Edit Driver") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Driver Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (+971...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = busNo,
                    onValueChange = { busNo = it },
                    label = { Text("Assigned Bus Plate (e.g. UAQ-214)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = seatsText,
                    onValueChange = { seatsText = it },
                    label = { Text("Bus Seating Capacity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val d = (driver ?: DriverEntity(name = name)).copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            busNo = busNo.trim(),
                            seats = seatsText.toIntOrNull() ?: 60
                        )
                        onSave(d)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AvaGreenPrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun openWhatsAppChat(context: Context, phone: String, message: String) {
    val clean = phone.replace(Regex("[^0-9]"), "")
    val url = "https://api.whatsapp.com/send?phone=$clean&text=${Uri.encode(message)}"
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {}
}
