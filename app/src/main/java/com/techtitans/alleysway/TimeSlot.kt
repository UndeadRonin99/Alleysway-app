// Package declaration indicating the namespace of the class
package com.techtitans.alleysway.model

// Importing the LocalDate class from the java.time package
import java.time.LocalDate

/**
 * Data class representing a time slot for scheduling purposes.
 *
 * @property date The specific date for the time slot, represented using LocalDate for easy date manipulation.
 * @property startTime The start time of the time slot in "HH:mm" format (e.g., "09:00").
 * @property endTime The end time of the time slot in "HH:mm" format (e.g., "10:00").
 *
 * This class is typically used to define available or booked time slots within a scheduling system,
 * such as booking appointments, classes, or other time-based activities.
 */
data class TimeSlot(
    val date: LocalDate,   // The date on which the time slot is scheduled
    val startTime: String, // The start time of the time slot (e.g., "09:00")
    val endTime: String    // The end time of the time slot (e.g., "10:00")
)
