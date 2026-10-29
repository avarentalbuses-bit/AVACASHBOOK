package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountEntity
import com.example.data.model.AccountTransaction
import com.example.data.model.AuthUser
import com.example.data.model.CategoryEntity
import com.example.data.model.CompanySettingsEntity
import com.example.data.model.DriverEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM account_transactions ORDER BY date DESC, timestamp DESC, id DESC")
    fun getAllTransactions(): Flow<List<AccountTransaction>>

    @Query("SELECT * FROM account_transactions WHERE registerType = :type ORDER BY date DESC, timestamp DESC, id DESC")
    fun getTransactionsByType(type: String): Flow<List<AccountTransaction>>

    @Query("SELECT * FROM account_transactions WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSyncTransactions(): List<AccountTransaction>

    @Query("SELECT COUNT(*) FROM account_transactions WHERE syncStatus = 'PENDING'")
    fun getPendingSyncCountFlow(): Flow<Int>

    @Query("SELECT * FROM account_transactions ORDER BY date ASC, timestamp ASC, id ASC")
    suspend fun getAllTransactionsSync(): List<AccountTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: AccountTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<AccountTransaction>)

    @Update
    suspend fun updateTransaction(transaction: AccountTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: AccountTransaction)

    @Query("DELETE FROM account_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM account_transactions")
    suspend fun deleteAll()

    @Query("UPDATE account_transactions SET syncStatus = 'SYNCED', lastSyncedAt = :syncedAt WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>, syncedAt: Long = System.currentTimeMillis())
}

@Dao
interface AuthUserDao {
    @Query("SELECT * FROM auth_users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): AuthUser?

    @Query("SELECT * FROM auth_users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): AuthUser?

    @Query("SELECT COUNT(*) FROM auth_users")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM auth_users ORDER BY id ASC")
    fun getAllUsersFlow(): Flow<List<AuthUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AuthUser): Long

    @Update
    suspend fun updateUser(user: AuthUser)
}

@Dao
interface DriverDao {
    @Query("SELECT * FROM drivers ORDER BY name ASC")
    fun getAllDriversFlow(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers ORDER BY name ASC")
    suspend fun getAllDriversSync(): List<DriverEntity>

    @Query("SELECT COUNT(*) FROM drivers")
    suspend fun getDriverCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrivers(drivers: List<DriverEntity>)

    @Update
    suspend fun updateDriver(driver: DriverEntity)

    @Delete
    suspend fun deleteDriver(driver: DriverEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}

@Dao
interface CompanySettingsDao {
    @Query("SELECT * FROM company_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<CompanySettingsEntity?>

    @Query("SELECT * FROM company_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): CompanySettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: CompanySettingsEntity)
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM account_entities ORDER BY accountName ASC")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM account_entities WHERE isActive = 1 ORDER BY accountName ASC")
    fun getActiveAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM account_entities WHERE accountType = :accountType AND isActive = 1 ORDER BY accountName ASC")
    fun getAccountsByTypeFlow(accountType: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM account_entities WHERE id = :id LIMIT 1")
    fun getAccountByIdFlow(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM account_entities WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("SELECT * FROM account_entities WHERE accountNumber = :accountNumber LIMIT 1")
    suspend fun getAccountByNumber(accountNumber: String): AccountEntity?

    @Query("SELECT * FROM account_entities ORDER BY id ASC")
    suspend fun getAllAccountsSync(): List<AccountEntity>

    @Query("SELECT COUNT(*) FROM account_entities")
    suspend fun getAccountCount(): Int

    @Query("SELECT SUM(balance) FROM account_entities WHERE isActive = 1")
    suspend fun getTotalActiveBalance(): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE account_entities SET balance = :newBalance, lastModifiedAt = :timestamp WHERE id = :id")
    suspend fun updateBalance(id: Long, newBalance: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE account_entities SET securityTag = :newTag, lastModifiedAt = :timestamp WHERE id = :id")
    suspend fun updateSecurityTag(id: Long, newTag: String, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM account_entities WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    @Query("DELETE FROM account_entities")
    suspend fun deleteAllAccounts()
}
