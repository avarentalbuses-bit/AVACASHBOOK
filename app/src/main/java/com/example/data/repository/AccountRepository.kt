package com.example.data.repository

import com.example.data.db.AccountDao
import com.example.data.model.AccountEntity
import com.example.security.SecurityUtils
import kotlinx.coroutines.flow.Flow

/**
 * Repository adhering to the Repository Pattern to abstract Account Room DAO operations
 * for the AvaSecure application.
 */
class AccountRepository(private val accountDao: AccountDao) {

    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccountsFlow()
    val activeAccounts: Flow<List<AccountEntity>> = accountDao.getActiveAccountsFlow()

    fun getAccountsByType(type: String): Flow<List<AccountEntity>> {
        return accountDao.getAccountsByTypeFlow(type)
    }

    fun getAccountByIdFlow(id: Long): Flow<AccountEntity?> {
        return accountDao.getAccountByIdFlow(id)
    }

    suspend fun getAccountById(id: Long): AccountEntity? {
        return accountDao.getAccountById(id)
    }

    suspend fun getAccountByNumber(accountNumber: String): AccountEntity? {
        return accountDao.getAccountByNumber(accountNumber)
    }

    suspend fun getAllAccountsSync(): List<AccountEntity> {
        return accountDao.getAllAccountsSync()
    }

    suspend fun getAccountCount(): Int {
        return accountDao.getAccountCount()
    }

    suspend fun getTotalActiveBalance(): Double {
        return accountDao.getTotalActiveBalance() ?: 0.0
    }

    suspend fun insertAccount(account: AccountEntity): Long {
        val insertedId = accountDao.insertAccount(account)
        val insertedAccount = account.copy(id = insertedId)
        val securedTag = generateSecurityTag(insertedAccount)
        accountDao.updateSecurityTag(insertedId, securedTag)
        return insertedId
    }

    suspend fun insertAccounts(accounts: List<AccountEntity>) {
        accounts.forEach { account ->
            insertAccount(account)
        }
    }

    suspend fun updateAccount(account: AccountEntity) {
        val updatedSecured = account.copy(
            lastModifiedAt = System.currentTimeMillis(),
            securityTag = generateSecurityTag(account)
        )
        accountDao.updateAccount(updatedSecured)
    }

    suspend fun updateBalance(id: Long, newBalance: Double) {
        val account = accountDao.getAccountById(id)
        if (account != null) {
            val updated = account.copy(balance = newBalance, lastModifiedAt = System.currentTimeMillis())
            val tag = generateSecurityTag(updated)
            accountDao.updateAccount(updated.copy(securityTag = tag))
        } else {
            accountDao.updateBalance(id, newBalance)
        }
    }

    suspend fun deleteAccount(account: AccountEntity) {
        accountDao.deleteAccount(account)
    }

    suspend fun deleteAccountById(id: Long) {
        accountDao.deleteAccountById(id)
    }

    suspend fun verifyAccountIntegrity(account: AccountEntity): Boolean {
        val expectedTag = generateSecurityTag(account)
        return account.securityTag == expectedTag
    }

    private fun generateSecurityTag(account: AccountEntity): String {
        return SecurityUtils.computeTransactionChecksum(
            account.id,
            account.accountType,
            account.accountNumber,
            account.balance,
            account.initialBalance,
            0.0,
            account.accountName,
            account.currency
        )
    }
}
