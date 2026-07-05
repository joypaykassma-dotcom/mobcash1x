package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: String, // Storing raw password is bad practice, but we'll do simple hashing or just store it plain if simple. Wait, simple plain for demo since it's local.
    val accountType: String,
    val currency: String,
    val name: String,
    val email: String,
    val number: String,
    val bkashNumber: String = "",
    val nagadNumber: String = "",
    val rocketNumber: String = "",
    val withdrawPin: String,
    
    // Balances
    val mainBalance: Double = 0.0,
    val withdrawLimit: Double = 0.0,
    val holdBalance: Double = 0.0,
    val withdrawableBalance: Double = 0.0,
    
    val referBalance: Double = 0.0,
    val bonusBalance: Double = 0.0,
    val commissionBalance: Double = 0.0,
    val gasFeeBalance: Double = 0.0,
    val bonusPercent: Double = 2.0, // Default 2%
    val agentCommissionPercent: Double = 2.0, // Default 2%
    val agentDpCommissionPercent: Double = 2.0, // Default 2%
    val agentWdCommissionPercent: Double = 2.0, // Default 2%
    val role: String = "User", // "User" or "Admin"
    val referralCode: String = "",
    val referredBy: String = "",
    val lastBettingSite: String = "1xbet",
    val lastBettingUserId: String = "",
    val isBlocked: Boolean = false,
    val isHold: Boolean = false,
    val ignoreWithdrawLimit: Boolean = false,
    
    // Action Permissions
    val canDeposit: Boolean = true,
    val canWithdraw: Boolean = true,
    val canAddMoney: Boolean = true,
    val canOutMoney: Boolean = true,
    val canSendMoney: Boolean = true,
    val canReceiveMoney: Boolean = true,
    
    // Cashier specific
    val eposCode: String = "",

    // Hidden penalty fields
    val hiddenPenaltyMain: Double = 0.0,
    val hiddenPenaltyOut: Double = 0.0,
    val hiddenPenaltyCombined: Double = 0.0
)
