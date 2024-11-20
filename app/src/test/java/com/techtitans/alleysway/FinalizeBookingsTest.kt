package com.techtitans.alleysway

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.techtitans.alleysway.data.BookedSession
import java.time.LocalDateTime

class FinalizeBookingsTest {

    // You can remove this instance if you're including methods directly
    // private val finalizeBookings = FinalizeBookings()

    @Test
    fun testSplitTimeSlotIntoSessions() {
        val date = LocalDate.of(2023, 10, 20)
        val startTime = "09:00"
        val endTime = "12:00"

        val expectedSlots = listOf(
            TimeSlot(date, "09:00", "10:00"),
            TimeSlot(date, "10:00", "11:00"),
            TimeSlot(date, "11:00", "12:00")
        )

        val actualSlots = splitTimeSlotIntoSessions(date, startTime, endTime)
        assertEquals(expectedSlots, actualSlots)
    }

    @Test
    fun testExcludeBookedTimeSlots() {
        val date = LocalDate.of(2023, 10, 20)
        val timeSlots = listOf(
            TimeSlot(date, "09:00", "10:00"),
            TimeSlot(date, "10:00", "11:00"),
            TimeSlot(date, "11:00", "12:00")
        )

        val bookedSessions = listOf(
            BookedSession(
                trainerID = "trainer1",
                clientID = "client1",
                paid = true,
                totalAmount = 100.0,
                startDateTime = "2023-10-20T10:00:00",
                endDateTime = "2023-10-20T11:00:00",
                eventID = "event1",
                sessionKey = "session1"
            )
        )

        val expectedAvailableSlots = listOf(
            TimeSlot(date, "09:00", "10:00"),
            TimeSlot(date, "11:00", "12:00")
        )

        val actualAvailableSlots = excludeBookedTimeSlots(timeSlots, bookedSessions)
        assertEquals(expectedAvailableSlots, actualAvailableSlots)
    }

    @Test
    fun testParseToZonedDateTime() {
        val dateTimeStr = "2023-10-20T10:00:00"
        val zoneId = ZoneId.of("Africa/Johannesburg")

        val expectedDateTime = ZonedDateTime.of(2023, 10, 20, 10, 0, 0, 0, zoneId)
        val actualDateTime = parseToZonedDateTime(dateTimeStr, zoneId)

        assertEquals(expectedDateTime, actualDateTime)
    }

    @Test
    fun testGetNextDateForDay() {
        val today = LocalDate.of(2023, 10, 16) // Let's assume today is Monday
        setCurrentDateForTesting(today)

        val expectedDate = LocalDate.of(2023, 10, 17) // Next Tuesday
        val actualDate = getNextDateForDay("Tuesday")
        assertEquals(expectedDate, actualDate)
    }


    private var currentDate: LocalDate = LocalDate.now()

    private fun setCurrentDateForTesting(date: LocalDate) {
        this.currentDate = date
    }

    private fun splitTimeSlotIntoSessions(
        date: LocalDate,
        startTimeStr: String,
        endTimeStr: String
    ): List<TimeSlot> {
        val timeSlots = mutableListOf<TimeSlot>()
        val startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"))
        val endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm"))

        var currentTime = startTime
        while (currentTime.isBefore(endTime)) {
            val nextTime = currentTime.plusHours(1)
            if (nextTime.isAfter(endTime)) break
            val timeSlot = TimeSlot(
                date,
                currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                nextTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            )
            timeSlots.add(timeSlot)
            currentTime = nextTime
        }
        return timeSlots
    }

    private fun excludeBookedTimeSlots(
        timeSlots: List<TimeSlot>,
        bookedSessions: List<BookedSession>
    ): List<TimeSlot> {
        val availableTimeSlots = mutableListOf<TimeSlot>()
        val zoneId = ZoneId.of("Africa/Johannesburg")

        for (timeSlot in timeSlots) {
            var isBooked = false
            for (bookedSession in bookedSessions) {
                val bookedStartDateTime = parseToZonedDateTime(bookedSession.startDateTime, zoneId)
                val bookedEndDateTime = parseToZonedDateTime(bookedSession.endDateTime, zoneId)

                if (bookedStartDateTime == null || bookedEndDateTime == null) continue

                val slotStart = ZonedDateTime.of(
                    timeSlot.date,
                    LocalTime.parse(timeSlot.startTime),
                    zoneId
                )
                val slotEnd = ZonedDateTime.of(
                    timeSlot.date,
                    LocalTime.parse(timeSlot.endTime),
                    zoneId
                )

                if (slotStart.isBefore(bookedEndDateTime) && slotEnd.isAfter(bookedStartDateTime)) {
                    isBooked = true
                    break
                }
            }
            if (!isBooked) {
                availableTimeSlots.add(timeSlot)
            }
        }
        return availableTimeSlots
    }

    private fun parseToZonedDateTime(dateTimeStr: String, zoneId: ZoneId): ZonedDateTime? {
        return try {
            val localDateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
            localDateTime.atZone(zoneId)
        } catch (e: Exception) {
            null
        }
    }

    private fun getNextDateForDay(dayName: String): LocalDate {
        val dayOfWeekMap = mapOf(
            "Monday" to java.time.DayOfWeek.MONDAY,
            "Tuesday" to java.time.DayOfWeek.TUESDAY,
            "Wednesday" to java.time.DayOfWeek.WEDNESDAY,
            "Thursday" to java.time.DayOfWeek.THURSDAY,
            "Friday" to java.time.DayOfWeek.FRIDAY,
            "Saturday" to java.time.DayOfWeek.SATURDAY,
            "Sunday" to java.time.DayOfWeek.SUNDAY
        )
        val targetDayOfWeek = dayOfWeekMap[dayName] ?: return currentDate
        val today = currentDate
        var daysUntilTarget = (targetDayOfWeek.value - today.dayOfWeek.value + 7) % 7
        if (daysUntilTarget == 0) daysUntilTarget = 7
        return today.plusDays(daysUntilTarget.toLong())
    }
    data class TimeSlot(
        val date: LocalDate,   // The date on which the time slot is scheduled
        val startTime: String, // The start time of the time slot (e.g., "09:00")
        val endTime: String    // The end time of the time slot (e.g., "10:00")
    )
    data class BookedSession(
        val trainerID: String,
        val clientID: String,
        val paid: Boolean,
        val totalAmount: Double,
        val startDateTime: String,
        val endDateTime: String,
        val eventID: String,
        val sessionKey: String
    )

}
