package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "support_tickets")
data class SupportTicket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val username: String,
    val subject: String,
    val status: String = "Open", // "Open", "Closed", "Pending Admin Response", "Pending User Response"
    val lastUpdated: Long,
    val createdAt: Long
)
