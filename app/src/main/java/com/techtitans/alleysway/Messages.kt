// File: app/src/main/java/com/example/alleysway/Messages.kt
package com.techtitans.alleysway

/**
 * Data class representing a single chat message between users.
 *
 * @param senderId The unique identifier of the message sender.
 * @param receiverId The unique identifier of the message receiver.
 * @param senderName The display name of the message sender.
 * @param text The content of the message.
 * @param timestamp The time the message was sent, represented as milliseconds since epoch.
 */
data class Messages(
    val senderId: String,      // ID of the user who sent the message
    val receiverId: String,    // ID of the user who receives the message
    val senderName: String,    // Name of the sender for display purposes
    val text: String,          // The actual message content
    val timestamp: Long        // Time when the message was sent
)
