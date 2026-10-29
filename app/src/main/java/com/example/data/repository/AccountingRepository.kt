package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountTransaction
import com.example.data.model.AuthUser
import com.example.data.model.CategoryEntity
import com.example.data.model.CompanySettingsEntity
import com.example.data.model.DriverEntity
import com.example.data.model.StoreAuditReport
import com.example.security.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AccountingRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    val accountDao = database.accountDao()
    private val transactionDao = database.transactionDao()
    private val authUserDao = database.authUserDao()
    private val driverDao = database.driverDao()
    private val categoryDao = database.categoryDao()
    private val companySettingsDao = database.companySettingsDao()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccountsFlow()
    val allTransactions: Flow<List<AccountTransaction>> = transactionDao.getAllTransactions()
    val allDrivers: Flow<List<DriverEntity>> = driverDao.getAllDriversFlow()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()
    val companySettings: Flow<CompanySettingsEntity?> = companySettingsDao.getSettingsFlow()
    val pendingSyncCount: Flow<Int> = transactionDao.getPendingSyncCountFlow()

    suspend fun initializeDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        // 1. Check Auth User
        if (authUserDao.getUserCount() == 0) {
            val adminSalt = SecurityUtils.generateSalt()
            val adminPassHash = SecurityUtils.hashWithSalt("ava@2026", adminSalt)
            val adminPinHash = SecurityUtils.hashWithSalt("1234", adminSalt)

            authUserDao.insertUser(
                AuthUser(
                    username = "admin",
                    displayName = "Finance Controller (AVA)",
                    passwordHash = adminPassHash,
                    salt = adminSalt,
                    pinHash = adminPinHash,
                    role = "ADMIN"
                )
            )

            // Also seed an accountant user
            val accSalt = SecurityUtils.generateSalt()
            authUserDao.insertUser(
                AuthUser(
                    username = "accountant",
                    displayName = "Senior Bus Fleet Accountant",
                    passwordHash = SecurityUtils.hashWithSalt("bus@2026", accSalt),
                    salt = accSalt,
                    pinHash = SecurityUtils.hashWithSalt("5678", accSalt),
                    role = "ACCOUNTANT"
                )
            )
        }

        // 2. Check Company Settings
        if (companySettingsDao.getSettingsSync() == null) {
            companySettingsDao.saveSettings(
                CompanySettingsEntity(
                    id = 1,
                    companyName = "AVA PASSENGERS TRANSPORT BY RENTAL BUSES LLC",
                    address = "P.O. Box 91977, Dubai, UAE",
                    phone = "00971 4 3439666",
                    email = "Contact@avarental.ae",
                    trn = "100364388700003",
                    bank = "RAKBANK",
                    iban = "AE110400008371527349901",
                    currency = "AED",
                    vatRate = 5.0,
                    sheetsUrl = "",
                    sheetsSyncEnabled = false
                )
            )
        }

        // 3. Check Drivers
        if (driverDao.getDriverCount() == 0) {
            val drivers = listOf(
                DriverEntity(name = "Shahzeeb", phone = "+971568556938", busNo = "UAQ-214", seats = 60),
                DriverEntity(name = "Haseeb", phone = "+971528360809", busNo = "AJM-214", seats = 60),
                DriverEntity(name = "Kamar Khan", phone = "+971502665742", busNo = "SHJ-214", seats = 60),
                DriverEntity(name = "SHAKIR ULLAH", phone = "+971501123456", busNo = "SHJ-220", seats = 60),
                DriverEntity(name = "ILYAS BACHA", phone = "+971559876543", busNo = "SHJ-213", seats = 60),
                DriverEntity(name = "MOHAMMED", phone = "+971543210987", busNo = "SHJ-218", seats = 60),
                DriverEntity(name = "KAMRAN KHAN", phone = "+971567890123", busNo = "SHJ-220-218", seats = 60),
                DriverEntity(name = "BASHEER", phone = "+971523456789", busNo = "Jabal Ali Shuttle", seats = 13),
                DriverEntity(name = "SABIR", phone = "+971508765432", busNo = "SHJ-225", seats = 60),
                DriverEntity(name = "SHARIF", phone = "+971556781234", busNo = "DXB-501", seats = 60),
                DriverEntity(name = "SALAM", phone = "+971509871234", busNo = "DXB-502", seats = 60),
                DriverEntity(name = "ABDUL GHAFOOR", phone = "+971554321098", busNo = "SHJ-228", seats = 60),
                DriverEntity(name = "NISAR", phone = "+971521122334", busNo = "AJM-310", seats = 60),
                DriverEntity(name = "MUHAMMED ABBAS", phone = "+971549988776", busNo = "UAQ-305", seats = 60),
                DriverEntity(name = "FAZAL HUSSAIN", phone = "+971503344556", busNo = "DXB-610", seats = 60)
            )
            driverDao.insertDrivers(drivers)
        }

        // 4. Check Categories
        if (categoryDao.getCategoryCount() == 0) {
            val categories = listOf(
                CategoryEntity(
                    name = "Income",
                    type = "Income",
                    subCategoriesCsv = "Transport Revenue, Rental Income, Corporate Contracts, School Transport, Other Income"
                ),
                CategoryEntity(
                    name = "Expense",
                    type = "Expense",
                    subCategoriesCsv = "Fuel, Vehicle Maintenance, Spare Parts, Tyres, Salary & Wages, Rent, Utilities, Food & Refreshments, Office Expenses, Bank Charges, Insurance, VAT, Other Expenses"
                ),
                CategoryEntity(
                    name = "Transfer",
                    type = "Transfer",
                    subCategoriesCsv = "Bank to Cash, Cash to Bank, Between Accounts"
                )
            )
            categoryDao.insertCategories(categories)
        }

        // 5. Seed initial baseline transactions for AVA Passengers Transport LLC
        val currentTx = transactionDao.getAllTransactionsSync()
        if (currentTx.isEmpty()) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val seedTransactions = listOf(
                AccountTransaction(
                    registerType = "BANK",
                    date = todayStr,
                    description = "RAKBANK Opening Deposit - Corporate Client Fleet Payment",
                    category = "Income",
                    subCategory = "Transport Revenue",
                    cashIn = 145000.0,
                    cashOut = 0.0,
                    amount = 145000.0,
                    reference = "RAK-FT-992140",
                    payeeOrCustomer = "Al Habtoor Contracting LLC",
                    syncStatus = "SYNCED",
                    hashSignature = "AVA-INIT-001"
                ),
                AccountTransaction(
                    registerType = "BANK",
                    date = todayStr,
                    description = "Monthly Bus Fleet Yard Lease - Al Qusais Depot",
                    category = "Expense",
                    subCategory = "Rent",
                    cashIn = 0.0,
                    cashOut = 28500.0,
                    amount = 28500.0,
                    reference = "CHQ-883102",
                    payeeOrCustomer = "Dubai Real Estate Properties",
                    propertyLocation = "Al Qusais Industrial Area 3",
                    rentalPeriod = "Current Month",
                    syncStatus = "SYNCED",
                    hashSignature = "AVA-INIT-002"
                ),
                AccountTransaction(
                    registerType = "PETTY",
                    date = todayStr,
                    description = "Withdrawal from RAKBANK for Cash float",
                    category = "Transfer",
                    subCategory = "Bank to Cash",
                    cashIn = 12000.0,
                    cashOut = 0.0,
                    amount = 12000.0,
                    reference = "VCH-001",
                    payeeOrCustomer = "Cashier Office",
                    syncStatus = "SYNCED",
                    hashSignature = "AVA-INIT-003"
                ),
                AccountTransaction(
                    registerType = "FUEL",
                    date = todayStr,
                    description = "Diesel filling for 60-seat bus route SHJ-214",
                    category = "Expense",
                    subCategory = "Fuel",
                    cashIn = 0.0,
                    cashOut = 840.0,
                    amount = 840.0,
                    reference = "ADNOC-99412",
                    payeeOrCustomer = "Kamar Khan",
                    busNo = "SHJ-214",
                    station = "ADNOC",
                    odometer = 184520.0,
                    quantityLiters = 262.5,
                    unitPrice = 3.20,
                    syncStatus = "SYNCED",
                    hashSignature = "AVA-INIT-004"
                ),
                AccountTransaction(
                    registerType = "SALARY",
                    date = todayStr,
                    description = "Driver monthly payroll & overtime settlement",
                    category = "Expense",
                    subCategory = "Salary & Wages",
                    cashIn = 0.0,
                    cashOut = 4350.0,
                    amount = 4350.0,
                    reference = "WPS-PAY-7712",
                    payeeOrCustomer = "Shahzeeb",
                    designation = "Senior Heavy Bus Driver",
                    basicSalary = 3500.0,
                    otHours = 34.0,
                    otRate = 25.0,
                    otAmount = 850.0,
                    allowances = 0.0,
                    deductions = 0.0,
                    netPay = 4350.0,
                    paymentMethod = "Bank Transfer",
                    syncStatus = "SYNCED",
                    hashSignature = "AVA-INIT-005"
                ),
                AccountTransaction(
                    registerType = "INCOME",
                    date = todayStr,
                    description = "Camp-to-Site Shuttle Services Contract Inv #8839",
                    category = "Income",
                    subCategory = "Transport Revenue",
                    cashIn = 38500.0,
                    cashOut = 0.0,
                    amount = 36666.67,
                    vatAmount = 1833.33,
                    totalWithVat = 38500.0,
                    reference = "INV-8839",
                    payeeOrCustomer = "ALICO Engineering LLC",
                    syncStatus = "SYNCED",
                    hashSignature = "AVA-INIT-006"
                )
            )

            seedTransactions.forEach { tx ->
                val id = transactionDao.insertTransaction(tx)
                val checksum = SecurityUtils.computeTransactionChecksum(
                    id = id,
                    registerType = tx.registerType,
                    date = tx.date,
                    cashIn = tx.cashIn,
                    cashOut = tx.cashOut,
                    amount = tx.amount,
                    description = tx.description,
                    reference = tx.reference
                )
                transactionDao.updateTransaction(tx.copy(id = id, hashSignature = checksum))
            }
        }

        // 6. Seed baseline AvaSecure accounts
        if (accountDao.getAccountCount() == 0) {
            val defaultAccounts = listOf(
                AccountEntity(
                    accountNumber = "RAK-001",
                    accountName = "RAKBANK Main Operations Account",
                    accountType = "BANK",
                    currency = "AED",
                    balance = 125000.0,
                    initialBalance = 125000.0,
                    bankName = "RAKBANK",
                    ibanOrDetails = "AE110400008371527349901",
                    description = "Primary corporate current account for passenger transport contracts and operations",
                    isActive = true,
                    securityTag = "AVA-SEC-RAK01"
                ),
                AccountEntity(
                    accountNumber = "PC-001",
                    accountName = "Main Yard Petty Cash Float",
                    accountType = "CASH",
                    currency = "AED",
                    balance = 15000.0,
                    initialBalance = 15000.0,
                    bankName = "Cash Locker",
                    ibanOrDetails = "Depot Vault 1",
                    description = "Petty cash float for rapid driver supplies, toll gates, and emergency road permits",
                    isActive = true,
                    securityTag = "AVA-SEC-PC01"
                ),
                AccountEntity(
                    accountNumber = "REV-100",
                    accountName = "Passenger Transport Contract Revenue",
                    accountType = "REVENUE",
                    currency = "AED",
                    balance = 183500.0,
                    initialBalance = 0.0,
                    bankName = "",
                    ibanOrDetails = "5% VAT Applicable",
                    description = "Corporate staff transport, school bus runs, and private luxury charters",
                    isActive = true,
                    securityTag = "AVA-SEC-REV01"
                ),
                AccountEntity(
                    accountNumber = "EXP-FUEL",
                    accountName = "Fleet Diesel & Fuel Expenses",
                    accountType = "EXPENSE",
                    currency = "AED",
                    balance = 52400.0,
                    initialBalance = 0.0,
                    bankName = "",
                    ibanOrDetails = "ADNOC / ENOC / EPPCO",
                    description = "Bulk and on-road diesel fuel expenses for 60-seat and 30-seat passenger fleet",
                    isActive = true,
                    securityTag = "AVA-SEC-FUEL01"
                ),
                AccountEntity(
                    accountNumber = "EXP-WPS",
                    accountName = "Drivers & Staff Payroll WPS",
                    accountType = "EXPENSE",
                    currency = "AED",
                    balance = 68000.0,
                    initialBalance = 0.0,
                    bankName = "RAKBANK WPS",
                    ibanOrDetails = "MOL WPS SIF Registry",
                    description = "Heavy bus drivers, conductors, mechanics, and administrative staff compensation",
                    isActive = true,
                    securityTag = "AVA-SEC-WPS01"
                ),
                AccountEntity(
                    accountNumber = "EXP-RENT",
                    accountName = "Bus Fleet Depot & Yard Lease",
                    accountType = "EXPENSE",
                    currency = "AED",
                    balance = 28500.0,
                    initialBalance = 0.0,
                    bankName = "Cheque Clearing",
                    ibanOrDetails = "Al Qusais Industrial Depot",
                    description = "Vehicle depot parking, maintenance bay lease, and staff accommodation",
                    isActive = true,
                    securityTag = "AVA-SEC-RENT01"
                )
            )
            accountDao.insertAccounts(defaultAccounts)
        }
    }

    // ==================== AUTHENTICATION ====================

    suspend fun authenticateUser(username: String, password: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        val user = authUserDao.getUserByUsername(username.trim())
            ?: return@withContext Result.failure(Exception("Invalid username or password"))

        if (SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)) {
            val updated = user.copy(lastLoginAt = System.currentTimeMillis())
            authUserDao.updateUser(updated)
            Result.success(updated)
        } else {
            Result.failure(Exception("Invalid username or password"))
        }
    }

    suspend fun authenticateWithPin(pin: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        val allUsers = authUserDao.getAllUsersFlow().firstOrNull() ?: emptyList()
        val trimmedPin = pin.trim()
        val matchedUser = allUsers.firstOrNull { user ->
            user.pinHash != null && SecurityUtils.verifyPassword(trimmedPin, user.salt, user.pinHash)
        }

        if (matchedUser != null) {
            val updated = matchedUser.copy(lastLoginAt = System.currentTimeMillis())
            authUserDao.updateUser(updated)
            Result.success(updated)
        } else {
            Result.failure(Exception("Incorrect security PIN"))
        }
    }

    suspend fun updatePin(userId: Long, newPin: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = authUserDao.getUserById(userId)
            ?: return@withContext Result.failure(Exception("User not found"))
        val pinHash = SecurityUtils.hashWithSalt(newPin.trim(), user.salt)
        authUserDao.updateUser(user.copy(pinHash = pinHash))
        Result.success(Unit)
    }

    suspend fun changePassword(userId: Long, currentPass: String, newPass: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = authUserDao.getUserById(userId)
            ?: return@withContext Result.failure(Exception("User not found"))
        if (!SecurityUtils.verifyPassword(currentPass, user.salt, user.passwordHash)) {
            return@withContext Result.failure(Exception("Current password is incorrect"))
        }
        val newHash = SecurityUtils.hashWithSalt(newPass, user.salt)
        authUserDao.updateUser(user.copy(passwordHash = newHash))
        Result.success(Unit)
    }

    suspend fun registerUser(
        username: String,
        displayName: String,
        password: String,
        pin: String,
        role: String
    ): Result<AuthUser> = withContext(Dispatchers.IO) {
        val existing = authUserDao.getUserByUsername(username.trim())
        if (existing != null) {
            return@withContext Result.failure(Exception("Username already exists"))
        }
        val salt = SecurityUtils.generateSalt()
        val passHash = SecurityUtils.hashWithSalt(password, salt)
        val pinHash = if (pin.isNotBlank()) SecurityUtils.hashWithSalt(pin.trim(), salt) else null

        val newUser = AuthUser(
            username = username.trim(),
            displayName = displayName.trim(),
            passwordHash = passHash,
            salt = salt,
            pinHash = pinHash,
            role = role
        )
        val id = authUserDao.insertUser(newUser)
        Result.success(newUser.copy(id = id))
    }

    // ==================== STORE PERSISTENCE & SYNC ====================

    suspend fun saveTransaction(transaction: AccountTransaction): Long = withContext(Dispatchers.IO) {
        val isNew = transaction.id == 0L
        val txId = if (isNew) {
            transactionDao.insertTransaction(transaction.copy(syncStatus = "PENDING"))
        } else {
            transaction.id
        }

        // Calculate cryptographic checksum to guarantee tamper resistance & accurate store sync
        val checksum = SecurityUtils.computeTransactionChecksum(
            id = txId,
            registerType = transaction.registerType,
            date = transaction.date,
            cashIn = transaction.cashIn,
            cashOut = transaction.cashOut,
            amount = transaction.amount,
            description = transaction.description,
            reference = transaction.reference
        )

        val finalTx = transaction.copy(
            id = txId,
            hashSignature = checksum,
            syncStatus = "PENDING"
        )
        transactionDao.updateTransaction(finalTx)

        // Attempt immediate cloud store / sheets sync if configured
        val settings = companySettingsDao.getSettingsSync()
        if (settings != null && settings.sheetsSyncEnabled && settings.sheetsUrl.isNotBlank()) {
            val syncSuccess = postToSheetsEndpoint(settings.sheetsUrl, finalTx, if (isNew) "add" else "update")
            if (syncSuccess) {
                transactionDao.markAsSynced(listOf(txId))
            }
        }

        txId
    }

    suspend fun deleteTransaction(transaction: AccountTransaction) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
        val settings = companySettingsDao.getSettingsSync()
        if (settings != null && settings.sheetsSyncEnabled && settings.sheetsUrl.isNotBlank()) {
            postToSheetsEndpoint(settings.sheetsUrl, transaction, "delete")
        }
    }

    suspend fun syncAllPendingTransactions(): Result<Int> = withContext(Dispatchers.IO) {
        val pending = transactionDao.getPendingSyncTransactions()
        if (pending.isEmpty()) return@withContext Result.success(0)

        val settings = companySettingsDao.getSettingsSync()
        val url = settings?.sheetsUrl ?: ""

        var syncedCount = 0
        val syncedIds = mutableListOf<Long>()

        if (url.isNotBlank()) {
            for (tx in pending) {
                val ok = postToSheetsEndpoint(url, tx, "sync")
                if (ok) {
                    syncedIds.add(tx.id)
                    syncedCount++
                }
            }
        } else {
            // Local store verification only: mark items as locally verified in Room
            syncedIds.addAll(pending.map { it.id })
            syncedCount = pending.size
        }

        if (syncedIds.isNotEmpty()) {
            transactionDao.markAsSynced(syncedIds)
            val now = System.currentTimeMillis()
            if (settings != null) {
                companySettingsDao.saveSettings(settings.copy(lastStoreSyncTimestamp = now))
            }
        }

        Result.success(syncedCount)
    }

    suspend fun verifyStoreIntegrity(): StoreAuditReport = withContext(Dispatchers.IO) {
        val all = transactionDao.getAllTransactionsSync()
        var bankBal = 0.0
        var pettyBal = 0.0
        var incomeTotal = 0.0
        var expenseTotal = 0.0
        var pendingCount = 0
        var allSignaturesValid = true

        for (tx in all) {
            if (tx.syncStatus == "PENDING") pendingCount++

            when (tx.registerType) {
                "BANK" -> bankBal += (tx.cashIn - tx.cashOut)
                "PETTY" -> pettyBal += (tx.cashIn - tx.cashOut)
                "INCOME" -> incomeTotal += tx.amount
                "RENT", "SALARY", "FUEL" -> expenseTotal += tx.amount
            }

            // Verify checksum
            val expectedChecksum = SecurityUtils.computeTransactionChecksum(
                id = tx.id,
                registerType = tx.registerType,
                date = tx.date,
                cashIn = tx.cashIn,
                cashOut = tx.cashOut,
                amount = tx.amount,
                description = tx.description,
                reference = tx.reference
            )
            if (tx.hashSignature.isNotEmpty() && tx.hashSignature != expectedChecksum) {
                allSignaturesValid = false
            }
        }

        val settings = companySettingsDao.getSettingsSync()
        val lastSyncStr = if (settings != null && settings.lastStoreSyncTimestamp > 0) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date(settings.lastStoreSyncTimestamp))
        } else {
            "Verified locally in Room database"
        }

        val overallChecksum = SecurityUtils.computeTransactionChecksum(
            id = all.size.toLong(),
            registerType = "ALL_STORE",
            date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
            cashIn = bankBal,
            cashOut = pettyBal,
            amount = incomeTotal,
            description = "AVA Store Audit",
            reference = "COUNT:${all.size}"
        )

        StoreAuditReport(
            totalTransactions = all.size,
            bankBalance = bankBal,
            pettyBalance = pettyBal,
            totalIncome = incomeTotal,
            totalExpense = expenseTotal,
            pendingSyncCount = pendingCount,
            integrityVerified = allSignaturesValid,
            checksumHash = overallChecksum,
            lastSyncFormatted = lastSyncStr
        )
    }

    suspend fun exportStoreToJson(): String = withContext(Dispatchers.IO) {
        val txs = transactionDao.getAllTransactionsSync()
        val drivers = driverDao.getAllDriversSync()
        val settings = companySettingsDao.getSettingsSync()

        val root = JSONObject()
        root.put("app", "AVA Accounting Pro")
        root.put("company", settings?.companyName ?: "AVA PASSENGERS TRANSPORT")
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

        val txArray = JSONArray()
        txs.forEach { tx ->
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("registerType", tx.registerType)
            obj.put("date", tx.date)
            obj.put("description", tx.description)
            obj.put("category", tx.category)
            obj.put("subCategory", tx.subCategory)
            obj.put("cashIn", tx.cashIn)
            obj.put("cashOut", tx.cashOut)
            obj.put("amount", tx.amount)
            obj.put("vatAmount", tx.vatAmount)
            obj.put("totalWithVat", tx.totalWithVat)
            obj.put("reference", tx.reference)
            obj.put("payeeOrCustomer", tx.payeeOrCustomer)
            obj.put("busNo", tx.busNo)
            obj.put("syncStatus", tx.syncStatus)
            obj.put("hashSignature", tx.hashSignature)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val driverArray = JSONArray()
        drivers.forEach { d ->
            val dobj = JSONObject()
            dobj.put("name", d.name)
            dobj.put("phone", d.phone)
            dobj.put("busNo", d.busNo)
            dobj.put("seats", d.seats)
            driverArray.put(dobj)
        }
        root.put("drivers", driverArray)

        root.toString(2)
    }

    suspend fun saveCompanySettings(settings: CompanySettingsEntity) = withContext(Dispatchers.IO) {
        companySettingsDao.saveSettings(settings)
    }

    suspend fun saveDriver(driver: DriverEntity): Long = withContext(Dispatchers.IO) {
        if (driver.id == 0L) driverDao.insertDriver(driver)
        else {
            driverDao.updateDriver(driver)
            driver.id
        }
    }

    suspend fun deleteDriver(driver: DriverEntity) = withContext(Dispatchers.IO) {
        driverDao.deleteDriver(driver)
    }

    suspend fun saveCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        if (category.id == 0L) categoryDao.insertCategory(category)
        else {
            categoryDao.updateCategory(category)
            category.id
        }
    }

    suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategory(category)
    }

    private fun postToSheetsEndpoint(url: String, tx: AccountTransaction, action: String): Boolean {
        try {
            val json = JSONObject()
            json.put("type", tx.registerType.lowercase(Locale.ROOT))
            json.put("action", action)
            json.put("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))

            val data = JSONObject()
            data.put("id", tx.id)
            data.put("date", tx.date)
            data.put("description", tx.description)
            data.put("category", tx.category)
            data.put("subCategory", tx.subCategory)
            data.put("cashIn", tx.cashIn)
            data.put("cashOut", tx.cashOut)
            data.put("amount", tx.amount)
            data.put("reference", tx.reference)
            data.put("payee", tx.payeeOrCustomer)
            data.put("busNo", tx.busNo)
            data.put("checksum", tx.hashSignature)
            json.put("data", data)

            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            return response.isSuccessful
        } catch (_: Exception) {
            return false
        }
    }
}
