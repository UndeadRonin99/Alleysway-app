package com.example.alleysway

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.alleysway.models.LeaderboardEntry
import com.google.firebase.database.*
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
                    sortAndDisplayLeaderboard()
                }
                R.id.radioTotalReps -> {
                    currentFilter = "reps"
                    sortAndDisplayLeaderboard()
                }
            }
        }
    }

    private fun fetchLeaderboardData() {
        val database = FirebaseDatabase.getInstance().getReference("users")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                previousLeaderboardData = leaderboardData.map { it.copy() }
                leaderboardData.clear()

                for (userSnapshot in snapshot.children) {
                    var totalWeight = 0.0
                    var totalReps = 0
                    val firstName = userSnapshot.child("firstName").getValue(String::class.java) ?: "Unknown"
                    val profileUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java) ?: ""
                    val workoutsSnapshot = userSnapshot.child("workouts")

                    for (workoutSnapshot in workoutsSnapshot.children) {
                        val workoutWeight = workoutSnapshot.child("totalWeight").getValue(Double::class.java) ?: 0.0
                        val workoutReps = workoutSnapshot.child("totalReps").getValue(Int::class.java) ?: 0
                        totalWeight += workoutWeight
                        totalReps += workoutReps
                    }

                    leaderboardData.add(LeaderboardEntry(firstName, totalWeight, totalReps, profileUrl))
                }

                // Sort and display the leaderboard based on the current filter
                sortAndDisplayLeaderboard()

                // Calculate rank changes
                calculateRankChanges()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Leaderboard, "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculateRankChanges() {
        val previousRanks = previousLeaderboardData.sortedWith(compareByDescending<LeaderboardEntry> {
            if (currentFilter == "weight") it.totalWeight else it.totalReps.toDouble()
        }).mapIndexed { index, entry ->
            entry.firstName to index + 1
        }.toMap()

        leaderboardData.forEachIndexed { index, entry ->
            val currentRank = index + 1
            val previousRank = previousRanks[entry.firstName] ?: currentRank
            entry.rankChange = previousRank - currentRank
        }

        // Update the UI with the new rank changes
        updateLeaderboardUI(leaderboardData)
    }

    private fun sortAndDisplayLeaderboard() {
        leaderboardData.sortWith(compareByDescending<LeaderboardEntry> {
            if (currentFilter == "weight") it.totalWeight else it.totalReps.toDouble()
        })
        updateLeaderboardUI(leaderboardData)
    }

    private fun updateLeaderboardUI(leaderboardData: List<LeaderboardEntry>) {

        if (leaderboardData.isEmpty()) return

        // Populate all users in the scroll view
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
            when {
                entry.rankChange > 0 -> {
                    rankIndicator.text = "▲${entry.rankChange}"
                    rankIndicator.setTextColor(Color.GREEN)
                }
                entry.rankChange < 0 -> {
                    rankIndicator.text = "▼${-entry.rankChange}"
                    rankIndicator.setTextColor(Color.RED)
                }
                else -> {
                    rankIndicator.text = "•"
                    rankIndicator.setTextColor(Color.GRAY)
                }
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
