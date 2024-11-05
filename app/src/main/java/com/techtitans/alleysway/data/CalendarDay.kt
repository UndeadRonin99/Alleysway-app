package com.techtitans.alleysway.data

import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate?,
    val attendanceCount: Int = 0
)

data class MonthData(val name: String, val daysInMonth: Int)

data class BookedSession(
    val TrainerID: String = "",
    val ClientID: String = "",
    val Paid: Boolean = false,
    val TotalAmount: Double = 0.0,
    val StartDateTime: String = "",  // ISO 8601 format
    val EndDateTime: String = ""     // ISO 8601 format
)

