package com.example.alleysway

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alleysway.adapter.SelectableTimeSlotAdapter
import com.example.alleysway.model.Day
import com.example.alleysway.model.TimeSlot
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_finalize_bookings)

        val trainerName = intent.getStringExtra("trainerName")
        trainerID = intent.getStringExtra("trainerID")

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
