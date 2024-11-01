package com.example.alleysway

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alleysway.data.CalendarDay
import com.example.alleysway.data.MonthData
import com.example.alleysway.models.LeaderboardEntry
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

class Workouts : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var monthHeaderRecyclerView: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private val attendanceData = mutableMapOf<LocalDate, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workouts)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Set OnClickListeners for various buttons
        setButtonListeners()

        // Load the top 3 users from Firebase
        loadTopThreeUsers()

        // Set OnClickListener for leaderboard card to go to the full leaderboard
        findViewById<LinearLayout>(R.id.leaderboard_card).setOnClickListener {
            val intent = Intent(this, Leaderboard::class.java)
            startActivity(intent)
        }
        monthHeaderRecyclerView = findViewById(R.id.monthHeaderRecyclerView)
        recyclerView = findViewById(R.id.calendarRecyclerView)

        monthHeaderRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val layoutManager = GridLayoutManager(this, 7, RecyclerView.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        // Set up monthHeaderRecyclerView
        val months = listOf(
            MonthData("Jan", 31),
            MonthData("Feb", 28), // or 29 for leap years
            MonthData("Mar", 31),
            MonthData("Apr", 30),
            MonthData("May", 31),
            MonthData("Jun", 30),
            MonthData("Jul", 31),
            MonthData("Aug", 31),
            MonthData("Sep", 30),
            MonthData("Oct", 31),
            MonthData("Nov", 30),
            MonthData("Dec", 31)
        )
        val monthAdapter = MonthHeaderAdapter(months)
        monthHeaderRecyclerView.adapter = monthAdapter

        // Fetch attendance data and set up the calendar
        val userId = Firebase.auth.currentUser?.uid

        if (userId != null) {
            fetchAttendanceData(userId)
        }

        // Synchronize scrolling
        synchronizeScrolling(monthHeaderRecyclerView, recyclerView)
    }

    private fun synchronizeScrolling(headerView: RecyclerView, mainView: RecyclerView) {
        headerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                mainView.scrollBy(dx, dy)
            }
        })

        mainView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                headerView.scrollBy(dx, dy)
            }
        })
    }

    private fun setupCalendar() {
        val startMonth = YearMonth.now().minusMonths(6) // Adjust as needed
        val endMonth = YearMonth.now()

        val startDate = startMonth.atDay(1)
        val endDate = endMonth.atEndOfMonth()

        val days = generateCalendarDays(startDate, endDate, attendanceData)

        adapter = CalendarAdapter(days)
        recyclerView.adapter = adapter
    }

    private fun fetchAttendanceData(userId: String) {
        val database = FirebaseDatabase.getInstance().reference
        val attendanceRef = database.child("users").child(userId).child("attendance")

        attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val dateStr = childSnapshot.key
                    val value = childSnapshot.getValue(Boolean::class.java)
                    if (dateStr != null && value == true) {
                        val date = LocalDate.parse(dateStr)
                        attendanceData[date] = attendanceData.getOrDefault(date, 0) + 1
                    }
                }
                // After fetching data, set up the calendar
                Log.d("AttendanceActivity", "Attendance Data: $attendanceData")
                setupCalendar()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun generateCalendarDays(
        startDate: LocalDate,
        endDate: LocalDate,
        attendanceData: Map<LocalDate, Int>
    ): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()

        // Define today as the last relevant date
        val today = LocalDate.now()
        val finalDate = if (endDate.isBefore(today)) endDate else today

        // Adjust the start date to align with full weeks (Sunday to Saturday)
        val currentStartDate = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        var currentDate = currentStartDate

        while (currentDate <= finalDate) {
            if (currentDate.isBefore(startDate) || currentDate.isAfter(finalDate)) {
                days.add(CalendarDay(date = null)) // Empty day slot
            } else {
                val attendanceCount = attendanceData.getOrDefault(currentDate, 0)
                days.add(CalendarDay(date = currentDate, attendanceCount = attendanceCount))
            }
            currentDate = currentDate.plusDays(1)
        }
        return days
    }

    private fun setButtonListeners() {
        // Get references to your buttons and images
        val btnLog = findViewById<Button>(R.id.btnLog)
        val btnPastWorkouts = findViewById<Button>(R.id.btnPastWorkouts)
        val btnViewExercises = findViewById<Button>(R.id.btnViewExcercises)

        val btnWorkout = findViewById<ImageView>(R.id.btnWorkout)
        val btnBooking = findViewById<ImageView>(R.id.btnBooking)
        val btnTracker = findViewById<ImageView>(R.id.btnTracker)
        val btnCamera = findViewById<ImageView>(R.id.btnCamera)
        val btnHome = findViewById<ImageView>(R.id.btnHome)

        btnPastWorkouts.setOnClickListener {
            val intent = Intent(this, PastWorkoutsActivity::class.java)
            startActivity(intent)
        }
        // Set OnClickListeners for each button
        btnCamera.setOnClickListener {
            val intent = Intent(this, ScanQRCode::class.java)
            startActivity(intent)
        }
        btnBooking.setOnClickListener {
            val intent = Intent(this, Bookings::class.java)
            startActivity(intent)
        }
        btnTracker.setOnClickListener {
            val intent = Intent(this, Tracker::class.java)
            startActivity(intent)
        }
        btnHome.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        // Log a Workout button
        btnLog.setOnClickListener {
            val intent = Intent(this, log_workout::class.java)
            startActivity(intent)
        }

        // View Exercises button
        btnViewExercises.setOnClickListener {
            val intent = Intent(this, ViewExcercises::class.java)
            startActivity(intent)
        }
    }


    private fun loadTopThreeUsers() {
        val usersRef = database.child("users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val leaderboardData = mutableListOf<LeaderboardEntry>()

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    var totalWeight = 0.0
                    var totalReps = 0

                    // Fetch firstName
                    val firstNameValue = userSnapshot.child("firstName").value
                    val firstName = when (firstNameValue) {
                        is String -> firstNameValue
                        else -> "Unknown"
                    }

                    // Fetch profileUrl
                    val profileUrlValue = userSnapshot.child("profileImageUrl").value
                    val profileUrl = when (profileUrlValue) {
                        is String -> profileUrlValue
                        else -> ""
                    }

                    val workoutsSnapshot = userSnapshot.child("workouts")

                    for (workoutSnapshot in workoutsSnapshot.children) {
                        // Fetch totalWeight
                        val weightValue = workoutSnapshot.child("totalWeight").value
                        val weight = when (weightValue) {
                            is Double -> weightValue
                            is Long -> weightValue.toDouble()
                            is Int -> weightValue.toDouble()
                            is String -> weightValue.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }

                        // Fetch totalReps
                        val reps = when (val repsValue = workoutSnapshot.child("totalReps").value) {
                            is Int -> repsValue
                            is Long -> repsValue.toInt()
                            is String -> repsValue.toIntOrNull() ?: 0
                            is Double -> repsValue.toInt()
                            else -> 0
                        }

                        totalWeight += weight
                        totalReps += reps
                    }

                    // Create LeaderboardEntry with correct parameters
                    leaderboardData.add(LeaderboardEntry(
                        userId = userId,
                        firstName = firstName,
                        totalWeight = totalWeight,
                        totalReps = totalReps,
                        profileUrl = profileUrl
                    ))
                }

                leaderboardData.sortByDescending { it.totalWeight }

                updateTopThreeUI(leaderboardData)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateTopThreeUI(leaderboardData: List<LeaderboardEntry>) {
        if (leaderboardData.isNotEmpty()) {
            // First Place
            val firstNameView = findViewById<TextView>(R.id.firstName)
            val firstPFP = findViewById<ImageView>(R.id.firstPFP)
            val firstTotalView = findViewById<TextView>(R.id.firstTotal)

            firstNameView.text = leaderboardData[0].firstName
            firstTotalView.text = "${leaderboardData[0].totalWeight} kg" // Set total weight for first place
            Glide.with(this)
                .load(leaderboardData[0].profileUrl)
                .placeholder(R.drawable.placeholder_profile) // Set placeholder
                .into(firstPFP)

            // Second Place
            if (leaderboardData.size > 1) {
                val secondNameView = findViewById<TextView>(R.id.secondName)
                val secondPFP = findViewById<ImageView>(R.id.secondPFP)
                val secondTotalView = findViewById<TextView>(R.id.secondTotal)

                secondNameView.text = leaderboardData[1].firstName
                secondTotalView.text = "${leaderboardData[1].totalWeight} kg" // Set total weight for second place
                Glide.with(this)
                    .load(leaderboardData[1].profileUrl)
                    .placeholder(R.drawable.placeholder_profile) // Set placeholder
                    .into(secondPFP)
            }

            // Third Place
            if (leaderboardData.size > 2) {
                val thirdNameView = findViewById<TextView>(R.id.thirdName)
                val thirdPFP = findViewById<ImageView>(R.id.thirdPFP)
                val thirdTotalView = findViewById<TextView>(R.id.thirdTotal)

                thirdNameView.text = leaderboardData[2].firstName
                thirdTotalView.text = "${leaderboardData[2].totalWeight} kg" // Set total weight for third place
                Glide.with(this)
                    .load(leaderboardData[2].profileUrl)
                    .placeholder(R.drawable.placeholder_profile) // Set placeholder
                    .into(thirdPFP)
            }
        }
    }
}
