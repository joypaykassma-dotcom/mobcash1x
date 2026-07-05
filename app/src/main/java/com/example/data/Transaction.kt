package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val type: String, // "deposit", "withdraw", "sent"
    val amount: Double,
    val trxId: String,
    val previousBalance: Double = 0.0,
    val newBalance: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
