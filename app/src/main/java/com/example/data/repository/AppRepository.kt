package com.example.data.repository

import android.util.Log
import com.example.data.FirebaseManager
import com.example.data.database.CustomerDao
import com.example.data.database.CustomerEntity
import com.example.data.database.OwnerDao
import com.example.data.database.OwnerEntity
import com.example.data.database.TransactionDao
import com.example.data.database.TransactionEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AppRepository(
    private val ownerDao: OwnerDao,
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao
) {
    private val TAG = "AppRepository"

    private fun getAuth(): FirebaseAuth? = FirebaseManager.getAuth()
    private fun getFirestore(): FirebaseFirestore? = FirebaseManager.getFirestore()

    // --- OWNER AUTHENTICATION & MANAGEMENT ---

    suspend fun signUpOwner(owner: OwnerEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Save locally in Room
            ownerDao.insertOwner(owner)

            // 2. Register in Firebase Auth (if available and not offline)
            val auth = getAuth()
            if (auth != null) {
                try {
                    auth.createUserWithEmailAndPassword(owner.email, owner.password).await()
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase Auth sign up failed: ${e.message}. Proceeding locally.")
                }
            }

            // 3. Save profile details in Firestore (if available)
            val firestore = getFirestore()
            if (firestore != null) {
                try {
                    val profileData = mapOf(
                        "email" to owner.email,
                        "name" to owner.name,
                        "shopName" to owner.shopName,
                        "whatsApp" to owner.whatsApp,
                        "password" to owner.password,
                        "currency" to owner.currency,
                        "language" to owner.language,
                        "soundEnabled" to owner.soundEnabled,
                        "vibrationEnabled" to owner.vibrationEnabled
                    )
                    firestore.collection("owners").document(owner.email).set(profileData).await()
                    Log.d(TAG, "Owner Firestore profile created!")
                } catch (e: Exception) {
                    Log.e(TAG, "Firestore write failed: ${e.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sign up error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun loginOwner(email: String, password: String): Result<OwnerEntity> = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = email.lowercase().trim()
            val normalizedPassword = password.uppercase().trim()

            // 1. Try Firestore First
            val auth = getAuth()
            val firestore = getFirestore()
            var firestoreOwner: OwnerEntity? = null

            if (auth != null && firestore != null) {
                try {
                    // Try to sign in with Firebase Auth
                    auth.signInWithEmailAndPassword(normalizedEmail, normalizedPassword).await()
                    
                    // Retrieve user data from Firestore
                    val snapshot = firestore.collection("owners").document(normalizedEmail).get().await()
                    if (snapshot.exists()) {
                        firestoreOwner = OwnerEntity(
                            email = normalizedEmail,
                            name = snapshot.getString("name") ?: "",
                            shopName = snapshot.getString("shopName") ?: "",
                            whatsApp = snapshot.getString("whatsApp") ?: "",
                            password = normalizedPassword,
                            currency = snapshot.getString("currency") ?: "INR",
                            language = snapshot.getString("language") ?: "EN",
                            soundEnabled = snapshot.getBoolean("soundEnabled") ?: true,
                            vibrationEnabled = snapshot.getBoolean("vibrationEnabled") ?: true
                        )
                        // Update our local cache with latest Firestore state
                        ownerDao.insertOwner(firestoreOwner)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase Login/Fetch failed: ${e.message}. Falling back to local Room cache.")
                }
            }

            // 2. Fetch from Room cache
            val localOwner = ownerDao.getOwnerByEmail(normalizedEmail)

            val finalOwner = firestoreOwner ?: localOwner

            if (finalOwner == null) {
                return@withContext Result.failure(Exception("Account not found. Please register first."))
            }

            if (finalOwner.password != normalizedPassword) {
                return@withContext Result.failure(Exception("Incorrect password."))
            }

            // 3. Sync all transactions and customers from Firestore to local Room Cache
            if (firestore != null) {
                syncFromFirestore(normalizedEmail)
            }

            Result.success(finalOwner)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed with error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateOwnerProfile(owner: OwnerEntity) = withContext(Dispatchers.IO) {
        ownerDao.updateOwner(owner)

        // Sync update to Firestore
        val firestore = getFirestore()
        if (firestore != null) {
            try {
                firestore.collection("owners").document(owner.email).set(owner).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update Firestore profile: ${e.message}")
            }
        }
    }

    suspend fun deleteOwnerAccount(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Delete from Room cache
            ownerDao.deleteOwner(email)

            // Delete from Firestore
            val firestore = getFirestore()
            if (firestore != null) {
                try {
                    firestore.collection("owners").document(email).delete().await()
                    
                    // Optionally delete customers and transactions
                    val customersSnapshot = firestore.collection("owners").document(email).collection("customers").get().await()
                    for (doc in customersSnapshot.documents) {
                        doc.reference.delete()
                    }
                    val transactionsSnapshot = firestore.collection("owners").document(email).collection("transactions").get().await()
                    for (doc in transactionsSnapshot.documents) {
                        doc.reference.delete()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Firestore delete account failed: ${e.message}")
                }
            }

            // Delete from Firebase Auth
            val auth = getAuth()
            if (auth != null) {
                try {
                    auth.currentUser?.delete()?.await()
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase Auth delete user failed: ${e.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Delete account failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val auth = getAuth()
            if (auth != null) {
                auth.sendPasswordResetEmail(email.lowercase().trim()).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Firebase is currently unavailable. Password reset cannot be sent."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send reset email: ${e.message}")
            Result.failure(e)
        }
    }

    fun observeOwner(email: String): Flow<OwnerEntity?> = ownerDao.observeOwnerByEmail(email)

    // --- CUSTOMER MANAGEMENT ---

    fun observeCustomers(ownerEmail: String): Flow<List<CustomerEntity>> =
        customerDao.getCustomersForOwner(ownerEmail)

    suspend fun addCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        customerDao.insertCustomer(customer)

        val firestore = getFirestore()
        if (firestore != null) {
            try {
                firestore.collection("owners").document(customer.ownerEmail)
                    .collection("customers").document(customer.uniqueId).set(customer).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync customer to Firestore: ${e.message}")
            }
        }
    }

    suspend fun deleteCustomer(customerUniqueId: String, ownerEmail: String) = withContext(Dispatchers.IO) {
        customerDao.deleteCustomer(customerUniqueId)
        transactionDao.deleteTransactionsForCustomer(customerUniqueId)

        val firestore = getFirestore()
        if (firestore != null) {
            try {
                firestore.collection("owners").document(ownerEmail)
                    .collection("customers").document(customerUniqueId).delete().await()

                // Also delete their transactions in Firestore
                val transSnapshot = firestore.collection("owners").document(ownerEmail)
                    .collection("transactions").whereEqualTo("customerUniqueId", customerUniqueId).get().await()
                for (doc in transSnapshot.documents) {
                    doc.reference.delete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete customer from Firestore: ${e.message}")
            }
        }
    }

    // --- TRANSACTION MANAGEMENT ---

    fun observeTransactionsForCustomer(customerUniqueId: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForCustomer(customerUniqueId)

    fun observeTransactionsForOwner(ownerEmail: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForOwner(ownerEmail)

    fun observeTransactionsInDateRange(ownerEmail: String, start: Long, end: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsInDateRange(ownerEmail, start, end)

    suspend fun addTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)

        val firestore = getFirestore()
        if (firestore != null) {
            try {
                // Generate a Firestore document reference with ID so we can sync it
                val idStr = if (transaction.id == 0) System.currentTimeMillis().toString() else transaction.id.toString()
                firestore.collection("owners").document(transaction.ownerEmail)
                    .collection("transactions").document(idStr).set(transaction).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync transaction to Firestore: ${e.message}")
            }
        }
    }

    suspend fun deleteTransaction(id: Int, customerUniqueId: String, ownerEmail: String) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(id)

        val firestore = getFirestore()
        if (firestore != null) {
            try {
                firestore.collection("owners").document(ownerEmail)
                    .collection("transactions").document(id.toString()).delete().await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete transaction from Firestore: ${e.message}")
            }
        }
    }

    // --- FIREBASE SYNC ENGINE ---

    private suspend fun syncFromFirestore(ownerEmail: String) {
        val firestore = getFirestore() ?: return
        try {
            // 1. Sync Customers
            val customersSnapshot = firestore.collection("owners").document(ownerEmail)
                .collection("customers").get().await()
            for (doc in customersSnapshot.documents) {
                val uniqueId = doc.id
                val name = doc.getString("name") ?: ""
                val mobile = doc.getString("mobile") ?: ""
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                
                customerDao.insertCustomer(
                    CustomerEntity(uniqueId, name, mobile, ownerEmail, createdAt)
                )
            }

            // 2. Sync Transactions
            val transactionsSnapshot = firestore.collection("owners").document(ownerEmail)
                .collection("transactions").get().await()
            for (doc in transactionsSnapshot.documents) {
                val id = doc.getLong("id")?.toInt() ?: doc.id.hashCode()
                val customerUniqueId = doc.getString("customerUniqueId") ?: ""
                val amount = doc.getDouble("amount") ?: 0.0
                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                val notes = doc.getString("notes") ?: ""

                transactionDao.insertTransaction(
                    TransactionEntity(id, customerUniqueId, ownerEmail, amount, timestamp, notes)
                )
            }
            Log.d(TAG, "Sync complete! Pulled ${customersSnapshot.size()} customers and ${transactionsSnapshot.size()} transactions.")
        } catch (e: Exception) {
            Log.e(TAG, "Synchronization with Firestore failed: ${e.message}", e)
        }
    }
}
