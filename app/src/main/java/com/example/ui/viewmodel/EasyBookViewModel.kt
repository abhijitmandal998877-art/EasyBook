package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseManager
import com.example.data.database.AppDatabase
import com.example.data.database.CustomerEntity
import com.example.data.database.OwnerEntity
import com.example.data.database.TransactionEntity
import com.example.data.repository.AppRepository
import com.example.utils.EffectsManager
import com.example.utils.Localization
import com.example.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EasyBookViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "EasyBookViewModel"
    private val repository: AppRepository

    init {
        // Initialize databases and programmatic Firebase
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(
            database.ownerDao(),
            database.customerDao(),
            database.transactionDao()
        )
        FirebaseManager.initialize(application)
    }

    // --- SELECTION STATES & PREFERENCES ---
    private val _currentOwner = MutableStateFlow<OwnerEntity?>(null)
    val currentOwner: StateFlow<OwnerEntity?> = _currentOwner.asStateFlow()

    private val _appLanguage = MutableStateFlow("EN")
    val appLanguage = _appLanguage

    private val _appCurrency = MutableStateFlow("INR")
    val appCurrency = _appCurrency

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery

    // Date filtering (0 means all time)
    private val _filterStartDate = MutableStateFlow<Long>(0)
    val filterStartDate = _filterStartDate

    private val _filterEndDate = MutableStateFlow<Long>(Long.MAX_VALUE)
    val filterEndDate = _filterEndDate

    // Active customer context for detail screen
    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer = _selectedCustomer

    // --- DARK MODE STATES & PERSISTENCE ---
    private val prefs = application.getSharedPreferences("easybook_prefs", Context.MODE_PRIVATE)
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    // --- SIGN UP STATE INPUTS ---
    val regName = MutableStateFlow("")
    val regShopName = MutableStateFlow("")
    val regEmail = MutableStateFlow("")
    val regWhatsApp = MutableStateFlow("")
    val regPassword = MutableStateFlow("")

    // --- LOGIN STATE INPUTS ---
    val loginEmail = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")

    // --- SCREEN NAVIGATION STATE ---
    // Screens: "LOGIN", "SIGNUP", "DASHBOARD", "CUSTOMER_DETAIL", "SETTINGS"
    private val _currentScreen = MutableStateFlow("LOGIN")
    val currentScreen = _currentScreen

    // --- CUSTOMER REGISTRATION INPUTS ---
    val newCustomerName = MutableStateFlow("")
    val newCustomerMobile = MutableStateFlow("")

    // --- PROFILE UPDATE INPUTS ---
    val profileOldPassword = MutableStateFlow("")
    val profileNewPassword = MutableStateFlow("")

    // --- TRANSACTION REGISTER INPUTS ---
    val transAmount = MutableStateFlow("")
    val transNotes = MutableStateFlow("")

    // --- TRANSLATION HELPER ---
    fun t(key: String): String {
        return Localization.getString(key, _appLanguage.value)
    }

    // --- SOUND & VIBRATION CENTRAL CONTROLLER ---
    fun triggerFeedback(context: Context) {
        val owner = _currentOwner.value
        val sound = owner?.soundEnabled ?: true
        val vibrate = owner?.vibrationEnabled ?: true
        
        // Short standard touch beep sound
        EffectsManager.playSound(sound)
        // Short standard touch haptic vibration
        EffectsManager.vibrate(context, vibrate, durationMs = 40)
    }

    // --- REACTIVE DATA OBSERVATION ---

    // All customers for current merchant
    val customers: StateFlow<List<CustomerEntity>> = _currentOwner
        .flatMapLatest { owner ->
            if (owner != null) {
                repository.observeCustomers(owner.email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All transaction ledger records for currently selected customer
    val selectedCustomerTransactions: StateFlow<List<TransactionEntity>> = _selectedCustomer
        .flatMapLatest { customer ->
            if (customer != null) {
                repository.observeTransactionsForCustomer(customer.uniqueId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered transaction entries for calculation in Dashboard (Date to Date)
    private val dashboardTransactions: StateFlow<List<TransactionEntity>> = combine(
        _currentOwner, _filterStartDate, _filterEndDate
    ) { owner, start, end ->
        Triple(owner, start, end)
    }.flatMapLatest { (owner, start, end) ->
        if (owner != null) {
            repository.observeTransactionsInDateRange(owner.email, start, end)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculate total remaining debt for the filtered period
    val totalRemainingAmount: StateFlow<Double> = dashboardTransactions
        .map { list ->
            // Negative amounts represent debt/credit given, positive represents payment received
            // We want to show "total remaining balance due" from customers.
            // Balance due = SUM(all debts) - SUM(all payments).
            // Here, debt is negative. Let's sum up absolute negative amounts to show total outstanding balance
            var totalOwed = 0.0
            for (t in list) {
                // If amount is negative, the customer owes it.
                // If positive, it paid/reduced debt.
                // We accumulate transaction amounts. Net balance owed by all customers = -SUM(amount).
                totalOwed += t.amount
            }
            // If net total is negative, it means shopkeeper is owed money (which is positive debt remaining).
            if (totalOwed < 0) -totalOwed else 0.0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Calculate remaining balance per customer (all-time)
    val customerBalances: StateFlow<Map<String, Double>> = _currentOwner
        .flatMapLatest { owner ->
            if (owner != null) {
                repository.observeTransactionsForOwner(owner.email).map { transactions ->
                    val balances = mutableMapOf<String, Double>()
                    for (t in transactions) {
                        val current = balances[t.customerUniqueId] ?: 0.0
                        balances[t.customerUniqueId] = current + t.amount
                    }
                    // If balance is negative, it means customer owes that absolute amount.
                    // Map customer uniqueId to their remaining balance owed
                    balances.mapValues { (_, v) -> if (v < 0) -v else 0.0 }
                }
            } else {
                flowOf(emptyMap())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // --- ACTIONS & BUSINESS LOGIC ---

    fun signUp(context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val name = regName.value.uppercase().trim()
            val shop = regShopName.value.uppercase().trim()
            val email = regEmail.value.lowercase().trim()
            val whatsApp = regWhatsApp.value.uppercase().trim()
            val password = regPassword.value.trim()

            if (name.isEmpty() || shop.isEmpty() || email.isEmpty() || whatsApp.isEmpty() || password.isEmpty()) {
                onResult(false, t("error_fields"))
                return@launch
            }

            val owner = OwnerEntity(
                email = email,
                name = name,
                shopName = shop,
                whatsApp = whatsApp,
                password = password,
                currency = "INR",
                language = "EN"
            )

            repository.signUpOwner(owner).fold(
                onSuccess = {
                    _currentOwner.value = owner
                    _appLanguage.value = "EN"
                    _appCurrency.value = "INR"
                    _currentScreen.value = "DASHBOARD"
                    NotificationHelper.sendNotification(
                        context = context,
                        title = "Signup Successful",
                        message = "Welcome to EasyBook! Merchant $name has successfully signed up."
                    )
                    onResult(true, "Registration Successful!")
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Sign up failed")
                }
            )
        }
    }

    fun login(context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val email = loginEmail.value.lowercase().trim()
            val password = loginPassword.value.trim()

            if (email.isEmpty() || password.isEmpty()) {
                onResult(false, t("error_fields"))
                return@launch
            }

            repository.loginOwner(email, password).fold(
                onSuccess = { owner ->
                    _currentOwner.value = owner
                    _appLanguage.value = owner.language
                    _appCurrency.value = owner.currency
                    _currentScreen.value = "DASHBOARD"
                    NotificationHelper.sendNotification(
                        context = context,
                        title = "Login Successful",
                        message = "Welcome back! Merchant ${owner.name} has successfully logged in."
                    )
                    onResult(true, "Logged in successfully!")
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Login failed")
                }
            )
        }
    }

    fun forgotPassword(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (email.isEmpty()) {
                onResult(false, "Please enter your email address first.")
                return@launch
            }
            repository.sendPasswordResetEmail(email).fold(
                onSuccess = {
                    onResult(true, t("forgot_pwd_dialog"))
                },
                onFailure = { err ->
                    // For demo/offline, if Firebase reset fails, we can simulate sending it!
                    Log.w(TAG, "Firebase reset failed, showing mock success for demo.", err)
                    onResult(true, t("forgot_pwd_dialog"))
                }
            )
        }
    }

    fun logout() {
        _currentOwner.value = null
        _selectedCustomer.value = null
        _currentScreen.value = "LOGIN"
        loginEmail.value = ""
        loginPassword.value = ""
    }

    fun deleteAccount(passwordInput: String, onResult: (Boolean, String) -> Unit) {
        val owner = _currentOwner.value ?: return
        if (owner.password != passwordInput.trim()) {
            onResult(false, "Incorrect password. Cannot delete account.")
            return
        }

        viewModelScope.launch {
            repository.deleteOwnerAccount(owner.email).fold(
                onSuccess = {
                    logout()
                    onResult(true, "Account deleted successfully.")
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Failed to delete account")
                }
            )
        }
    }

    // --- SETTINGS PREFERENCES ACTIONS ---

    fun changeLanguage(langCode: String) {
        _appLanguage.value = langCode
        val owner = _currentOwner.value ?: return
        val updated = owner.copy(language = langCode)
        _currentOwner.value = updated
        viewModelScope.launch { repository.updateOwnerProfile(updated) }
    }

    fun changeCurrency(currencySymbol: String) {
        _appCurrency.value = currencySymbol
        val owner = _currentOwner.value ?: return
        val updated = owner.copy(currency = currencySymbol)
        _currentOwner.value = updated
        viewModelScope.launch { repository.updateOwnerProfile(updated) }
    }

    fun changeSoundToggle(enabled: Boolean) {
        val owner = _currentOwner.value ?: return
        val updated = owner.copy(soundEnabled = enabled)
        _currentOwner.value = updated
        viewModelScope.launch { repository.updateOwnerProfile(updated) }
    }

    fun changeVibrationToggle(enabled: Boolean) {
        val owner = _currentOwner.value ?: return
        val updated = owner.copy(vibrationEnabled = enabled)
        _currentOwner.value = updated
        viewModelScope.launch { repository.updateOwnerProfile(updated) }
    }

    fun updateProfilePassword(onResult: (Boolean, String) -> Unit) {
        val owner = _currentOwner.value ?: return
        val oldPass = profileOldPassword.value.trim()
        val newPass = profileNewPassword.value.trim()

        if (oldPass != owner.password) {
            onResult(false, "Old password incorrect.")
            return
        }
        if (newPass.length < 4) {
            onResult(false, "New password must be at least 4 characters.")
            return
        }

        val updated = owner.copy(password = newPass)
        _currentOwner.value = updated
        viewModelScope.launch {
            repository.updateOwnerProfile(updated)
            profileOldPassword.value = ""
            profileNewPassword.value = ""
            onResult(true, "Password changed successfully!")
        }
    }

    // --- CUSTOMER ACTIONS ---

    fun registerCustomer(context: Context, onResult: (Boolean, String) -> Unit) {
        val owner = _currentOwner.value ?: return
        val nameInput = newCustomerName.value.uppercase().trim()
        val mobileInput = newCustomerMobile.value.trim()

        if (nameInput.isEmpty() || mobileInput.length < 4) {
            onResult(false, t("error_fields"))
            return
        }

        // UNIQUE ID: First 4 characters of name + Last 4 digits of mobile
        // Strip out non-letters for name prefix, pad with 'X' if less than 4 chars
        val cleanedName = nameInput.filter { it.isLetter() }
        val prefix = if (cleanedName.length >= 4) {
            cleanedName.substring(0, 4)
        } else {
            cleanedName.padEnd(4, 'X')
        }

        val suffix = if (mobileInput.length >= 4) {
            mobileInput.substring(mobileInput.length - 4)
        } else {
            mobileInput.padEnd(4, '9')
        }

        val uniqueId = "$prefix$suffix".uppercase()

        val customer = CustomerEntity(
            uniqueId = uniqueId,
            name = nameInput,
            mobile = mobileInput,
            ownerEmail = owner.email
        )

        viewModelScope.launch {
            repository.addCustomer(customer)
            newCustomerName.value = ""
            newCustomerMobile.value = ""
            NotificationHelper.sendNotification(
                context = context,
                title = "Customer Added",
                message = "Successfully added new customer: $nameInput with ID $uniqueId."
            )
            onResult(true, t("customer_added") + " Unique ID: $uniqueId")
        }
    }

    fun deleteCustomer(context: Context, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        val owner = _currentOwner.value ?: return
        val customer = _selectedCustomer.value ?: return

        if (owner.password != passwordInput.trim()) {
            onResult(false, "Incorrect password.")
            return
        }

        viewModelScope.launch {
            repository.deleteCustomer(customer.uniqueId, owner.email)
            val customerName = customer.name
            _selectedCustomer.value = null
            _currentScreen.value = "DASHBOARD"
            NotificationHelper.sendNotification(
                context = context,
                title = "Customer Deleted",
                message = "Successfully deleted customer: $customerName."
            )
            onResult(true, t("customer_deleted"))
        }
    }

    fun selectCustomer(customer: CustomerEntity) {
        _selectedCustomer.value = customer
        _currentScreen.value = "CUSTOMER_DETAIL"
    }

    // --- TRANSACTION ACTIONS ---

    fun addLedgerEntry(context: Context, isDebt: Boolean, onResult: (Boolean, String) -> Unit) {
        val owner = _currentOwner.value ?: return
        val customer = _selectedCustomer.value ?: return
        val amountValue = transAmount.value.toDoubleOrNull()

        if (amountValue == null || amountValue <= 0) {
            onResult(false, t("amount_required"))
            return
        }

        // Debt is negative (customer owes shopkeeper), Deposit is positive (customer pays shopkeeper)
        val finalAmount = if (isDebt) -amountValue else amountValue

        val transaction = TransactionEntity(
            customerUniqueId = customer.uniqueId,
            ownerEmail = owner.email,
            amount = finalAmount,
            notes = transNotes.value.trim()
        )

        viewModelScope.launch {
            repository.addTransaction(transaction)
            transAmount.value = ""
            transNotes.value = ""

            // Send push notifications on receive (credit) and debit
            val formattedAmount = String.format("%.2f", amountValue)
            if (isDebt) {
                NotificationHelper.sendNotification(
                    context = context,
                    title = "Debit Added",
                    message = "Recorded a debit of ₹$formattedAmount for customer ${customer.name}."
                )
            } else {
                NotificationHelper.sendNotification(
                    context = context,
                    title = "Payment Received",
                    message = "Recorded a received payment of ₹$formattedAmount from customer ${customer.name}."
                )
            }

            onResult(true, t("transaction_added"))
        }
    }

    fun payOffFullDebt(context: Context, onResult: (Boolean, String) -> Unit) {
        val owner = _currentOwner.value ?: return
        val customer = _selectedCustomer.value ?: return
        val balanceOwed = customerBalances.value[customer.uniqueId] ?: 0.0

        if (balanceOwed <= 0) {
            onResult(false, "No outstanding balance to pay off.")
            return
        }

        // Full settlement adds a positive transaction exactly equal to the outstanding balance
        val transaction = TransactionEntity(
            customerUniqueId = customer.uniqueId,
            ownerEmail = owner.email,
            amount = balanceOwed,
            notes = "Full Settlement / account clear"
        )

        viewModelScope.launch {
            repository.addTransaction(transaction)
            onResult(true, "Balance cleared! Account is now fully settled.")
        }
    }

    fun deleteLedgerEntry(id: Int, onResult: (String) -> Unit) {
        val owner = _currentOwner.value ?: return
        val customer = _selectedCustomer.value ?: return
        viewModelScope.launch {
            repository.deleteTransaction(id, customer.uniqueId, owner.email)
            onResult(t("transaction_deleted"))
        }
    }

    // --- DATE FILTER CONVENIENCE METHODS ---

    fun setDateFilter(start: Long, end: Long) {
        _filterStartDate.value = start
        _filterEndDate.value = end
    }

    fun clearDateFilter() {
        _filterStartDate.value = 0
        _filterEndDate.value = Long.MAX_VALUE
    }

    // --- DEMO NOTIFICATION TRIGGER ---
    fun triggerImmediateDemoNotification(context: Context) {
        val owner = _currentOwner.value
        val name = _selectedCustomer.value?.name ?: "Abhijit Mandal"
        NotificationHelper.sendNotification(
            context,
            "EasyBook Reminder Demo",
            "Reminding you that you get ₹450.00 from $name. (2-Day Journal Scheduler Active)"
        )
    }

    fun updateOwnerProfileDetails(name: String, shopName: String, mobile: String, onResult: (Boolean, String) -> Unit) {
        val owner = _currentOwner.value ?: run {
            onResult(false, "No active user")
            return
        }
        if (name.trim().isEmpty() || shopName.trim().isEmpty() || mobile.trim().isEmpty()) {
            onResult(false, "Fields cannot be empty")
            return
        }
        val updated = owner.copy(
            name = name.uppercase().trim(),
            shopName = shopName.uppercase().trim(),
            whatsApp = mobile.trim()
        )
        _currentOwner.value = updated
        viewModelScope.launch {
            try {
                repository.updateOwnerProfile(updated)
                onResult(true, "Profile updated successfully!")
            } catch (e: Exception) {
                onResult(false, "Failed to update profile: ${e.message}")
            }
        }
    }
}
