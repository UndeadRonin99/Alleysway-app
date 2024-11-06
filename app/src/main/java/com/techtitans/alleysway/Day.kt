package com.techtitans.alleysway.model

import java.time.LocalDate

data class Day(
    val date: LocalDate,
    val timeSlots: List<TimeSlot>
)

