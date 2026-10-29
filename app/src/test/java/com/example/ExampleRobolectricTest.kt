package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AVA Accounting", appName)
  }

  @Test
  fun `verify password hashing and pin matching`() {
    val salt = com.example.security.SecurityUtils.generateSalt()
    val hash = com.example.security.SecurityUtils.hashWithSalt("ava@2026", salt)
    org.junit.Assert.assertTrue(com.example.security.SecurityUtils.verifyPassword("ava@2026", salt, hash))
    org.junit.Assert.assertFalse(com.example.security.SecurityUtils.verifyPassword("wrongpass", salt, hash))

    val checksum = com.example.security.SecurityUtils.computeTransactionChecksum(
      1L, "BANK", "2026-09-06", 1000.0, 0.0, 1000.0, "Deposit", "REF123"
    )
    org.junit.Assert.assertEquals(16, checksum.length)
  }

  @Test
  fun `verify room database implementation for account entities`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemoryDb = androidx.room.Room.inMemoryDatabaseBuilder(
      context,
      com.example.data.db.AvaSecureDatabase::class.java
    ).allowMainThreadQueries().build()

    val accountDao = inMemoryDb.accountDao()
    val repo = com.example.data.repository.AccountRepository(accountDao)

    // Verify initial count is 0
    assertEquals(0, accountDao.getAccountCount())

    // Insert account entity
    val account = com.example.data.model.AccountEntity(
      accountNumber = "RAK-001",
      accountName = "RAKBANK Operations Account",
      accountType = "BANK",
      currency = "AED",
      balance = 150000.0,
      initialBalance = 150000.0,
      bankName = "RAKBANK",
      ibanOrDetails = "AE110400008371527349901"
    )
    val id = repo.insertAccount(account)
    org.junit.Assert.assertTrue(id > 0)
    assertEquals(1, accountDao.getAccountCount())

    // Fetch and check
    val fetched = repo.getAccountById(id)
    org.junit.Assert.assertNotNull(fetched)
    assertEquals("RAK-001", fetched?.accountNumber)
    assertEquals("RAKBANK Operations Account", fetched?.accountName)
    assertEquals("BANK", fetched?.accountType)
    assertEquals(150000.0, fetched?.balance ?: 0.0, 0.001)
    org.junit.Assert.assertTrue(repo.verifyAccountIntegrity(fetched!!))

    // Update balance
    repo.updateBalance(id, 185000.0)
    val updated = repo.getAccountById(id)
    assertEquals(185000.0, updated?.balance ?: 0.0, 0.001)
    org.junit.Assert.assertTrue(repo.verifyAccountIntegrity(updated!!))

    // Query by account number
    val byNumber = repo.getAccountByNumber("RAK-001")
    org.junit.Assert.assertNotNull(byNumber)
    assertEquals(id, byNumber?.id)

    // Total active balance
    val total = repo.getTotalActiveBalance()
    assertEquals(185000.0, total, 0.001)

    // Delete
    repo.deleteAccountById(id)
    assertEquals(0, accountDao.getAccountCount())

    inMemoryDb.close()
  }
}
