package com.techtitans.alleysway

data class Messages(
    val senderId: String,
    val receiverId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long
)