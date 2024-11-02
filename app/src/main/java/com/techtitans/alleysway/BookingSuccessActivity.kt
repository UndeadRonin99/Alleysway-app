package com.techtitans.alleysway

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class BookingSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_success)

        val doneButton: Button = findViewById(R.id.done_button)
        doneButton.setOnClickListener {
            // Navigate to the home page or other target page
            val intent = Intent(this, Bookings::class.java)
            startActivity(intent)
            finish()
        }
    }
}
