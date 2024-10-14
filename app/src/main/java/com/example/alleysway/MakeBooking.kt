package com.example.alleysway

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

class MakeBooking : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var trainerContainer: LinearLayout
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_make_booking)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance("https://alleysway-310a8-default-rtdb.firebaseio.com/").reference
        trainerContainer = findViewById(R.id.trainer_list_container)
        btnBack = findViewById(R.id.back_arrow)

        btnBack.setOnClickListener {
            finish()
        }


        // Fetch and display trainers
        fetchTrainers()
    }

    private fun fetchTrainers() {
        val usersRef = database.child("users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val trainerID = userSnapshot.key ?: continue
                    val role = userSnapshot.child("role").getValue(String::class.java)
                    if (role == "admin") {
                        val trainerName = userSnapshot.child("firstName").getValue(String::class.java) ?: "Unknown"
                        val profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java)
                        val rate = userSnapshot.child("rate").getValue(String::class.java) ?: "N/A"

                        addTrainerView(trainerName, profileImageUrl, rate, trainerID)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "loadUser:onCancelled", error.toException())
            }
        })
    }


    private fun addTrainerView(name: String, profileImageUrl: String?, rate: String, trainerID: String?) {
        val trainerItem = LayoutInflater.from(this).inflate(R.layout.trainer_item_layout, trainerContainer, false)

        val trainerNameTextView: TextView = trainerItem.findViewById(R.id.trainer_name)
        val trainerImageView: ImageView = trainerItem.findViewById(R.id.trainer_image)

        trainerNameTextView.text = "$name - $rate/hour"

        // Load and convert the image using Coil
        if (profileImageUrl != null) {
            lifecycleScope.launch {
                loadImageWithCoilAndGlide(profileImageUrl, trainerImageView)
            }
        } else {
            trainerImageView.setImageResource(R.drawable.dft)
        }

        // Set click listener to navigate to the next page
        trainerItem.setOnClickListener {
            val intent = Intent(this, FinalizeBookings::class.java)
            intent.putExtra("trainerName", name)
            intent.putExtra("profileImageUrl", profileImageUrl)
            intent.putExtra("rate", rate)
            intent.putExtra("trainerID", trainerID)
            startActivity(intent)
        }

        trainerContainer.addView(trainerItem)
    }

    private suspend fun loadImageWithCoilAndGlide(url: String, imageView: ImageView) {
        val imageLoader = ImageLoader(this)
        val request = ImageRequest.Builder(this)
            .data(url)
            .build()

        val result = (imageLoader.execute(request) as? SuccessResult)?.drawable
        if (result != null) {
            val bitmap = (result as BitmapDrawable).bitmap
            // Save bitmap as JPEG
            val jpegFile = File(cacheDir, "temp_${System.currentTimeMillis()}.jpg")
            FileOutputStream(jpegFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            // Load the JPEG with Glide
            Glide.with(this)
                .load(jpegFile)
                .circleCrop() // Applies circular transformation
                .error(R.drawable.dft)
                .into(imageView)
        } else {
            // If Coil fails, set default image
            imageView.setImageResource(R.drawable.dft)
        }
    }

}