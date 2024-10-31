package com.example.alleysway

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.alleysway.models.LeaderboardEntry
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Workouts : AppCompatActivity() {

    private lateinit var database: DatabaseReference

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
    }

    private fun setButtonListeners() {
        // Get references to your buttons and images
        val btnLog = findViewById<Button>(R.id.btnLog)
        val btnStreak = findViewById<Button>(R.id.btnStreak)
        val btnPastWorkouts = findViewById<Button>(R.id.btnPastWorkouts)
        val btnViewExercises = findViewById<Button>(R.id.btnViewExcercises)

        val btnWorkout = findViewById<ImageView>(R.id.btnWorkout)
        val btnBooking = findViewById<ImageView>(R.id.btnBooking)
        val btnTracker = findViewById<ImageView>(R.id.btnTracker)
        val btnCamera = findViewById<ImageView>(R.id.btnCamera)
        val btnHome = findViewById<ImageView>(R.id.btnHome)

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

        btnStreak.setOnClickListener{
            val intent = Intent(this, Attendance::class.java)
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
                        val repsValue = workoutSnapshot.child("totalReps").value
                        val reps = when (repsValue) {
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
