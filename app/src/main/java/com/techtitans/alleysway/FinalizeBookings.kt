package com.techtitans.alleysway

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventAttendee
import com.google.api.services.calendar.model.EventDateTime
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.techtitans.alleysway.adapter.SelectableTimeSlotAdapter
import com.techtitans.alleysway.data.BookedSession
import com.techtitans.alleysway.model.Day
import com.techtitans.alleysway.model.TimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID  // Import UUID for generating unique IDs



class FinalizeBookings : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var trainerContainer: LinearLayout
    private lateinit var btnBack: ImageView
    private lateinit var txtNames: TextView
    private lateinit var sessionRecycler: RecyclerView
    private lateinit var timeSlotAdapter: SelectableTimeSlotAdapter
    private val timeSlotsList = mutableListOf<Day>()
    private val VIEW_TYPE_HEADER = 0
    private var trainerID: String? = null
    private var trainerEmail: String? = null
    private var trainerName: String? = null
    private var trainerRate: String? = null
    private lateinit var btnBookNow: Button


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_finalize_bookings)

        trainerName = intent.getStringExtra("trainerName")
        trainerID = intent.getStringExtra("trainerID")
        trainerEmail = intent.getStringExtra("trainerEmail")
        trainerRate = intent.getStringExtra("rate")

        btnBack = findViewById(R.id.back_arrow)
        btnBack.setOnClickListener {
            finish()
        }

        txtNames = findViewById(R.id.toolbar_text)
        txtNames.text = "Choose Date and Time to work with $trainerName"

        sessionRecycler = findViewById(R.id.session_slots_recycler)
        val gridLayoutManager = GridLayoutManager(this, 3) // 3 items per row for time slots
        sessionRecycler.layoutManager = gridLayoutManager
        timeSlotAdapter = SelectableTimeSlotAdapter(timeSlotsList)
        sessionRecycler.adapter = timeSlotAdapter

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {

                return if (timeSlotAdapter.getItemViewType(position) == VIEW_TYPE_HEADER) {
                    3 // Headers span all 3 columns
                } else {
                    1 // Time slots occupy 1 column each
                }
            }
        }

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Fetch timeslots from the admins
        fetchAdminTimeSlots()

        btnBookNow = findViewById(R.id.book_now_button)
        btnBookNow.setOnClickListener{
            val selectedSessions = timeSlotAdapter.getSelectedTimeSlots()
            createCalendarEvent(selectedSessions)
            val intent = Intent(this, BookingSuccessActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private const val REQUEST_AUTHORIZATION = 1001
    }

    private fun getFullName(onResult: (String?) -> Unit) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val uid = user.uid
            val database = Firebase.database.reference

            val userRef = database.child("users").child(uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
                    val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                    val fullName = "$firstName $lastName"
                    onResult(fullName)
                } else {
                    Log.e("getFullName", "User data not found for UID: $uid")
                    onResult(null)
                }
            }.addOnFailureListener { exception ->
                Log.e("getFullName", "Error fetching user data: ${exception.message}")
                onResult(null)
            }
        } else {
            Log.e("getFullName", "No user is signed in.")
            onResult(null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createCalendarEvent(selectedSessions: List<TimeSlot>) {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        val user = Firebase.auth.currentUser
        val userID = user?.uid

        getFullName { UserName ->
            if (account != null && userID != null && UserName != null) {
                val credential = GoogleAccountCredential.usingOAuth2(
                    this, listOf("https://www.googleapis.com/auth/calendar")
                )
                credential.selectedAccount = account.account

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val service = Calendar.Builder(
                            NetHttpTransport(),
                            JacksonFactory.getDefaultInstance(),
                            credential
                        ).setApplicationName("Alleysway Gym").build()

                        // Loop through selected sessions and create events
                        for (session in selectedSessions) {
                            // Prepare date and time
                            val date = session.date
                            val startTime = session.startTime
                            val endTime = session.endTime

                            val startDateTimeLocal = LocalDateTime.of(date, LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm")))
                            val endDateTimeLocal = LocalDateTime.of(date, LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm")))
                            val zoneId = ZoneId.of("Africa/Johannesburg")
                            val startDateTimeZoned = startDateTimeLocal.atZone(zoneId)
                            val endDateTimeZoned = endDateTimeLocal.atZone(zoneId)

                            // Create Google Calendar event
                            val event = Event().apply {
                                summary = "Training Session with $trainerName"
                                description = "Booked by $UserName"
                                start = EventDateTime().apply {
                                    dateTime = com.google.api.client.util.DateTime(startDateTimeZoned.toInstant().toEpochMilli())
                                    timeZone = "Africa/Johannesburg"
                                }
                                end = EventDateTime().apply {
                                    dateTime = com.google.api.client.util.DateTime(endDateTimeZoned.toInstant().toEpochMilli())
                                    timeZone = "Africa/Johannesburg"
                                }
                                attendees = listOf(EventAttendee().setEmail(trainerEmail))
                            }
                            val eventResult = service.events().insert("primary", event).execute()
                            Log.d("CalendarEvent", "Event created: ${eventResult.htmlLink}")

                            // Generate unique session ID
                            val sessionId = UUID.randomUUID().toString()

                            // Save booked session to Firebase
                            val isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                            val startDateTimeStr = startDateTimeZoned.format(isoFormatter)
                            val endDateTimeStr = endDateTimeZoned.format(isoFormatter)

                            val bookedSession = BookedSession(
                                trainerID = trainerID!!,
                                clientID = userID,
                                paid = false,
                                totalAmount = trainerRate!!.toDouble(),
                                startDateTime = startDateTimeStr,
                                endDateTime = endDateTimeStr,
                                eventID = eventResult.id,
                                sessionKey = sessionId  // Assign the session ID
                            )

                            bookedSession.sessionKey = sessionId

                            // Save under trainer's sessions
                            val trainerSessionRef = database.child("users").child(trainerID!!).child("sessions").child("SessionID").child(sessionId)
                            trainerSessionRef.setValue(bookedSession)

                            // Save under client's sessions
                            val clientSessionRef = database.child("users").child(userID).child("sessions").child("SessionID").child(sessionId)
                            clientSessionRef.setValue(bookedSession)

                            // Create a Message object
                            val messageText = "I've booked a session with you on ${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} at $startTime"
                            val timestamp = System.currentTimeMillis()

                            val message = Messages(
                                senderId = userID,
                                receiverId = trainerID!!,
                                senderName = UserName,
                                text = messageText,
                                timestamp = timestamp
                            )

                            // Save the message for both client and trainer
                            val clientMessageRef = database.child("user_messages").child(userID).child(trainerID!!).child("messages").push()
                            clientMessageRef.setValue(message)

                            val trainerMessageRef = database.child("user_messages").child(trainerID!!).child(userID).child("messages").push()
                            trainerMessageRef.setValue(message)
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@FinalizeBookings, "Events created successfully!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: UserRecoverableAuthIOException) {
                        startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@FinalizeBookings, "Failed to create events.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "User not signed in.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDateTime(dayOfWeekStr: String, time: String): LocalDateTime {
        // Map day names to DayOfWeek enum values
        val dayOfWeekMap = mapOf(
            "Monday" to DayOfWeek.MONDAY,
            "Tuesday" to DayOfWeek.TUESDAY,
            "Wednesday" to DayOfWeek.WEDNESDAY,
            "Thursday" to DayOfWeek.THURSDAY,
            "Friday" to DayOfWeek.FRIDAY,
            "Saturday" to DayOfWeek.SATURDAY,
            "Sunday" to DayOfWeek.SUNDAY
        )

        val targetDayOfWeek = dayOfWeekMap[dayOfWeekStr]
            ?: throw IllegalArgumentException("Invalid day of week: $dayOfWeekStr")

        // Get current date in the specified time zone
        val zoneId = ZoneId.of("Africa/Johannesburg")
        val currentDate = LocalDate.now(zoneId)
        val currentDayOfWeek = currentDate.dayOfWeek

        // Calculate days until target day
        val daysUntilTarget = (targetDayOfWeek.value - currentDayOfWeek.value + 7) % 7
        val daysToAdd = if (daysUntilTarget == 0) 7 else daysUntilTarget.toLong()
        val targetDate = currentDate.plusDays(daysToAdd)

        // Combine target date with time
        val dateTimeString = "${targetDate} $time"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val localDateTime = LocalDateTime.parse(dateTimeString, formatter)
        return localDateTime
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchAdminTimeSlots() {
        if (trainerID == null) {
            Log.e("FinalizeBookings", "Trainer ID is null, cannot fetch time slots")
            return
        }

        val trainerRef = database.child("users").child(trainerID!!)
        trainerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                timeSlotsList.clear()
                val role = snapshot.child("role").getValue(String::class.java)
                if (role == "admin") {
                    val daysSnapshot = snapshot.child("Days")
                    val dateSpecificAvailabilitySnapshot = snapshot.child("DateSpecificAvailability")

                    // Fetch booked sessions for this trainer
                    val sessionsSnapshot = snapshot.child("sessions").child("SessionID")
                    val bookedSessions = mutableListOf<BookedSession>()

                    // Define formatter for parsing ISO date strings
                    val formatter = DateTimeFormatter.ISO_DATE_TIME
                    val zoneId = ZoneId.of("Africa/Johannesburg")

                    for (sessionSnapshot in sessionsSnapshot.children) {
                        val bookedSession = sessionSnapshot.getValue(BookedSession::class.java)
                        if (bookedSession != null) {
                            try {
                                // Parse the start time of the booked session
                                val bookedStartLocalDateTime = LocalDateTime.parse(bookedSession.startDateTime, formatter)
                                val bookedStartDateTime = bookedStartLocalDateTime.atZone(zoneId)

                                // Only consider future sessions
                                if (bookedStartDateTime.isAfter(ZonedDateTime.now(zoneId))) {
                                    bookedSessions.add(bookedSession)
                                    Log.d("fetchAdminTimeSlots", "Booked Session Added: $bookedSession")
                                }
                            } catch (e: Exception) {
                                Log.e("fetchAdminTimeSlots", "Error parsing date: ${e.message}")
                            }
                        }
                    }

                    Log.d("fetchAdminTimeSlots", "Total Booked Sessions: ${bookedSessions.size}")

                    // Generate dates for the next 7 days
                    val today = LocalDate.now(zoneId)
                    val dayList = mutableListOf<Day>()

                    for (i in 1 until 8) {
                        val date = today.plusDays(i.toLong())
                        val dateStr = date.toString()

                        // Check if date-specific availability exists for this date
                        val dateSpecificSnapshot = dateSpecificAvailabilitySnapshot.child(dateStr)

                        if (dateSpecificSnapshot.exists()) {
                            // Date-specific availability exists
                            var isFullDayUnavailable = false
                            val dayTimeSlots = mutableListOf<TimeSlot>()

                            for (slotSnapshot in dateSpecificSnapshot.children) {
                                val slotIsFullDayUnavailable = slotSnapshot.child("IsFullDayUnavailable").getValue(Boolean::class.java) ?: false
                                if (slotIsFullDayUnavailable) {
                                    isFullDayUnavailable = true
                                    break // No need to process further, day is fully unavailable
                                }

                                val startTime = slotSnapshot.child("StartTime").getValue(String::class.java)
                                val endTime = slotSnapshot.child("EndTime").getValue(String::class.java)

                                if (startTime != null && endTime != null) {
                                    // Split the time slot into individual 1-hour sessions
                                    val splitTimeSlots = splitTimeSlotIntoSessions(date, startTime, endTime)
                                    dayTimeSlots.addAll(splitTimeSlots)
                                }
                            }

                            if (isFullDayUnavailable) {
                                // Day is fully unavailable, do not add any time slots
                                Log.d("fetchAdminTimeSlots", "Date $dateStr is fully unavailable")
                            } else if (dayTimeSlots.isNotEmpty()) {
                                // Exclude any booked time slots
                                val availableTimeSlots = excludeBookedTimeSlots(dayTimeSlots, bookedSessions)
                                if (availableTimeSlots.isNotEmpty()) {
                                    dayList.add(Day(date, availableTimeSlots))
                                    Log.d("fetchAdminTimeSlots", "Date-specific availability for $dateStr added with ${availableTimeSlots.size} slots")
                                } else {
                                    Log.d("fetchAdminTimeSlots", "No available time slots for date $dateStr after excluding booked sessions")
                                }
                            } else {
                                Log.d("fetchAdminTimeSlots", "No time slots found for date $dateStr")
                            }
                        } else {
                            // No date-specific availability, use default availability
                            val dayOfWeek = date.dayOfWeek
                            val dayName = dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

                            val daySnapshot = daysSnapshot.child(dayName)

                            if (daySnapshot.exists()) {
                                val timeSlotsSnapshot = daySnapshot.child("TimeSlots")
                                val dayTimeSlots = mutableListOf<TimeSlot>()

                                for (timeSlotSnapshot in timeSlotsSnapshot.children) {
                                    val startTime = timeSlotSnapshot.child("StartTime").getValue(String::class.java)
                                    val endTime = timeSlotSnapshot.child("EndTime").getValue(String::class.java)
                                    if (startTime != null && endTime != null) {
                                        // Split the time slot into individual 1-hour sessions
                                        val splitTimeSlots = splitTimeSlotIntoSessions(date, startTime, endTime)
                                        dayTimeSlots.addAll(splitTimeSlots)
                                    }
                                }
                                if (dayTimeSlots.isNotEmpty()) {
                                    // Exclude any booked time slots
                                    val availableTimeSlots = excludeBookedTimeSlots(dayTimeSlots, bookedSessions)
                                    if (availableTimeSlots.isNotEmpty()) {
                                        dayList.add(Day(date, availableTimeSlots))
                                        Log.d("fetchAdminTimeSlots", "Default availability for $dateStr added with ${availableTimeSlots.size} slots")
                                    } else {
                                        Log.d("fetchAdminTimeSlots", "No available time slots for date $dateStr after excluding booked sessions")
                                    }
                                } else {
                                    Log.d("fetchAdminTimeSlots", "No default time slots for day $dayName")
                                }
                            } else {
                                Log.d("fetchAdminTimeSlots", "No default availability for day $dayName")
                            }
                        }
                    }

                    // Sort the days list by date and add to the main time slots list
                    timeSlotsList.addAll(dayList.sortedBy { it.date })
                    Log.d("fetchAdminTimeSlots", "Total Available Days: ${dayList.size}")
                }
                timeSlotAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FinalizeBookings", "Failed to fetch time slots: ${error.message}")
            }
        })
    }

    private fun getNextDateForDay(dayName: String): LocalDate {
        val dayOfWeekMap = mapOf(
            "Monday" to DayOfWeek.MONDAY,
            "Tuesday" to DayOfWeek.TUESDAY,
            "Wednesday" to DayOfWeek.WEDNESDAY,
            "Thursday" to DayOfWeek.THURSDAY,
            "Friday" to DayOfWeek.FRIDAY,
            "Saturday" to DayOfWeek.SATURDAY,
            "Sunday" to DayOfWeek.SUNDAY
        )
        val targetDayOfWeek = dayOfWeekMap[dayName] ?: return LocalDate.now()
        val today = LocalDate.now()
        var daysUntilTarget = (targetDayOfWeek.value - today.dayOfWeek.value + 7) % 7

        // Include today if the target day is today
        if (daysUntilTarget == 0) {
            daysUntilTarget = 7 // Set to 7 if you want to show next week's date
        }
        return today.plusDays(daysUntilTarget.toLong())
    }

    private fun splitTimeSlotIntoSessions(date: LocalDate, startTimeStr: String, endTimeStr: String): List<TimeSlot> {
        val timeSlots = mutableListOf<TimeSlot>()

        val startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"))
        val endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm"))

        var currentTime = startTime

        while (currentTime.plusHours(1).isBefore(endTime) || currentTime.plusHours(1).equals(endTime)) {
            val nextTime = currentTime.plusHours(1)
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

    @RequiresApi(Build.VERSION_CODES.O)
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

                if (bookedStartDateTime == null || bookedEndDateTime == null) {
                    continue // Skip if parsing failed
                }

                // Create ZonedDateTime for the time slot
                val slotStart = ZonedDateTime.of(timeSlot.date, LocalTime.parse(timeSlot.startTime), zoneId)
                val slotEnd = ZonedDateTime.of(timeSlot.date, LocalTime.parse(timeSlot.endTime), zoneId)

                // Check for overlap
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseToZonedDateTime(dateTimeStr: String, zoneId: ZoneId): ZonedDateTime? {
        return try {
            val localDateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
            localDateTime.atZone(zoneId)
        } catch (e: Exception) {
            Log.e("parseToZonedDateTime", "Error parsing dateTimeStr: $dateTimeStr, Error: ${e.message}")
            null
        }
    }



}
