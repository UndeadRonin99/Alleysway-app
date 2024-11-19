// Package declaration for the app
package com.techtitans.alleysway

// Necessary Android and Firebase imports
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.techtitans.alleysway.data.CalendarDay
import com.techtitans.alleysway.data.MonthData
import com.techtitans.alleysway.models.LeaderboardEntry
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

// AppCompatActivity class for the workout and calendar UI
class Workouts : AppCompatActivity() {

    private lateinit var database: DatabaseReference // Reference to Firebase database
    private lateinit var recyclerView: RecyclerView // Main RecyclerView for calendar
    private lateinit var monthHeaderRecyclerView: RecyclerView // RecyclerView for month headers
    private lateinit var adapter: CalendarAdapter // Adapter for the main calendar
    private val attendanceData = mutableMapOf<LocalDate, Int>() // Stores attendance data
    private var isAttendanceMarked = false // Tracks if today's attendance has been marked

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Setup for edge-to-edge UI
        setContentView(R.layout.activity_workouts) // Set layout for the activity

        database = FirebaseDatabase.getInstance().reference // Initialize database reference

        // Set onClickListeners for various UI components
        setButtonListeners()

        // Load top 3 leaderboard entries from Firebase
        loadTopThreeUsers()

        // Setup for navigating to the full leaderboard
        findViewById<LinearLayout>(R.id.leaderboard_card).setOnClickListener {
            val intent = Intent(this, Leaderboard::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerViews for calendar and month headers
        monthHeaderRecyclerView = findViewById(R.id.monthHeaderRecyclerView)
        recyclerView = findViewById(R.id.calendarRecyclerView)
        monthHeaderRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = GridLayoutManager(this, 7, RecyclerView.HORIZONTAL, false)

        // Setup month headers using predefined month data
        val months = listOf(
            MonthData("Jan", 31), MonthData("Feb", 28), MonthData("Mar", 31),
            MonthData("Apr", 30), MonthData("May", 31), MonthData("Jun", 30),
            MonthData("Jul", 31), MonthData("Aug", 31), MonthData("Sep", 30),
            MonthData("Oct", 31), MonthData("Nov", 30), MonthData("Dec", 31)
        )
        val monthAdapter = MonthHeaderAdapter(months)
        monthHeaderRecyclerView.adapter = monthAdapter

        // Fetch user ID and attendance data if logged in
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            fetchAttendanceData(userId)
        }

        // Synchronize scrolling between month headers and calendar
        synchronizeScrolling(monthHeaderRecyclerView, recyclerView)
    }

