package com.techtitans.alleysway

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

// This activity handles the success screen displayed after a booking is completed.
class BookingSuccessActivity : AppCompatActivity() {

    // The onCreate method initializes the activity and sets up the UI and button behavior.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_success)
// Find the "Done" button in the layout by its ID.
        val doneButton: Button = findViewById(R.id.done_button)

        // Set a click listener on the button to handle user interactions.
        doneButton.setOnClickListener {
            // Navigate to the home page or other target page
             // Create an intent to navigate to the Bookings activity.
            val intent = Intent(this, Bookings::class.java)
            startActivity(intent)// Start the Bookings activity.
            finish()// Finish the current activity to remove it from the back stack.
        }
    }
}
