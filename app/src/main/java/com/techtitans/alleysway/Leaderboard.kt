package com.techtitans.alleysway

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.techtitans.alleysway.models.LeaderboardEntry
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Calendar
import android.widget.*
import com.google.firebase.database.*

class Leaderboard : AppCompatActivity() {

    private var currentFilter = "weight" // Default filter is "Total Weight"
    private lateinit var leaderboardData: MutableList<LeaderboardEntry>
    private val weightRankChangeMap = mutableMapOf<String, Int>() // Rank changes for weight
    private val repsRankChangeMap = mutableMapOf<String, Int>()   // Rank changes for reps
    private val loggedInUserId: String? = Firebase.auth.currentUser?.uid // Get the logged-in user's ID

    private lateinit var switchLeaderboardParticipation: Switch
    private val userId = Firebase.auth.currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        // Back button functionality
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        leaderboardData = mutableListOf() // Initialize leaderboard data
        checkForDailyReset() // Check if rank indicators should reset
        loadRankChanges() // Load cumulative rank changes from previous sessions

        // Initialize the participation switch
        switchLeaderboardParticipation = findViewById(R.id.switchLeaderboardParticipation)
        setupParticipationSwitch()

        fetchLeaderboardData() // Initial data load

        // Handle filter selection
        val filterRadioGroup = findViewById<RadioGroup>(R.id.filterRadioGroup)
        filterRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            currentFilter = if (checkedId == R.id.radioTotalWeight) "weight" else "reps"
            fetchLeaderboardData() // Refetch and recalculate on filter change
        }
    }
    private fun setupParticipationSwitch() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("users/$userId/participateInLeaderboard")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var participates = snapshot.getValue(Boolean::class.java)
                if (participates == null) {
                    // If the field doesn't exist, set it to true in the database
                    participates = true
                    databaseRef.setValue(true)
                }
                switchLeaderboardParticipation.isChecked = participates
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Leaderboard, "Failed to load preference", Toast.LENGTH_SHORT).show()
            }
        })

        // Listen for changes
        switchLeaderboardParticipation.setOnCheckedChangeListener { _, isChecked ->
            databaseRef.setValue(isChecked)
            fetchLeaderboardData()
        }
    }


    private fun checkForDailyReset() {
        val sharedPreferences = getSharedPreferences("LeaderboardPrefs", Context.MODE_PRIVATE)
        val lastResetTime = sharedPreferences.getLong("lastResetTime", 0)
        val currentTime = System.currentTimeMillis()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = lastResetTime

        // If last reset was before today, clear the ranking indicators
        if (isNewDay(calendar)) {
            weightRankChangeMap.clear()
            repsRankChangeMap.clear()
            saveRankChanges("weight")
            saveRankChanges("reps")

            // Update the last reset time to now
            sharedPreferences.edit().putLong("lastResetTime", currentTime).apply()
        }
    }

    private fun isNewDay(lastResetCalendar: Calendar): Boolean {
        val currentCalendar = Calendar.getInstance()
        return currentCalendar.get(Calendar.YEAR) > lastResetCalendar.get(Calendar.YEAR) ||
                currentCalendar.get(Calendar.DAY_OF_YEAR) > lastResetCalendar.get(Calendar.DAY_OF_YEAR)
    }

    private fun fetchLeaderboardData() {
        val database = FirebaseDatabase.getInstance().getReference("users")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                leaderboardData.clear()

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue

                    // Get participation preference, default to true if not set
                    val participates = userSnapshot.child("participateInLeaderboard").getValue(Boolean::class.java) ?: true

                    // Skip users who have opted out
                    if (!participates) continue

                    val firstName = userSnapshot.child("firstName").getValue(String::class.java) ?: "Unknown"
                    val profileUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java) ?: ""

                    var totalWeightValue = 0.0
                    var totalRepsValue = 0

                    // **Iterate over the user's workouts to calculate totalWeight and totalReps**
                    val workoutsSnapshot = userSnapshot.child("workouts")
                    for (workoutSnapshot in workoutsSnapshot.children) {
                        val workoutWeight = when (val weightValue = workoutSnapshot.child("totalWeight").getValue()) {
                            is Number -> weightValue.toDouble()
                            else -> 0.0
                        }

                        val workoutReps = when (val repsValue = workoutSnapshot.child("totalReps").getValue()) {
                            is Number -> repsValue.toInt()
                            else -> 0
                        }

                        totalWeightValue += workoutWeight
                        totalRepsValue += workoutReps
                    }

                    leaderboardData.add(LeaderboardEntry(userId, firstName, totalWeightValue, totalRepsValue, profileUrl))
                }

                sortLeaderboardData()
                calculateRankChanges()
                updateLeaderboardUI(leaderboardData)
                saveCurrentRanks()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Leaderboard, "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
            }
        })
    }




    private fun loadPreviousRanks(filter: String): Map<String, Int> {
        val sharedPreferences = getSharedPreferences("LeaderboardPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val key = "rankHistory_$filter" // Key varies by filter
        val json = sharedPreferences.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(json, type)
        } else {
            leaderboardData.mapIndexed { index, entry -> entry.userId to (index + 1) }.toMap()
        }
    }

    private fun saveCurrentRanks() {
        val sharedPreferences = getSharedPreferences("LeaderboardPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val currentRanks = leaderboardData.mapIndexed { index, entry -> entry.userId to (index + 1) }.toMap()
        editor.putString("rankHistory_$currentFilter", gson.toJson(currentRanks))
        editor.apply()
    }

    private fun loadRankChanges() {
        val sharedPreferences = getSharedPreferences("LeaderboardPrefs", Context.MODE_PRIVATE)
        val gson = Gson()

        // Load rank changes for "weight" and "reps"
        val weightJson = sharedPreferences.getString("rankChanges_weight", null)
        if (weightJson != null) {
            val type = object : TypeToken<MutableMap<String, Int>>() {}.type
            weightRankChangeMap.putAll(gson.fromJson(weightJson, type))
        }

        val repsJson = sharedPreferences.getString("rankChanges_reps", null)
        if (repsJson != null) {
            val type = object : TypeToken<MutableMap<String, Int>>() {}.type
            repsRankChangeMap.putAll(gson.fromJson(repsJson, type))
        }
    }

    private fun saveRankChanges(filter: String) {
        val sharedPreferences = getSharedPreferences("LeaderboardPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        // Save rank changes separately for each filter
        if (filter == "weight") {
            editor.putString("rankChanges_weight", gson.toJson(weightRankChangeMap))
        } else {
            editor.putString("rankChanges_reps", gson.toJson(repsRankChangeMap))
        }
        editor.apply()
    }

    // Main calculateRankChanges method, delegates to specific methods
    private fun calculateRankChanges() {
        if (currentFilter == "weight") {
            calculateWeightRankChanges()
        } else {
            calculateRepsRankChanges()
        }
    }

    // Calculate rank changes for weight
    private fun calculateWeightRankChanges() {
        val previousRanks = loadPreviousRanks("weight")

        leaderboardData.forEachIndexed { index, entry ->
            val currentRank = index + 1
            val previousRank = previousRanks[entry.userId] ?: currentRank
            val rankChange = previousRank - currentRank

            if (rankChange != 0) {
                weightRankChangeMap[entry.userId] = rankChange
            }
        }
        saveRankChanges("weight")
    }

    // Calculate rank changes for reps
    private fun calculateRepsRankChanges() {
        val previousRanks = loadPreviousRanks("reps")

        leaderboardData.forEachIndexed { index, entry ->
            val currentRank = index + 1
            val previousRank = previousRanks[entry.userId] ?: currentRank
            val rankChange = previousRank - currentRank

            if (rankChange != 0) {
                repsRankChangeMap[entry.userId] = rankChange
            }
        }
        saveRankChanges("reps")
    }

    private fun sortLeaderboardData() {
        leaderboardData.sortWith(compareByDescending<LeaderboardEntry> {
            if (currentFilter == "weight") it.totalWeight else it.totalReps.toDouble()
        })
    }
    private fun updateLeaderboardUI(leaderboardData: List<LeaderboardEntry>) {
        // Clear existing views
        findViewById<TextView>(R.id.firstPlaceName).text = ""
        findViewById<TextView>(R.id.secondPlaceName).text = ""
        findViewById<TextView>(R.id.thirdPlaceName).text = ""
        findViewById<CircleImageView>(R.id.firstPlaceImage).setImageResource(R.drawable.placeholder_profile)
        findViewById<CircleImageView>(R.id.secondPlaceImage).setImageResource(R.drawable.placeholder_profile)
        findViewById<CircleImageView>(R.id.thirdPlaceImage).setImageResource(R.drawable.placeholder_profile)
        findViewById<TextView>(R.id.firstPlacePoints).text = ""
        findViewById<TextView>(R.id.secondPlacePoints).text = ""
        findViewById<TextView>(R.id.thirdPlacePoints).text = ""

        if (leaderboardData.isEmpty()) {
            Toast.makeText(this, "No users are participating in the leaderboard.", Toast.LENGTH_SHORT).show()
            return
        }

        // Update UI for top 3 users
        if (leaderboardData.size >= 1) updateTop3UI(0, R.id.firstPlaceName, R.id.firstPlaceImage, R.id.firstPlacePoints)
        if (leaderboardData.size >= 2) updateTop3UI(1, R.id.secondPlaceName, R.id.secondPlaceImage, R.id.secondPlacePoints)
        if (leaderboardData.size >= 3) updateTop3UI(2, R.id.thirdPlaceName, R.id.thirdPlaceImage, R.id.thirdPlacePoints)

        // ScrollView for the full leaderboard
        val leaderboardContainer = findViewById<LinearLayout>(R.id.leaderboardContainer)
        leaderboardContainer.removeAllViews()

        // Use the rank change map based on the current filter
        val rankChangeMap = if (currentFilter == "weight") weightRankChangeMap else repsRankChangeMap

        leaderboardData.forEachIndexed { index, entry ->
            // Choose layout based on whether this entry is the logged-in user
            val leaderboardItem = if (entry.userId == loggedInUserId) {
                layoutInflater.inflate(R.layout.leaderboard_item2, null)
            } else {
                layoutInflater.inflate(R.layout.leaderboard_item, null)
            }

            val rankView = leaderboardItem.findViewById<TextView>(R.id.rank)
            val nameView = leaderboardItem.findViewById<TextView>(R.id.name)
            val pointsView = leaderboardItem.findViewById<TextView>(R.id.points)
            val profileImageView = leaderboardItem.findViewById<CircleImageView>(R.id.profileImage)
            val rankIndicator = leaderboardItem.findViewById<TextView>(R.id.rank_indicator)

            rankView.text = "${index + 1}"
            nameView.text = entry.firstName // **Change here**
            pointsView.text = if (currentFilter == "weight") "${entry.totalWeight} kg" else "${entry.totalReps} reps"
            loadProfileImage(profileImageView, entry.profileUrl)

            val rankChange = rankChangeMap[entry.userId] ?: 0
            when {
                rankChange > 0 -> {
                    rankIndicator.text = "▲$rankChange"
                    rankIndicator.setTextColor(Color.GREEN)
                }
                rankChange < 0 -> {
                    rankIndicator.text = "▼${-rankChange}"
                    rankIndicator.setTextColor(Color.RED)
                }
                else -> {
                    rankIndicator.text = "–"
                    rankIndicator.setTextColor(Color.GRAY)
                }
            }
            leaderboardContainer.addView(leaderboardItem)
        }
    }

    private fun updateTop3UI(index: Int, nameId: Int, imageId: Int, pointsId: Int) {
        val entry = leaderboardData[index]
        val nameView = findViewById<TextView>(nameId)
        val imageView = findViewById<CircleImageView>(imageId)
        val pointsView = findViewById<TextView>(pointsId)

        nameView.text = entry.firstName // **Change here**
        pointsView.text = if (currentFilter == "weight") "${entry.totalWeight} kg" else "${entry.totalReps} reps"
        loadProfileImage(imageView, entry.profileUrl)
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
