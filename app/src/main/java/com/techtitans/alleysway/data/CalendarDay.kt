package com.techtitans.alleysway.data

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName
import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate?,
    val attendanceCount: Int = 0
)

data class MonthData(val name: String, val daysInMonth: Int)

@IgnoreExtraProperties
data class BookedSession(
    @get:PropertyName("ClientID") @set:PropertyName("ClientID")
    var clientID: String = "",

    @get:PropertyName("TrainerID") @set:PropertyName("TrainerID")
    var trainerID: String = "",

    @get:PropertyName("Paid") @set:PropertyName("Paid")
    var paid: Boolean = false,

    @get:PropertyName("TotalAmount") @set:PropertyName("TotalAmount")
    var totalAmount: Double = 0.0,

    @get:PropertyName("StartDateTime") @set:PropertyName("StartDateTime")
    var startDateTime: String = "",

    @get:PropertyName("EndDateTime") @set:PropertyName("EndDateTime")
    var endDateTime: String = ""
)


