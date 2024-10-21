package com.example.alleysway.models

data class LeaderboardEntry(
    val firstName: String,

    val totalWeight: Double,
    val profileUrl: String
) {
    // Helper method to get full name
    val fullName: String
        get() = "$firstName "
}
