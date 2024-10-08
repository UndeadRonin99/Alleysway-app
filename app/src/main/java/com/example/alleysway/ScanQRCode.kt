package com.example.alleysway

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
                    Toast.makeText(this, "Attendance logged", Toast.LENGTH_SHORT).show()
                    this.finish()
                } else {
                    Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show()
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
}