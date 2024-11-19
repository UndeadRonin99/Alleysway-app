// File: app/src/main/java/com/example/alleysway/PastWorkoutsActivity.kt
package com.techtitans.alleysway

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.Locale

/**
 * Activity to display and manage the user's past workouts.
 */
class PastWorkoutsActivity : AppCompatActivity() {

    // UI Components
    private lateinit var workoutsRecyclerView: RecyclerView
    private lateinit var pastWorkoutsAdapter: PastWorkoutsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView
    private lateinit var searchExercise: EditText

    // Data Lists
    private val workoutsList = mutableListOf<WorkoutData>()
    private val filteredWorkoutsList = mutableListOf<WorkoutData>() // For search filtering

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_workouts)

        // Initialize UI components
        workoutsRecyclerView = findViewById(R.id.workoutsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)
        searchExercise = findViewById(R.id.searchExercise)

        // Setup RecyclerView with adapter and layout manager
        workoutsRecyclerView.layoutManager = LinearLayoutManager(this)
        pastWorkoutsAdapter = PastWorkoutsAdapter(filteredWorkoutsList)
        workoutsRecyclerView.adapter = pastWorkoutsAdapter

        // Back button functionality to close the activity
        btnBack.setOnClickListener {
            finish()
        }

        // Search functionality to filter workouts by name
        searchExercise.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterWorkouts(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed
            }
        })

        // Fetch workouts from Firebase
        fetchUserWorkouts()
    }

    /**
     * Fetches the user's past workouts from Firebase Realtime Database.
     */
    private fun fetchUserWorkouts() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference
        val workoutsRef = database.child("users").child(userId).child("workouts")

        progressBar.visibility = View.VISIBLE

        // Listen for a single update of the workouts data
        workoutsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                workoutsList.clear()
                for (workoutSnapshot in snapshot.children) {
                    val workoutData = parseWorkoutSnapshot(workoutSnapshot)
                    workoutsList.add(workoutData)
                }

                // Sort workouts by timestamp descending (most recent first)
                workoutsList.sortByDescending { it.timestamp }

                // Initialize the filtered list with all workouts
                filteredWorkoutsList.clear()
                filteredWorkoutsList.addAll(workoutsList)

                // Notify adapter of data changes and hide progress bar
                pastWorkoutsAdapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
                progressBar.visibility = View.GONE
                Toast.makeText(this@PastWorkoutsActivity, "Failed to load workouts.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Filters the workouts based on the user's search query.
     *
     * @param query The search string entered by the user.
     */
    private fun filterWorkouts(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        filteredWorkoutsList.clear()
        if (lowerCaseQuery.isEmpty()) {
            // If query is empty, show all workouts
            filteredWorkoutsList.addAll(workoutsList)
        } else {
            // Filter workouts whose names contain the query string
            for (workout in workoutsList) {
                if (workout.name.lowercase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredWorkoutsList.add(workout)
                }
            }
        }
        pastWorkoutsAdapter.notifyDataSetChanged()
    }

    /**
     * Parses a DataSnapshot from Firebase into a WorkoutData object.
     *
     * @param workoutSnapshot The snapshot representing a single workout.
     * @return The parsed WorkoutData object.
     */
    private fun parseWorkoutSnapshot(workoutSnapshot: DataSnapshot): WorkoutData {
        val name = workoutSnapshot.child("name").getValue(String::class.java) ?: "Unnamed Workout"
        val date = workoutSnapshot.child("date").getValue(String::class.java) ?: ""
        val totalWeight = workoutSnapshot.child("totalWeight").getValue(Double::class.java) ?: 0.0
        val totalReps = workoutSnapshot.child("totalReps").getValue(Int::class.java) ?: 0
        val timestamp = workoutSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L

        val exercisesList = mutableListOf<ExerciseData>()

        // Iterate through each exercise in the workout
        val workoutExercisesSnapshot = workoutSnapshot.child("workout")
        for (exerciseSnapshot in workoutExercisesSnapshot.children) {
            val exerciseName = exerciseSnapshot.key ?: "Unnamed Exercise"
            val setsList = mutableListOf<SetData>()

            // Iterate through each set in the exercise
            val setsSnapshot = exerciseSnapshot.child("sets")
            for (setSnapshot in setsSnapshot.children) {
                val reps = setSnapshot.child("reps").getValue(Int::class.java) ?: 0
                val weight = setSnapshot.child("weight").getValue(Double::class.java) ?: 0.0
                setsList.add(SetData(reps, weight))
            }

            exercisesList.add(ExerciseData(exerciseName, setsList))
        }

        return WorkoutData(name, date, totalWeight, totalReps, exercisesList, timestamp)
    }
}
