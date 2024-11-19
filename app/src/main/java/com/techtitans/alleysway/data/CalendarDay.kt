// Package declaration for data classes related to Alleysway application
package com.techtitans.alleysway.data

// Import statements for Firebase database annotations and LocalDate class
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName
import java.time.LocalDate

// Data class representing a calendar day with optional date and attendance count
data class CalendarDay(
    val date: LocalDate?,            // The specific date of the calendar day
    val attendanceCount: Int = 0     // Number of attendances on that day (default is 0)
)

// Data class representing month information with name and number of days
data class MonthData(
    val name: String,                // Name of the month (e.g., "January")
    val daysInMonth: Int             // Total number of days in the month
)

// Annotation to ignore any extra properties when mapping data from Firebase
@IgnoreExtraProperties
data class BookedSession(
    // Client ID associated with the session, with custom Firebase property name
    @get:PropertyName("ClientID") @set:PropertyName("ClientID")
    var clientID: String = "",

    // Trainer ID associated with the session, with custom Firebase property name
    @get:PropertyName("TrainerID") @set:PropertyName("TrainerID")
    var trainerID: String = "",

    // Boolean indicating if the session has been paid for, with custom property name
    @get:PropertyName("Paid") @set:PropertyName("Paid")
    var paid: Boolean = false,

    // Total amount for the session, with custom Firebase property name
    @get:PropertyName("TotalAmount") @set:PropertyName("TotalAmount")
    var totalAmount: Double = 0.0,

    // Start date and time of the session in string format, with custom property name
    @get:PropertyName("StartDateTime") @set:PropertyName("StartDateTime")
    var startDateTime: String = "",

    // End date and time of the session in string format, with custom property name
    @get:PropertyName("EndDateTime") @set:PropertyName("EndDateTime")
    var endDateTime: String = "",

    // Event ID from a calendar or scheduling system, with custom property name
    @get:PropertyName("EventID") @set:PropertyName("EventID")
    var eventID: String = "",

    // Unique key for the session, with custom Firebase property name
    @get:PropertyName("SessionKey") @set:PropertyName("SessionKey")
    var sessionKey: String = ""
)



























































































































