package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owners")
data class OwnerEntity(
    @PrimaryKey val email: String, // email is not changed, lowcase
    val name: String,             // uppercase
    val shopName: String,         // uppercase
    val whatsApp: String,         // uppercase/numbers
    val password: String,         // uppercase
    val currency: String = "INR", // e.g., INR (₹), USD ($), EUR (€)
    val language: String = "EN",   // EN, BN, HI
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val uniqueId: String, // e.g., "ABHI3286"
    val name: String,                 // auto-capitalized (uppercase)
    val mobile: String,
    val ownerEmail: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerUniqueId: String,
    val ownerEmail: String,
    val amount: Double,               // Negative means user owes money (Debt), Positive means deposited/paid
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
