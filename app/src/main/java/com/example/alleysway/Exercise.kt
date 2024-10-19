package com.example.alleysway

data class Exercise(
    val name: String = "",
    val imageUrl: String = "",
    val mainMuscle: String = "",
    val tips: List<String> = emptyList()
)
