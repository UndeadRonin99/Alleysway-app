package com.techtitans.alleysway.model

import java.time.LocalDate

data class TimeSlot(
    val date: LocalDate,
    val startTime: String,
    val endTime: String
)

