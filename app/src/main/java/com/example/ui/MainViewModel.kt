package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccountTransaction
import com.example.data.model.AuthUser
import com.example.data.model.CategoryEntity
import com.example.data.model.CompanySettingsEntity
import com.example.data.model.DriverEntity
import com.example.data.model.StoreAuditReport
import com.example.data.repository.AccountingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AccountingRepository(application.applicationContext)

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _auditReport = MutableStateFlow<StoreAuditReport?>(null)
    val auditReport: StateFlow<StoreAuditReport?> = _auditReport.asStateFlow()

    val allTransactions: StateFlow<List<AccountTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDrivers: StateFlow<List<DriverEntity>> = repository.allDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val companySettings: StateFlow<CompanySettingsEntity?> = repository.companySettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pendingSyncCount: StateFlow<Int> = repository.pendingSyncCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
            refreshAudit()
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun loginWithPassword(username: String, pass: String) {
        viewModelScope.launch {
            _authError.value = null
            val result = repository.authenticateUser(username, pass)
            result.onSuccess { user ->
                _currentUser.value = user
                _isLocked.value = false
                _feedbackMessage.value = "Welcome back, ${user.displayName}"
                refreshAudit()
            }.onFailure { err ->
                _authError.value = err.message ?: "Authentication failed"
            }
        }
    }

    fun loginWithPin(pin: String) {
        viewModelScope.launch {
            _authError.value = null
            val result = repository.authenticateWithPin(pin)
            result.onSuccess { user ->
                _currentUser.value = user
                _isLocked.value = false
                _feedbackMessage.value = "Security PIN verified"
                refreshAudit()
            }.onFailure { err ->
                _authError.value = err.message ?: "Incorrect security PIN"
            }
        }
    }

    fun registerUser(username: String, displayName: String, pass: String, pin: String, role: String) {
        viewModelScope.launch {
            _authError.value = null
            val result = repository.registerUser(username, displayName, pass, pin, role)
            result.onSuccess { user ->
                _currentUser.value = user
                _isLocked.value = false
                _feedbackMessage.value = "Account created successfully"
            }.onFailure { err ->
                _authError.value = err.message ?: "Registration failed"
            }
        }
    }

    fun lockApp() {
        _isLocked.value = true
    }

    fun logout() {
        _currentUser.value = null
        _isLocked.value = true
        _feedbackMessage.value = "Session securely locked"
    }

    fun saveTransaction(tx: AccountTransaction) {
        viewModelScope.launch {
            try {
                repository.saveTransaction(tx)
                _feedbackMessage.value = "Transaction saved & store synchronized"
                refreshAudit()
            } catch (e: Exception) {
                _feedbackMessage.value = "Error saving transaction: ${e.message}"
            }
        }
    }

    fun deleteTransaction(tx: AccountTransaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(tx)
                _feedbackMessage.value = "Transaction deleted"
                refreshAudit()
            } catch (e: Exception) {
                _feedbackMessage.value = "Error deleting: ${e.message}"
            }
        }
    }

    fun syncPendingTransactions() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val res = repository.syncAllPendingTransactions()
                res.onSuccess { count ->
                    _feedbackMessage.value = if (count > 0) "Successfully synced $count transaction(s) to store" else "Store is completely up to date"
                    refreshAudit()
                }.onFailure { e ->
                    _feedbackMessage.value = "Sync failed: ${e.message}"
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun refreshAudit() {
        viewModelScope.launch {
            val report = repository.verifyStoreIntegrity()
            _auditReport.value = report
        }
    }

    fun saveCompanySettings(settings: CompanySettingsEntity) {
        viewModelScope.launch {
            repository.saveCompanySettings(settings)
            _feedbackMessage.value = "Company settings updated"
        }
    }

    fun saveDriver(driver: DriverEntity) {
        viewModelScope.launch {
            repository.saveDriver(driver)
            _feedbackMessage.value = "Driver roster updated"
        }
    }

    fun deleteDriver(driver: DriverEntity) {
        viewModelScope.launch {
            repository.deleteDriver(driver)
            _feedbackMessage.value = "Driver removed from roster"
        }
    }

    fun saveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.saveCategory(category)
            _feedbackMessage.value = "Category updated"
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            _feedbackMessage.value = "Category removed"
        }
    }

    fun exportStoreJson(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportStoreToJson()
            onExportReady(json)
        }
    }
}
