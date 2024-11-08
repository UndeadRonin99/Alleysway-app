package com.techtitans.alleysway

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanQRCode : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qrcode)

        // Initiate QR code scanning
        val integrator = IntentIntegrator(this)
        integrator.setPrompt("Scan the gym QR code to mark attendance")
        integrator.setCameraId(0)
        integrator.setOrientationLocked(true)
        integrator.setBeepEnabled(true)
        integrator.captureActivity = CustomCaptureActivity::class.java
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show()
                this.finish()
            } else {
                val scannedData = result.contents
                val expectedData = "https://yourapp.com/attendance_checkin"  // The data embedded in your QR code

                // Verify if the scanned data is correct
                if (scannedData == expectedData) {
                    updateAttendance()
                    updatePublicAttendance()
                    Toast.makeText(this, "Attendance logged", Toast.LENGTH_SHORT).show()
                    scheduleDelayedNotification("You can now log a workout and don't forget to record your weight.") // You can customize the title as needed
                    this.finish()
                } else {
                    Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show()
                    this.finish()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun updateAttendance() {
        // Get the currently logged-in user's ID
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return

        // Reference to the attendance node for the user in Firebase
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("attendance")

        // Get today's date in the desired format
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Mark attendance for the current date
        databaseReference.child(todayDate).setValue(true)
            .addOnSuccessListener {
                Toast.makeText(this, "Attendance Marked Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to Mark Attendance", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePublicAttendance() {

        // Reference to the attendance node for the user in Firebase
        val databaseReference = FirebaseDatabase.getInstance().getReference("attendance")

        // Get today's date in the desired format
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date()) // Get the hour of check-in

        // Increment the counter for the current hour
        val hourlyAttendanceRef = databaseReference.child(todayDate).child(currentHour)
        hourlyAttendanceRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentCount = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentCount + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed) {
                    Toast.makeText(this@ScanQRCode, "Attendance Marked Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ScanQRCode, "Failed to Mark Attendance", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun scheduleDelayedNotification(Message: String) {
        val delayInMillis = 15 * 1000L // 2 minutes in milliseconds

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("MEAL_TITLE", Message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the notification to trigger 2 minutes after the current time
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + delayInMillis,
            pendingIntent
        )
    }


}