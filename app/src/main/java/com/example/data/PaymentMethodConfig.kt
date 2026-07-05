package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_methods")
data class PaymentMethodConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // e.g. bKash, Nagad, Bank
    val number: String,
    val subMethod: String, // e.g. Sent Money, Cashout, etc.
    val isActive: Boolean = true
)
