package com.example.alleysway

import android.os.Bundle

import com.example.alleysway.models.LeaderboardEntry

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class Leaderboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        // Back button functionality
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()  // Close the leaderboard activity
        }

        // Fetch leaderboard data from Firebase
        fetchLeaderboardData()
    }

    private fun fetchLeaderboardData() {
        val database = FirebaseDatabase.getInstance().getReference("users")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val leaderboardData = mutableListOf<LeaderboardEntry>()

                for (userSnapshot in snapshot.children) {
                    var totalWeight = 0.0
                    val firstName = userSnapshot.child("firstName").getValue(String::class.java) ?: "Unknown"
                    val profileUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java) ?: ""
                    val workoutsSnapshot = userSnapshot.child("workouts")

                    for (workoutSnapshot in workoutsSnapshot.children) {
                        val workoutWeight = workoutSnapshot.child("totalWeight").getValue(Double::class.java) ?: 0.0
                        totalWeight += workoutWeight
                    }

                    leaderboardData.add(LeaderboardEntry(firstName, totalWeight, profileUrl))
                }

                leaderboardData.sortByDescending { it.totalWeight }
                updateLeaderboardUI(leaderboardData)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Leaderboard, "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateLeaderboardUI(leaderboardData: List<LeaderboardEntry>) { if (leaderboardData.isEmpty()) return

        // Update top 3 users outside the scroll view
        if (leaderboardData.size >= 1) {
            val firstNameView = findViewById<TextView>(R.id.firstPlaceName)
            val firstPFP = findViewById<CircleImageView>(R.id.firstPlaceImage)
            val firstPointsView = findViewById<TextView>(R.id.firstPlacePoints)

            firstNameView.text = leaderboardData[0].firstName
            firstPointsView.text = "${leaderboardData[0].totalWeight} kg"
            loadProfileImage(firstPFP, leaderboardData[0].profileUrl)
        }

        if (leaderboardData.size >= 2) {
            val secondNameView = findViewById<TextView>(R.id.secondPlaceName)
            val secondPFP = findViewById<CircleImageView>(R.id.secondPlaceImage)
            val secondPointsView = findViewById<TextView>(R.id.secondPlacePoints)

            secondNameView.text = leaderboardData[1].firstName
            secondPointsView.text = "${leaderboardData[1].totalWeight} kg"
            loadProfileImage(secondPFP, leaderboardData[1].profileUrl)
        }

        if (leaderboardData.size >= 3) {
            val thirdNameView = findViewById<TextView>(R.id.thirdPlaceName)
            val thirdPFP = findViewById<CircleImageView>(R.id.thirdPlaceImage)
            val thirdPointsView = findViewById<TextView>(R.id.thirdPlacePoints)

            thirdNameView.text = leaderboardData[2].firstName
            thirdPointsView.text = "${leaderboardData[2].totalWeight} kg"
            loadProfileImage(thirdPFP, leaderboardData[2].profileUrl)
        }

        // Populate other users in the scroll view
        val leaderboardContainer = findViewById<LinearLayout>(R.id.leaderboardContainer)
        leaderboardContainer.removeAllViews()

        for (i in leaderboardData.indices) {
            val entry = leaderboardData[i]
            val leaderboardItem = layoutInflater.inflate(R.layout.leaderboard_item, null)

            // Set rank, name, points, and profile image
            val rankView = leaderboardItem.findViewById<TextView>(R.id.rank)
            val nameView = leaderboardItem.findViewById<TextView>(R.id.name)
            val pointsView = leaderboardItem.findViewById<TextView>(R.id.points)
            val profileImageView = leaderboardItem.findViewById<CircleImageView>(R.id.profileImage)
            val rankIndicator = leaderboardItem.findViewById<TextView>(R.id.rank_indicator)

            rankView.text = "${i + 1}"
            nameView.text = entry.firstName
            pointsView.text = "${entry.totalWeight} kg"
            loadProfileImage(profileImageView, entry.profileUrl)

            // Set the background based on the rank
            val backgroundDrawable = when (i) {
                0 -> R.drawable.rounded_gold_background
                1 -> R.drawable.rounded_silver_background
                2 -> R.drawable.rounded_bronze_background
                else -> R.drawable.rounded_default_background
            }
            leaderboardItem.background = resources.getDrawable(backgroundDrawable, null)

            // Set the rank indicator (▲ for up, ▼ for down, and color it green/red)
            if (i % 2 == 0) {
                // Example for an increase in rank
                rankIndicator.text = "▲"
                rankIndicator.setTextColor(resources.getColor(R.color.green, null)) // Green color
            } else {
                // Example for a decrease in rank
                rankIndicator.text = "▼"
                rankIndicator.setTextColor(resources.getColor(R.color.red, null)) // Red color
            }

            // Add the leaderboard item to the container
            leaderboardContainer.addView(leaderboardItem)
        }
    }

    private fun loadProfileImage(imageView: CircleImageView, profileUrl: String) {
        if (profileUrl.isNotEmpty()) {
            Glide.with(this)
                .load(profileUrl)
                .placeholder(R.drawable.placeholder_profile)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.placeholder_profile)
        }
    }

}
