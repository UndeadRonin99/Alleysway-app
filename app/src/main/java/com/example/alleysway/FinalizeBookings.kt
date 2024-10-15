package com.example.alleysway

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
import com.example.alleysway.adapter.SelectableTimeSlotAdapter
import com.example.alleysway.model.Day
import com.example.alleysway.model.TimeSlot
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
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
    private lateinit var btnBookNow: Button



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_finalize_bookings)

        trainerName = intent.getStringExtra("trainerName")
        trainerID = intent.getStringExtra("trainerID")
        trainerEmail = intent.getStringExtra("trainerEmail")

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
        var UserName:String? = null
        getFullName { fullName ->
            if (fullName != null) {
                Log.d("FinalizeBookings", "User's full name: $fullName")
                UserName = fullName
            } else {
                Log.e("FinalizeBookings", "Failed to retrieve user's full name.")
                Toast.makeText(this, "Could not retrieve full name.", Toast.LENGTH_SHORT).show()
            }
        }
        if (account != null) {
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
                        val event = Event().apply {
                            summary = "Training Session with $trainerName"
                            description = "Booked by $UserName"
                            start = EventDateTime().apply {
                                dateTime = getDateTime(session.day, session.startTime)
                                timeZone = "Africa/Johannesburg"
                            }
                            end = EventDateTime().apply {
                                dateTime = getDateTime(session.day, session.endTime)
                                timeZone = "Africa/Johannesburg"
                            }
                            attendees = listOf(EventAttendee().setEmail(trainerEmail))
                        }

                        val eventResult = service.events().insert("primary", event).execute()
                        Log.d("CalendarEvent", "Event created: ${eventResult.htmlLink}")
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDateTime(dayOfWeekStr: String, time: String): com.google.api.client.util.DateTime {
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
        val zonedDateTime = localDateTime.atZone(zoneId)
        return com.google.api.client.util.DateTime(zonedDateTime.toInstant().toEpochMilli())
    }


    private fun fetchAdminTimeSlots() {
        if (trainerID == null) {
            Log.e("FinalizeBookings", "Trainer ID is null, cannot fetch timeslots")
            return
        }

        val trainerRef = database.child("users").child(trainerID!!)
        trainerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                timeSlotsList.clear()
                val role = snapshot.child("role").getValue(String::class.java)
                if (role == "admin") {
                    val daysSnapshot = snapshot.child("Days")
                    for (daySnapshot in daysSnapshot.children) {
                        val day = daySnapshot.key ?: continue
                        val timeSlotsSnapshot = daySnapshot.child("TimeSlots")
                        val dayTimeSlots = mutableListOf<TimeSlot>()
                        for (timeSlotSnapshot in timeSlotsSnapshot.children) {
                            val startTime = timeSlotSnapshot.child("StartTime").getValue(String::class.java)
                            val endTime = timeSlotSnapshot.child("EndTime").getValue(String::class.java)
                            if (startTime != null && endTime != null) {
                                splitTimeSlotIntoSessions(day, startTime, endTime, dayTimeSlots)
                            }
                        }
                        if (dayTimeSlots.isNotEmpty()) {
                            timeSlotsList.add(Day(day, dayTimeSlots))
                        }
                    }
                }
                timeSlotAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FinalizeBookings", "Failed to fetch timeslots: ${error.message}")
            }
        })
    }

    private fun splitTimeSlotIntoSessions(day: String, startTime: String, endTime: String, dayTimeSlots: MutableList<TimeSlot>) {
        val startHour = startTime.split(":")[0].toInt()
        val endHour = endTime.split(":")[0].toInt()

        for (hour in startHour until endHour) {
            val sessionStartTime = String.format("%02d:00", hour)
            val sessionEndTime = String.format("%02d:00", hour + 1)
            val timeSlot = TimeSlot(day, sessionStartTime, sessionEndTime)
            dayTimeSlots.add(timeSlot)
        }
    }
}
