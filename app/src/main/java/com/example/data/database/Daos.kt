package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnerDao {
    @Query("SELECT * FROM owners WHERE email = :email LIMIT 1")
    suspend fun getOwnerByEmail(email: String): OwnerEntity?

    @Query("SELECT * FROM owners WHERE email = :email LIMIT 1")
    fun observeOwnerByEmail(email: String): Flow<OwnerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwner(owner: OwnerEntity)

    @Update
    suspend fun updateOwner(owner: OwnerEntity)

    @Query("DELETE FROM owners WHERE email = :email")
    suspend fun deleteOwner(email: String)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE ownerEmail = :ownerEmail ORDER BY name ASC")
    fun getCustomersForOwner(ownerEmail: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE uniqueId = :uniqueId LIMIT 1")
    suspend fun getCustomerByUniqueId(uniqueId: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE uniqueId = :uniqueId")
    suspend fun deleteCustomer(uniqueId: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE customerUniqueId = :customerUniqueId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerUniqueId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE ownerEmail = :ownerEmail ORDER BY timestamp DESC")
    fun getTransactionsForOwner(ownerEmail: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE ownerEmail = :ownerEmail AND timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getTransactionsInDateRange(ownerEmail: String, startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE customerUniqueId = :customerUniqueId")
    suspend fun deleteTransactionsForCustomer(customerUniqueId: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Int)
}
