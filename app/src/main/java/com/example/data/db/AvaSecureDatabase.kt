package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountTransaction
import com.example.data.model.AuthUser
import com.example.data.model.CategoryEntity
import com.example.data.model.CompanySettingsEntity
import com.example.data.model.DriverEntity

/**
 * Room database implementation to manage local data for the AvaSecure app.
 * Provides thread-safe singleton access to account, transaction, user, and fleet DAOs.
 */
@Database(
    entities = [
        AccountEntity::class,
        AuthUser::class,
        AccountTransaction::class,
        DriverEntity::class,
        CategoryEntity::class,
        CompanySettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AvaSecureDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun authUserDao(): AuthUserDao
    abstract fun driverDao(): DriverDao
    abstract fun categoryDao(): CategoryDao
    abstract fun companySettingsDao(): CompanySettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AvaSecureDatabase? = null

        const val DATABASE_NAME = "avasecure_local_store.db"

        fun getDatabase(context: Context): AvaSecureDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AvaSecureDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration(false)
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
