package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AvaExpenseRed
import com.example.ui.theme.AvaIncomeGreen
import com.example.ui.theme.AvaWarningAmber
import java.util.Locale

fun formatCurrency(amount: Double): String {
    return String.format(Locale.US, "AED %,.2f", amount)
}

@Composable
fun SyncStatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val isSynced = status.equals("SYNCED", ignoreCase = true)
    val bgColor = if (isSynced) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
    val textColor = if (isSynced) AvaIncomeGreen else AvaWarningAmber
    val icon = if (isSynced) Icons.Default.CheckCircle else Icons.Default.Schedule

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (isSynced) "Synced to store" else "Pending sync",
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isSynced) "STORE SYNCED" else "PENDING SYNC",
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AmountBadge(
    amount: Double,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isPositive) AvaIncomeGreen else AvaExpenseRed
    Text(
        text = "${if (isPositive) "+" else "-"}${formatCurrency(Math.abs(amount))}",
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = modifier
    )
}
