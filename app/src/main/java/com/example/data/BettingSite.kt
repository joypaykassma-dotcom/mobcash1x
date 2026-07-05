package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "betting_sites")
data class BettingSite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val siteName: String,
    val username: String,
    val passwordHash: String,
    val workId: String,
    val withdrawAddress: String,
    val balance: Double = 0.0,
    val isActive: Boolean = true,
    val onlyDeposit: Boolean = false,
    val onlyWithdraw: Boolean = false
)
