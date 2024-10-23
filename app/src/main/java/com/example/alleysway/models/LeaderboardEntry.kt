package com.example.alleysway.models


data class LeaderboardEntry(
    val firstName: String,
    val totalWeight: Double,
    val totalReps: Int,
    val profileUrl: String,
    var rankChange: Int = 0
)

 {
    // Helper method to get full name
    val fullName: String
        get() = "$firstName "
}
