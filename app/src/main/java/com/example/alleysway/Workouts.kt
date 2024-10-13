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

        val btnWorkout = findViewById<ImageView>(R.id.btnWorkout)
        val btnBooking = findViewById<ImageView>(R.id.btnBooking)
        val btnTracker = findViewById<ImageView>(R.id.btnTracker)
        val btnCamera = findViewById<ImageView>(R.id.btnCamera)

        // Set OnClickListeners for each button

        // Log a Workout button
        btnLog.setOnClickListener {
            // Perform the action for logging a workout
            val intent = Intent(this, Stronger_function_page_1::class.java) // Replace with your activity
            startActivity(intent)
        }


        val btnHome: ImageView = findViewById(R.id.btnHome)
        btnHome.setOnClickListener {
            // Navigate to the Bookings activity
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)

        }







    }
}
