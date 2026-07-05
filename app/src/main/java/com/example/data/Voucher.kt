package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vouchers")
data class Voucher(
    @PrimaryKey val code: String,
    val amount: Double,
    val creatorId: Int,
    val isRedeemed: Boolean = false,
    val redeemedById: Int? = null,
    val balanceType: String = "Main Balance",
    val isCancelled: Boolean = false,
    val designatedReceiver: String? = null,
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
    val timestamp: Long = System.currentTimeMillis()
)
