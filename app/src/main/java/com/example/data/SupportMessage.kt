package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "support_messages")
data class SupportMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticketId: Int,
    val senderId: Int,
    val senderRole: String, // "User" or "Admin"
    val senderName: String,
    val message: String,
    val timestamp: Long,
    val imagePath: String? = null
)
