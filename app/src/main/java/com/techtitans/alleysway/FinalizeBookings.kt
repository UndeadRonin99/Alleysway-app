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
                                endDateTime = endDateTimeStr
                            )

                            // Save under trainer's sessions
                            val trainerSessionRef = database.child("users").child(trainerID!!).child("sessions").child("SessionID")
                            val newSessionRef = trainerSessionRef.push()
                            newSessionRef.setValue(bookedSession)

                            // Save under client's sessions
                            val clientSessionRef = database.child("users").child(userID).child("sessions").child("SessionID")
                            val newClientSessionRef = clientSessionRef.push()
                            newClientSessionRef.setValue(bookedSession)
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
            Log.e("FinalizeBookings", "Trainer ID is null, cannot fetch timeslots")
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

                    // Fetch booked sessions
                    val sessionsSnapshot = snapshot.child("sessions").child("SessionID")
                    val bookedSessions = mutableListOf<BookedSession>()

                    // Define formatter for parsing dates
                    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

                    val zoneId = ZoneId.of("Africa/Johannesburg")

                    for (sessionSnapshot in sessionsSnapshot.children) {
                        val bookedSession = sessionSnapshot.getValue(BookedSession::class.java)
                        if (bookedSession != null) {
                            try {
                                val startDateTime = ZonedDateTime.parse(bookedSession.startDateTime, formatter)
                                if (startDateTime.isAfter(ZonedDateTime.now(zoneId))) {
                                    bookedSessions.add(bookedSession)
                                }
                            } catch (e: Exception) {
                                Log.e("fetchAdminTimeSlots", "Error parsing date: ${e.message}")
                            }
                        }
                    }

                    // Process availability and calculate actual dates
                    val dayList = mutableListOf<Day>()

                    for (daySnapshot in daysSnapshot.children) {
                        val dayName = daySnapshot.key ?: continue
                        val timeSlotsSnapshot = daySnapshot.child("TimeSlots")
                        val dayTimeSlots = mutableListOf<TimeSlot>()

                        // Calculate the actual date for this day
                        val date = getNextDateForDay(dayName)

                        for (timeSlotSnapshot in timeSlotsSnapshot.children) {
                            val startTime = timeSlotSnapshot.child("StartTime").getValue(String::class.java)
                            val endTime = timeSlotSnapshot.child("EndTime").getValue(String::class.java)
                            if (startTime != null && endTime != null) {
                                val splitTimeSlots = splitTimeSlotIntoSessions(date, startTime, endTime)
                                val availableTimeSlots = excludeBookedTimeSlots(splitTimeSlots, bookedSessions)
                                dayTimeSlots.addAll(availableTimeSlots)
                            }
                        }
                        if (dayTimeSlots.isNotEmpty()) {
                            dayList.add(Day(date, dayTimeSlots))
                        }
                    }

                    // Sort the days list by date
                    timeSlotsList.addAll(dayList.sortedBy { it.date })
                }
                timeSlotAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FinalizeBookings", "Failed to fetch timeslots: ${error.message}")
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

    private fun splitTimeSlotIntoSessions(date: LocalDate, startTime: String, endTime: String): List<TimeSlot> {
        val startHour = startTime.split(":")[0].toInt()
        val endHour = endTime.split(":")[0].toInt()
        val timeSlots = mutableListOf<TimeSlot>()

        for (hour in startHour until endHour) {
            val sessionStartTime = String.format("%02d:00", hour)
            val sessionEndTime = String.format("%02d:00", hour + 1)
            val timeSlot = TimeSlot(date, sessionStartTime, sessionEndTime)
            timeSlots.add(timeSlot)
        }
        return timeSlots
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun excludeBookedTimeSlots(
        timeSlots: List<TimeSlot>,
        bookedSessions: List<BookedSession>
    ): List<TimeSlot> {
        val availableTimeSlots = mutableListOf<TimeSlot>()
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        for (timeSlot in timeSlots) {
            var isBooked = false
            for (bookedSession in bookedSessions) {
                try {
                    val bookedStartDateTime = ZonedDateTime.parse(bookedSession.startDateTime, formatter)
                    val bookedDate = bookedStartDateTime.toLocalDate()
                    val bookedTime = bookedStartDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                    if (bookedDate == timeSlot.date && bookedTime == timeSlot.startTime) {
                        isBooked = true
                        break
                    }
                } catch (e: Exception) {
                    Log.e("excludeBookedTimeSlots", "Error parsing date: ${e.message}")
                }
            }
            if (!isBooked) {
                availableTimeSlots.add(timeSlot)
            }
        }
        return availableTimeSlots
    }


}
