package com.example.alleysway.data

import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate?,
    val attendanceCount: Int = 0
)

data class MonthData(val name: String, val daysInMonth: Int)


