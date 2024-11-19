// File: app/src/main/java/com/example/alleysway/MakeBooking.kt
package com.techtitans.alleysway

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Activity for making bookings with trainers.
 */
class MakeBooking : AppCompatActivity() {

    // Reference to Firebase Realtime Database
    private lateinit var database: DatabaseReference
    // Container layout to hold trainer views
    private lateinit var trainerContainer: LinearLayout
    // Back button to navigate to the previous screen
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display for a modern UI
        setContentView(R.layout.activity_make_booking)

        // Initialize Firebase database reference with the specified URL
        database = FirebaseDatabase.getInstance("https://alleysway-310a8-default-rtdb.firebaseio.com/").reference
        // Find the container layout in the layout file
        trainerContainer = findViewById(R.id.trainer_list_container)
        // Find the back button in the layout
        btnBack = findViewById(R.id.back_arrow)

        // Set click listener for the back button to finish the activity
        btnBack.setOnClickListener {
            finish()
        }

        // Initialize and set up navigation buttons
        val btnScan: ImageView = findViewById(R.id.btnCamera)
        btnScan.setOnClickListener {
            // Navigate to the ScanQRCode activity
            val intent = Intent(this, ScanQRCode::class.java)
            startActivity(intent)
        }

        val btnHome: ImageView = findViewById(R.id.btnHome)
        btnHome.setOnClickListener {
            // Navigate to the HomePage activity
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        val btnWorkout: ImageView = findViewById(R.id.btnWorkout)
        btnWorkout.setOnClickListener {
            // Navigate to the Workouts activity
            val intent = Intent(this, Workouts::class.java)
            startActivity(intent)
        }

        val btnTracker: ImageView = findViewById(R.id.btnTracker)
        btnTracker.setOnClickListener {
            // Navigate to the Tracker activity
            val intent = Intent(this, Tracker::class.java)
            startActivity(intent)
        }

        // Fetch and display the list of trainers
        fetchTrainers()
    }

    /**
     * Fetches trainers with the role "admin" from Firebase and displays them.
     */
    private fun fetchTrainers() {
        val usersRef = database.child("users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Iterate through all users in the "users" node
                for (userSnapshot in snapshot.children) {
                    val trainerID = userSnapshot.key ?: continue
                    val role = userSnapshot.child("role").getValue(String::class.java)
                    // Check if the user has the role "admin"
                    if (role == "admin") {
                        // Retrieve trainer details from the snapshot
                        val trainerName = userSnapshot.child("firstName").getValue(String::class.java) ?: "Unknown"
                        val profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java)
                        val rate = userSnapshot.child("rate").getValue(String::class.java) ?: "N/A"
                        val email = userSnapshot.child("email").getValue(String::class.java) ?: "N/A"

                        // Add the trainer's view to the container
                        addTrainerView(trainerName, profileImageUrl, rate, trainerID, email)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log any errors encountered while fetching data
                Log.w("Firebase", "loadUser:onCancelled", error.toException())
            }
        })
    }

    /**
     * Inflates and adds a trainer view to the container layout.
     *
     * @param name The trainer's name.
     * @param profileImageUrl URL of the trainer's profile image.
     * @param rate The trainer's rate per hour.
     * @param trainerID The unique ID of the trainer.
     * @param email The trainer's email address.
     */
    private fun addTrainerView(name: String, profileImageUrl: String?, rate: String, trainerID: String?, email: String) {
        // Inflate the trainer item layout
        val trainerItem = LayoutInflater.from(this).inflate(R.layout.trainer_item_layout, trainerContainer, false)

        // Find the TextView and ImageView in the inflated layout
        val trainerNameTextView: TextView = trainerItem.findViewById(R.id.trainer_name)
        val trainerImageView: ImageView = trainerItem.findViewById(R.id.trainer_image)

        // Set the trainer's name and rate in the TextView
        trainerNameTextView.text = "$name - $rate/hour"

        // Load and display the trainer's profile image using Coil and Glide
        if (profileImageUrl != null) {
            lifecycleScope.launch {
                loadImageWithCoilAndGlide(profileImageUrl, trainerImageView)
            }
        } else {
            // Set a default image if profileImageUrl is null
            trainerImageView.setImageResource(R.drawable.dft)
        }

        // Set click listener to navigate to FinalizeBookings activity with trainer details
        trainerItem.setOnClickListener {
            val intent = Intent(this, FinalizeBookings::class.java).apply {
                putExtra("trainerName", name)
                putExtra("profileImageUrl", profileImageUrl)
                putExtra("rate", rate)
                putExtra("trainerID", trainerID)
                putExtra("trainerEmail", email)
            }
            startActivity(intent)
        }

        // Add the trainer item view to the container layout
        trainerContainer.addView(trainerItem)
    }

    /**
     * Loads an image using Coil, converts it to a bitmap, saves it as JPEG, and then loads it into the ImageView using Glide.
     *
     * @param url The URL of the image to load.
     * @param imageView The ImageView where the image will be displayed.
     */
    private suspend fun loadImageWithCoilAndGlide(url: String, imageView: ImageView) {
        val imageLoader = ImageLoader(this)
        val request = ImageRequest.Builder(this)
            .data(url)
            .build()

        // Execute the image request with Coil
        val result = (imageLoader.execute(request) as? SuccessResult)?.drawable
        if (result != null) {
            val bitmap = (result as BitmapDrawable).bitmap
            // Save the bitmap as a JPEG file in the cache directory
            val jpegFile = File(cacheDir, "temp_${System.currentTimeMillis()}.jpg")
            FileOutputStream(jpegFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            // Load the JPEG file into the ImageView using Glide with circular cropping
            Glide.with(this)
                .load(jpegFile)
                .circleCrop() // Applies circular transformation to the image
                .error(R.drawable.dft) // Sets a default image in case of an error
                .into(imageView)
        } else {
            // If Coil fails to load the image, set a default image
            imageView.setImageResource(R.drawable.dft)
        }
    }

}
