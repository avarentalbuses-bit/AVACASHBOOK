package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AccountTransaction
import com.example.data.model.DriverEntity
import com.example.ui.MainViewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DriversScreen
import com.example.ui.screens.RegistersScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsAndSyncScreen
import com.example.ui.screens.TransactionDialog
import com.example.ui.theme.AvaBorder
import com.example.ui.theme.AvaGoldAccent
import com.example.ui.theme.AvaGreenDark
import com.example.ui.theme.AvaGreenLight
import com.example.ui.theme.AvaGreenPrimary
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AvaAccountingApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvaAccountingApp(viewModel: MainViewModel = viewModel()) {
    val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val drivers by viewModel.allDrivers.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val settings by viewModel.companySettings.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val auditReport by viewModel.auditReport.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentNavDestination by remember { mutableStateOf("dashboard") }
    var currentRegisterTab by remember { mutableStateOf("BANK") }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<AccountTransaction?>(null) }
    var dialogDefaultRegister by remember { mutableStateOf("BANK") }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    if (isLocked || currentUser == null) {
        AuthScreen(
            authError = authError,
            onLoginWithPin = { pin -> viewModel.loginWithPin(pin) },
            onLoginWithPassword = { user, pass -> viewModel.loginWithPassword(user, pass) },
            onRegister = { user, name, pass, pin, role ->
                viewModel.registerUser(user, name, pass, pin, role)
            },
            onClearError = { viewModel.clearAuthError() }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, AvaGoldAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_app_icon),
                                    contentDescription = "AVA Icon",
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AVA ACCOUNTING",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Passengers Transport LLC",
                                    fontSize = 10.sp,
                                    color = AvaGoldAccent
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick Sync Indicator Action
                        IconButton(
                            onClick = { viewModel.syncPendingTransactions() },
                            modifier = Modifier.testTag("topbar_sync_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (pendingSyncCount > 0) {
                                        Badge(containerColor = Color(0xFFFF9800)) {
                                            Text("$pendingSyncCount", fontSize = 9.sp, color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Sync to store",
                                    tint = if (pendingSyncCount == 0) AvaGoldAccent else Color(0xFFFFB74D)
                                )
                            }
                        }

                        // Lock Session Button
                        IconButton(
                            onClick = { viewModel.lockApp() },
                            modifier = Modifier.testTag("lock_app_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Session",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AvaGreenDark
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentNavDestination == "dashboard",
                        onClick = { currentNavDestination = "dashboard" },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AvaGreenDark,
                            selectedTextColor = AvaGreenDark,
                            indicatorColor = AvaGreenLight
                        ),
                        modifier = Modifier.testTag("nav_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentNavDestination == "registers",
                        onClick = { currentNavDestination = "registers" },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Registers") },
                        label = { Text("Registers", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AvaGreenDark,
                            selectedTextColor = AvaGreenDark,
                            indicatorColor = AvaGreenLight
                        ),
                        modifier = Modifier.testTag("nav_registers")
                    )

                    NavigationBarItem(
                        selected = currentNavDestination == "reports",
                        onClick = { currentNavDestination = "reports" },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                        label = { Text("Reports", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AvaGreenDark,
                            selectedTextColor = AvaGreenDark,
                            indicatorColor = AvaGreenLight
                        ),
                        modifier = Modifier.testTag("nav_reports")
                    )

                    NavigationBarItem(
                        selected = currentNavDestination == "drivers",
                        onClick = { currentNavDestination = "drivers" },
                        icon = { Icon(Icons.Default.DirectionsBus, contentDescription = "Drivers") },
                        label = { Text("Drivers", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AvaGreenDark,
                            selectedTextColor = AvaGreenDark,
                            indicatorColor = AvaGreenLight
                        ),
                        modifier = Modifier.testTag("nav_drivers")
                    )

                    NavigationBarItem(
                        selected = currentNavDestination == "settings",
                        onClick = { currentNavDestination = "settings" },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AvaGreenDark,
                            selectedTextColor = AvaGreenDark,
                            indicatorColor = AvaGreenLight
                        ),
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingTransaction = null
                        dialogDefaultRegister = if (currentNavDestination == "registers") currentRegisterTab else "BANK"
                        showTransactionDialog = true
                    },
                    containerColor = AvaGreenPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("global_fab_add")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentNavDestination) {
                    "dashboard" -> {
                        DashboardScreen(
                            transactions = transactions,
                            auditReport = auditReport,
                            pendingSyncCount = pendingSyncCount,
                            isSyncing = isSyncing,
                            onAddTransactionClick = {
                                editingTransaction = null
                                dialogDefaultRegister = "BANK"
                                showTransactionDialog = true
                            },
                            onSyncStoreClick = {
                                viewModel.syncPendingTransactions()
                            },
                            onSelectTransaction = { tx ->
                                editingTransaction = tx
                                dialogDefaultRegister = tx.registerType
                                showTransactionDialog = true
                            },
                            onNavigateToRegister = { reg ->
                                currentRegisterTab = reg
                                currentNavDestination = "registers"
                            }
                        )
                    }

                    "registers" -> {
                        RegistersScreen(
                            currentRegister = currentRegisterTab,
                            onRegisterChange = { currentRegisterTab = it },
                            transactions = transactions,
                            onAddClick = {
                                editingTransaction = null
                                dialogDefaultRegister = currentRegisterTab
                                showTransactionDialog = true
                            },
                            onEditClick = { tx ->
                                editingTransaction = tx
                                dialogDefaultRegister = tx.registerType
                                showTransactionDialog = true
                            },
                            onDeleteClick = { tx ->
                                viewModel.deleteTransaction(tx)
                            }
                        )
                    }

                    "reports" -> {
                        ReportsScreen(transactions = transactions)
                    }

                    "drivers" -> {
                        DriversScreen(
                            drivers = drivers,
                            onSaveDriver = { d -> viewModel.saveDriver(d) },
                            onDeleteDriver = { d -> viewModel.deleteDriver(d) },
                            onAddFuelForDriver = { d ->
                                editingTransaction = null
                                dialogDefaultRegister = "FUEL"
                                showTransactionDialog = true
                            }
                        )
                    }

                    "settings" -> {
                        SettingsAndSyncScreen(
                            currentUser = currentUser,
                            settings = settings,
                            auditReport = auditReport,
                            isSyncing = isSyncing,
                            onSaveSettings = { s -> viewModel.saveCompanySettings(s) },
                            onSyncStoreNow = { viewModel.syncPendingTransactions() },
                            onVerifyStoreIntegrity = { viewModel.refreshAudit() },
                            onExportStoreJson = { cb -> viewModel.exportStoreJson(cb) },
                            onLockApp = { viewModel.lockApp() },
                            onLogout = { viewModel.logout() },
                            onUpdatePin = { pin ->
                                currentUser?.let { u ->
                                    scope.launch {
                                        viewModel.repository.updatePin(u.id, pin)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // New/Edit Transaction Modal Dialog
    if (showTransactionDialog) {
        TransactionDialog(
            initialTransaction = editingTransaction,
            defaultRegister = dialogDefaultRegister,
            drivers = drivers,
            categories = categories,
            onDismiss = { showTransactionDialog = false },
            onSave = { tx ->
                viewModel.saveTransaction(tx)
                showTransactionDialog = false
            }
        )
    }
}
