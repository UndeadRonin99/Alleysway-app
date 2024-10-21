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

    private fun updateLeaderboardUI(leaderboardData: List<LeaderboardEntry>) {
        if (leaderboardData.isEmpty()) return

        // First Place
        if (leaderboardData.size >= 1) {
            val firstNameView = findViewById<TextView>(R.id.firstPlaceName)
            val firstPFP = findViewById<CircleImageView>(R.id.firstPlaceImage)
            val firstPointsView = findViewById<TextView>(R.id.firstPlacePoints)
            firstNameView.text = leaderboardData[0].firstName
            firstPointsView.text = "${leaderboardData[0].totalWeight} kg" // Display total weight
            loadProfileImage(firstPFP, leaderboardData[0].profileUrl)
        }

        // Second Place
        if (leaderboardData.size >= 2) {
            val secondNameView = findViewById<TextView>(R.id.secondPlaceName)
            val secondPFP = findViewById<CircleImageView>(R.id.secondPlaceImage)
            val secondPointsView = findViewById<TextView>(R.id.secondPlacePoints)
            secondNameView.text = leaderboardData[1].firstName
            secondPointsView.text = "${leaderboardData[1].totalWeight} kg" // Display total weight
            loadProfileImage(secondPFP, leaderboardData[1].profileUrl)
        }

        // Third Place
        if (leaderboardData.size >= 3) {
            val thirdNameView = findViewById<TextView>(R.id.thirdPlaceName)
            val thirdPFP = findViewById<CircleImageView>(R.id.thirdPlaceImage)
            val thirdPointsView = findViewById<TextView>(R.id.thirdPlacePoints)
            thirdNameView.text = leaderboardData[2].firstName
            thirdPointsView.text = "${leaderboardData[2].totalWeight} kg" // Display total weight
            loadProfileImage(thirdPFP, leaderboardData[2].profileUrl)

        }
        // Display users beyond the top 3
        val leaderboardContainer = findViewById<LinearLayout>(R.id.leaderboardContainer)
        leaderboardContainer.removeAllViews()

        for (i in 3 until leaderboardData.size) {
            val entry = leaderboardData[i]
            val leaderboardItem = layoutInflater.inflate(R.layout.leaderboard_item, null)

            // Set the rank, name, and total weight for each user
            val rankView = leaderboardItem.findViewById<TextView>(R.id.rank)
            val nameView = leaderboardItem.findViewById<TextView>(R.id.name)
            val pointsView = leaderboardItem.findViewById<TextView>(R.id.points)
            val profileImageView = leaderboardItem.findViewById<CircleImageView>(R.id.profileImage)

            rankView.text = "${i + 1}" // Ranking
            nameView.text = entry.firstName
            pointsView.text = "${entry.totalWeight} kg" // Display total weight

            // Load profile image
            loadProfileImage(profileImageView, entry.profileUrl)

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
