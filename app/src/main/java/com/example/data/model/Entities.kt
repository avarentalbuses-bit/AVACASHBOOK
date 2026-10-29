package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_users")
data class AuthUser(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val displayName: String,
    val passwordHash: String,
    val salt: String,
    val pinHash: String? = null,
    val role: String = "ADMIN", // ADMIN, ACCOUNTANT, AUDITOR
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "account_transactions")
data class AccountTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val registerType: String, // BANK, PETTY, INCOME, RENT, SALARY, FUEL
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val description: String = "",
    val category: String = "",
    val subCategory: String = "",
    val cashIn: Double = 0.0,
    val cashOut: Double = 0.0,
    val amount: Double = 0.0,
    val vatAmount: Double = 0.0,
    val totalWithVat: Double = 0.0,
    val reference: String = "", // Chq No / Voucher No / Invoice No
    val payeeOrCustomer: String = "", // Payee, Customer, Landlord, Employee, Driver
    val phone: String = "", // Driver phone / Customer phone
    val busNo: String = "", // Bus Plate e.g. UAQ-214
    val station: String = "", // ADNOC, ENOC, etc.
    val odometer: Double = 0.0,
    val quantityLiters: Double = 0.0,
    val unitPrice: Double = 0.0,
    val designation: String = "",
    val basicSalary: Double = 0.0,
    val otHours: Double = 0.0,
    val otRate: Double = 0.0,
    val otAmount: Double = 0.0,
    val allowances: Double = 0.0,
    val deductions: Double = 0.0,
    val netPay: Double = 0.0,
    val paymentMethod: String = "Bank Transfer", // Bank Transfer, Cash, Cheque
    val propertyLocation: String = "",
    val rentalPeriod: String = "",
    val receiptNote: String = "",
    val syncStatus: String = "SYNCED", // SYNCED, PENDING, FAILED
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val hashSignature: String = ""
)

@Entity(tableName = "drivers")
data class DriverEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val busNo: String = "",
    val seats: Int = 60
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // Income, Expense, Transfer
    val subCategoriesCsv: String = "" // comma-separated list of sub-categories
)

@Entity(tableName = "company_settings")
data class CompanySettingsEntity(
    @PrimaryKey val id: Int = 1,
    val companyName: String = "AVA PASSENGERS TRANSPORT BY RENTAL BUSES LLC",
    val address: String = "P.O. Box 91977, Dubai, UAE",
    val phone: String = "00971 4 3439666",
    val email: String = "Contact@avarental.ae",
    val trn: String = "100364388700003",
    val bank: String = "RAKBANK",
    val iban: String = "AE110400008371527349901",
    val currency: String = "AED",
    val vatRate: Double = 5.0,
    val sheetsUrl: String = "",
    val sheetsSyncEnabled: Boolean = false,
    val quickPinEnabled: Boolean = true,
    val lastStoreSyncTimestamp: Long = 0L,
    val storeChecksum: String = ""
)

data class StoreAuditReport(
    val totalTransactions: Int,
    val bankBalance: Double,
    val pettyBalance: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val pendingSyncCount: Int,
    val integrityVerified: Boolean,
    val checksumHash: String,
    val lastSyncFormatted: String,
    val auditTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "account_entities")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountNumber: String, // e.g. "RAK-01", "CASH-01", "REV-BUS", "EXP-FUEL"
    val accountName: String, // e.g. "RAKBANK Operations Account", "Petty Cash Locker"
    val accountType: String, // BANK, CASH, REVENUE, EXPENSE, ASSET, LIABILITY
    val currency: String = "AED",
    val balance: Double = 0.0,
    val initialBalance: Double = 0.0,
    val bankName: String = "",
    val ibanOrDetails: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val securityTag: String = "", // AvaSecure verification hash / signature
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis()
)
