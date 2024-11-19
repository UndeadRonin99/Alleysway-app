// Package declaration
package com.techtitans.alleysway

// Import statements
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity for scanning QR codes to mark user attendance.
 */
class ScanQRCode : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qrcode)

        // Initialize QR code scanner
        val integrator = IntentIntegrator(this)
        integrator.setPrompt("Scan the gym QR code to mark attendance")
        integrator.setCameraId(0)
        integrator.setOrientationLocked(true)
        integrator.setBeepEnabled(true)
        integrator.captureActivity = CustomCaptureActivity::class.java
        integrator.initiateScan()
    }

    /**
     * Handles the result of the QR code scan.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                // User cancelled the scan
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show()
                finish()
            } else {
                val scannedData = result.contents
                val expectedData = "https://yourapp.com/attendance_checkin"  // Expected QR code data

                if (scannedData == expectedData) {
                    // Valid QR code scanned
                    updateAttendance()
                    updatePublicAttendance()
                    Toast.makeText(this, "Attendance Marked Successfully", Toast.LENGTH_SHORT).show()
                    scheduleDelayedNotification("You can now log a workout and don't forget to record your weight.")
                    finish()
                } else {
                    // Invalid QR code
                    Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Updates the user's attendance in Firebase.
     */
    private fun updateAttendance() {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("attendance")
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Mark attendance for today
        databaseReference.child(todayDate).setValue(true)
            .addOnSuccessListener {
                // Attendance marked successfully
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to Mark Attendance", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Updates the public attendance count in Firebase.
     */
    private fun updatePublicAttendance() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("attendance")
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date())

        // Increment attendance count for the current hour
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
                if (!committed) {
                    Toast.makeText(this@ScanQRCode, "Failed to Update Public Attendance", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Schedules a delayed notification to remind the user to log their workout.
     *
     * @param message The message to display in the notification.
     */
    private fun scheduleDelayedNotification(message: String) {
        val delayInMillis = 15 * 1000L // 15 seconds

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("MEAL_TITLE", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the notification
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + delayInMillis,
            pendingIntent
        )
    }
}
