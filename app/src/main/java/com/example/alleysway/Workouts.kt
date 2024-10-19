package com.example.alleysway

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Workouts : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workouts)

        // Get references to your buttons and images
        val btnLog = findViewById<Button>(R.id.btnLog)
        val btnStreak = findViewById<Button>(R.id.btnStreak)
        val btnPastWorkouts = findViewById<Button>(R.id.btnPastWorkouts)
        val btnViewExcercises = findViewById<Button>(R.id.btnViewExcercises)

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
        btnBooking.setOnClickListener{
            val intent = Intent(this, Bookings::class.java)
            startActivity(intent)
        }
        btnTracker.setOnClickListener{
            val intent = Intent(this, Tracker::class.java)
            startActivity(intent)
        }
        btnHome.setOnClickListener{
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }
        // Log a Workout button
        btnLog.setOnClickListener {
            // Perform the action for logging a workout
            val intent = Intent(this, log_workout::class.java)
            startActivity(intent)
        }
        // View Excercises button
        btnViewExcercises.setOnClickListener {
            // Perform the action for viewing excercises
            val intent = Intent(this, ViewExcercises::class.java)
            startActivity(intent)
        }
    }
}
