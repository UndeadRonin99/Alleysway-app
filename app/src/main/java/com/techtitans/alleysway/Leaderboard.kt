package com.techtitans.alleysway

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.techtitans.alleysway.models.LeaderboardEntry
import de.hdodenhof.circleimageview.CircleImageView

class Leaderboard : AppCompatActivity() {

    private var currentFilter = "weight" // Default filter is "Total Weight"
    private lateinit var leaderboardData: MutableList<LeaderboardEntry>
    private var previousLeaderboardData: List<LeaderboardEntry> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        // Back button functionality
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Initialize the leaderboard data list
        leaderboardData = mutableListOf()

        // Fetch leaderboard data from Firebase
        fetchLeaderboardData()

        // Handle filter selection
        val filterRadioGroup = findViewById<RadioGroup>(R.id.filterRadioGroup)
        filterRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioTotalWeight -> {
                    currentFilter = "weight"
                    fetchLeaderboardData()
                }
                R.id.radioTotalReps -> {
                    currentFilter = "reps"
                    fetchLeaderboardData()
                }
            }
        }
    }

    private fun fetchLeaderboardData() {
        val database = FirebaseDatabase.getInstance().getReference("users")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Load previous leaderboard data from SharedPreferences
                loadPreviousLeaderboardData()

                leaderboardData.clear()

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue // Use userId as a unique identifier
                    var totalWeight = 0.0
                    var totalReps = 0
                    val firstName = userSnapshot.child("firstName").getValue(String::class.java) ?: "Unknown"
                    val profileUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java) ?: ""
                    val workoutsSnapshot = userSnapshot.child("workouts")

                    for (workoutSnapshot in workoutsSnapshot.children) {
                        // Fetch totalWeight
                        val workoutWeightValue = workoutSnapshot.child("totalWeight").value
                        val workoutWeight = when (workoutWeightValue) {
                            is Double -> workoutWeightValue
                            is Long -> workoutWeightValue.toDouble()
                            is String -> workoutWeightValue.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }

                        // Fetch totalReps
                        val workoutRepsValue = workoutSnapshot.child("totalReps").value
                        val workoutReps = when (workoutRepsValue) {
                            is Int -> workoutRepsValue
                            is Long -> workoutRepsValue.toInt()
                            is String -> workoutRepsValue.toIntOrNull() ?: 0
                            else -> 0
                        }

                        totalWeight += workoutWeight
                        totalReps += workoutReps
                    }

                    leaderboardData.add(LeaderboardEntry(userId, firstName, totalWeight, totalReps, profileUrl))
                }

                // Sort the leaderboard data
                sortLeaderboardData()

                // Calculate rank changes
                calculateRankChanges()

                // Update the UI with the new rank changes
                updateLeaderboardUI(leaderboardData)

                // Save the current leaderboard data to SharedPreferences
                saveCurrentLeaderboardData()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Leaderboard, "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPreviousLeaderboardData() {
        val sharedPreferences = getSharedPreferences("LeaderboardPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val key = "previousLeaderboardData_$currentFilter"
        val json = sharedPreferences.getString(key, null)
        if (json != null) {
            val type = object : TypeToken<List<LeaderboardEntry>>() {}.type
            previousLeaderboardData = gson.fromJson(json, type)
        } else {
            previousLeaderboardData = listOf()
        }
    }

    private fun saveCurrentLeaderboardData() {
        val sharedPreferences = getSharedPreferences("LeaderboardPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(leaderboardData)
        val key = "previousLeaderboardData_$currentFilter"
        editor.putString(key, json)
        editor.apply()
    }

    private fun calculateRankChanges() {
        val previousRanks = previousLeaderboardData.sortedWith(compareByDescending<LeaderboardEntry> {
            if (currentFilter == "weight") it.totalWeight else it.totalReps.toDouble()
        }).mapIndexed { index, entry ->
            entry.userId to index + 1
        }.toMap()

        leaderboardData.forEachIndexed { index, entry ->
            val currentRank = index + 1
            val previousRank = previousRanks[entry.userId] ?: currentRank
            entry.rankChange = previousRank - currentRank
        }
    }

    private fun sortLeaderboardData() {
        leaderboardData.sortWith(compareByDescending<LeaderboardEntry> {
            if (currentFilter == "weight") it.totalWeight else it.totalReps.toDouble()
        })
    }

    private fun updateLeaderboardUI(leaderboardData: List<LeaderboardEntry>) {

        if (leaderboardData.isEmpty()) return

        // Update top 3 users outside the scroll view
        if (leaderboardData.size >= 1) {
            val firstNameView = findViewById<TextView>(R.id.firstPlaceName)
            val firstPFP = findViewById<CircleImageView>(R.id.firstPlaceImage)
            val firstPointsView = findViewById<TextView>(R.id.firstPlacePoints)

            firstNameView.text = leaderboardData[0].firstName
            firstPointsView.text = if (currentFilter == "weight") {
                "${leaderboardData[0].totalWeight} kg"
            } else {
                "${leaderboardData[0].totalReps} reps"
            }
            loadProfileImage(firstPFP, leaderboardData[0].profileUrl)
        }

        if (leaderboardData.size >= 2) {
            val secondNameView = findViewById<TextView>(R.id.secondPlaceName)
            val secondPFP = findViewById<CircleImageView>(R.id.secondPlaceImage)
            val secondPointsView = findViewById<TextView>(R.id.secondPlacePoints)

            secondNameView.text = leaderboardData[1].firstName
            secondPointsView.text = if (currentFilter == "weight") {
                "${leaderboardData[1].totalWeight} kg"
            } else {
                "${leaderboardData[1].totalReps} reps"
            }
            loadProfileImage(secondPFP, leaderboardData[1].profileUrl)
        }

        if (leaderboardData.size >= 3) {
            val thirdNameView = findViewById<TextView>(R.id.thirdPlaceName)
            val thirdPFP = findViewById<CircleImageView>(R.id.thirdPlaceImage)
            val thirdPointsView = findViewById<TextView>(R.id.thirdPlacePoints)

            thirdNameView.text = leaderboardData[2].firstName
            thirdPointsView.text = if (currentFilter == "weight") {
                "${leaderboardData[2].totalWeight} kg"
            } else {
                "${leaderboardData[2].totalReps} reps"
            }
            loadProfileImage(thirdPFP, leaderboardData[2].profileUrl)
        }

        // **Include all users, including top 3, in the scroll view**
        // Remove or comment out the line that excludes the top 3 users
        // val restOfLeaderboard = if (leaderboardData.size > 3) leaderboardData.subList(3, leaderboardData.size) else emptyList()

        val leaderboardContainer = findViewById<LinearLayout>(R.id.leaderboardContainer)
        leaderboardContainer.removeAllViews()

        // Use the full leaderboardData list
        for (i in leaderboardData.indices) {
            val entry = leaderboardData[i]
            val leaderboardItem = layoutInflater.inflate(R.layout.leaderboard_item, null)

            // Set rank, name, points, and profile image
            val rankView = leaderboardItem.findViewById<TextView>(R.id.rank)
            val nameView = leaderboardItem.findViewById<TextView>(R.id.name)
            val pointsView = leaderboardItem.findViewById<TextView>(R.id.points)
            val profileImageView = leaderboardItem.findViewById<CircleImageView>(R.id.profileImage)

            rankView.text = "${i + 1}"
            nameView.text = entry.firstName
            pointsView.text = if (currentFilter == "weight") {
                "${entry.totalWeight} kg"
            } else {
                "${entry.totalReps} reps"
            }
            loadProfileImage(profileImageView, entry.profileUrl)

            // Update rank indicator
            val rankIndicator = leaderboardItem.findViewById<TextView>(R.id.rank_indicator)
            if (entry.rankChange > 0) {
                rankIndicator.text = "▲${entry.rankChange}"
                rankIndicator.setTextColor(Color.GREEN)
            } else if (entry.rankChange < 0) {
                rankIndicator.text = "▼${-entry.rankChange}"
                rankIndicator.setTextColor(Color.RED)
            } else {
                rankIndicator.text = "–"
                rankIndicator.setTextColor(Color.GRAY)
            }

            // Set the background based on the rank
            val backgroundDrawable = when (i) {
                0 -> R.drawable.rounded_gold_background
                1 -> R.drawable.rounded_silver_background
                2 -> R.drawable.rounded_bronze_background
                else -> R.drawable.rounded_default_background
            }
            leaderboardItem.background = resources.getDrawable(backgroundDrawable, null)

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