    // Method to synchronize scrolling between two RecyclerViews
    private fun synchronizeScrolling(headerView: RecyclerView, mainView: RecyclerView) {
        headerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                mainView.scrollBy(dx, dy) // Scroll main view when header is scrolled
            }
        })

        mainView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                headerView.scrollBy(dx, dy) // Scroll header when main view is scrolled
            }
        })
    }

    // Method to set up the calendar based on attendance data
    private fun setupCalendar() {
        val startMonth = YearMonth.now().minusMonths(6) // Define the start month
        val endMonth = YearMonth.now() // Define the end month
        val startDate = startMonth.atDay(1) // First day of the start month
        val endDate = endMonth.atEndOfMonth() // Last day of the end month
        val days = generateCalendarDays(startDate, endDate, attendanceData) // Generate calendar days
        adapter = CalendarAdapter(days) // Initialize adapter with days
        recyclerView.adapter = adapter // Set adapter to RecyclerView
    }

    // Fetch attendance data from Firebase
    private fun fetchAttendanceData(userId: String) {
        val attendanceRef = database.child("users").child(userId).child("attendance")
        val todayDate = LocalDate.now().toString() // Format today's date

        // Add value event listener to the attendance reference
        attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { childSnapshot ->
                    val dateStr = childSnapshot.key // Get date string key
                    val value = childSnapshot.getValue(Boolean::class.java) // Get attendance value
                    if (dateStr != null && value == true) {
                        val date = LocalDate.parse(dateStr)
                        attendanceData[date] = attendanceData.getOrDefault(date, 0) + 1 // Increment attendance count
                        if (dateStr == todayDate) {
                            isAttendanceMarked = true // Check if today's attendance is marked
                        }
                    }
                }
                setupCalendar() // Setup the calendar after fetching data
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
    }

    // Generate calendar days within a given range including attendance counts
    private fun generateCalendarDays(
        startDate: LocalDate,
        endDate: LocalDate,
        attendanceData: Map<LocalDate, Int>
    ): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val today = LocalDate.now() // Current date
        val finalDate = if (endDate.isBefore(today)) endDate else today // Last relevant date

        // Adjust the start date to the previous or same Sunday for full week display
        var currentDate = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

        while (currentDate <= finalDate) {
            val attendanceCount = attendanceData.getOrDefault(currentDate, 0) // Get attendance count
            days.add(CalendarDay(date = currentDate, attendanceCount = attendanceCount)) // Add day with count
            currentDate = currentDate.plusDays(1) // Increment date
        }
        return days // Return the list of days
    }

    // Setup button listeners for various UI components
    private fun setButtonListeners() {
        val btnLog = findViewById<Button>(R.id.btnLog)
        val btnPastWorkouts = findViewById<Button>(R.id.btnPastWorkouts)
        val btnViewExercises = findViewById<Button>(R.id.btnViewExcercises)
        val btnWorkout = findViewById<ImageView>(R.id.btnWorkout)
        val btnBooking = findViewById<ImageView>(R.id.btnBooking)
        val btnTracker = findViewById<ImageView>(R.id.btnTracker)
        val btnCamera = findViewById<ImageView>(R.id.btnCamera)
        val btnHome = findViewById<ImageView>(R.id.btnHome)

        // Listener for past workouts button to open PastWorkoutsActivity
        btnPastWorkouts.setOnClickListener {
            startActivity(Intent(this, PastWorkoutsActivity::class.java))
        }

        // Listener for other buttons to start respective activities
        btnCamera.setOnClickListener { startActivity(Intent(this, ScanQRCode::class.java)) }
        btnBooking.setOnClickListener { startActivity(Intent(this, Bookings::class.java)) }
        btnTracker.setOnClickListener { startActivity(Intent(this, Tracker::class.java)) }
        btnHome.setOnClickListener { startActivity(Intent(this, HomePage::class.java)) }

        // Conditional listener for logging workout based on attendance marking
        btnLog.setOnClickListener {
            if(isAttendanceMarked){
                startActivity(Intent(this, log_workout::class.java))
            }else{
                Toast.makeText(this, "Please scan attendance to log workout", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener to view exercises
        btnViewExercises.setOnClickListener {
            startActivity(Intent(this, ViewExcercises::class.java))
        }
    }

    // Load and display the top three users from Firebase for the leaderboard
    private fun loadTopThreeUsers() {
        val usersRef = database.child("users") // Reference to users node in Firebase
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val leaderboardData = mutableListOf<LeaderboardEntry>() // List to hold leaderboard entries

                snapshot.children.forEach { userSnapshot ->
                    val userId = userSnapshot.key ?: continue // Skip if userId is null
                    val participates = userSnapshot.child("participateInLeaderboard").getValue(Boolean::class.java) ?: true // Check if user participates
                    if (!participates) continue // Skip if not participating

                    // Fetch user details
                    val firstName = userSnapshot.child("firstName").value.toString()
                    val profileUrl = userSnapshot.child("profileImageUrl").value.toString()
                    var totalWeight = 0.0
                    var totalReps = 0

                    // Calculate total weight and reps from user's workouts
                    userSnapshot.child("workouts").children.forEach { workoutSnapshot ->
                        val weight = workoutSnapshot.child("totalWeight").value.toString().toDouble()
                        val reps = workoutSnapshot.child("totalReps").value.toString().toInt()
                        totalWeight += weight
                        totalReps += reps
                    }

                    // Add entry to leaderboard data list
                    leaderboardData.add(LeaderboardEntry(userId, firstName, totalWeight, totalReps, profileUrl))
                }

                // Sort leaderboard entries by total weight
                leaderboardData.sortByDescending { it.totalWeight }
                updateTopThreeUI(leaderboardData) // Update UI with top three entries
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        })
    }

    private fun updateTopThreeUI(leaderboardData: List<LeaderboardEntry>) {
    // Update UI for the first place if the leaderboard has at least one entry
    if (leaderboardData.isNotEmpty()) {
        val firstNameView = findViewById<TextView>(R.id.firstName)
        val firstPFP = findViewById<ImageView>(R.id.firstPFP)
        val firstTotalView = findViewById<TextView>(R.id.firstTotal)

        // Set the text for the first place's name and total weight
        firstNameView.text = leaderboardData[0].firstName
        firstTotalView.text = "${leaderboardData[0].totalWeight} kg"

        // Load the profile picture for the first place using Glide
        Glide.with(this)
            .load(leaderboardData[0].profileUrl)
            .placeholder(R.drawable.placeholder_profile)  // Placeholder in case the image fails to load
            .into(firstPFP)
    } else {
        // Optionally clear or set default values if no user is in first place
    }

    // Update UI for the second place if the leaderboard has at least two entries
    if (leaderboardData.size > 1) {
        val secondNameView = findViewById<TextView>(R.id.secondName)
        val secondPFP = findViewById<ImageView>(R.id.secondPFP)
        val secondTotalView = findViewById<TextView>(R.id.secondTotal)

        // Set the text for the second place's name and total weight
        secondNameView.text = leaderboardData[1].firstName
        secondTotalView.text = "${leaderboardData[1].totalWeight} kg"

        // Load the profile picture for the second place using Glide
        Glide.with(this)
            .load(leaderboardData[1].profileUrl)
            .placeholder(R.drawable.placeholder_profile)
            .into(secondPFP)
    } else {
        // Optionally clear or set default values if no user is in second place
    }

    // Update UI for the third place if the leaderboard has at least three entries
    if (leaderboardData.size > 2) {
        val thirdNameView = findViewById<TextView>(R.id.thirdName)
        val thirdPFP = findViewById<ImageView>(R.id.thirdPFP)
        val thirdTotalView = findViewById<TextView>(R.id.thirdTotal)

        // Set the text for the third place's name and total weight
        thirdNameView.text = leaderboardData[2].firstName
        thirdTotalView.text = "${leaderboardData[2].totalWeight} kg"

        // Load the profile picture for the third place using Glide
        Glide.with(this)
            .load(leaderboardData[2].profileUrl)
            .placeholder(R.drawable.placeholder_profile)
            .into(thirdPFP)
    } else {
        // Optionally clear or set default values if no user is in third place
    }
}

}
