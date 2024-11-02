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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.util.Locale

class PastWorkoutsActivity : AppCompatActivity() {

    private lateinit var workoutsRecyclerView: RecyclerView
    private lateinit var pastWorkoutsAdapter: PastWorkoutsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView
    private lateinit var searchExercise: EditText

    private val workoutsList = mutableListOf<WorkoutData>()
    private val filteredWorkoutsList = mutableListOf<WorkoutData>() // For filtering

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_workouts)

        // Initialize UI components
        workoutsRecyclerView = findViewById(R.id.workoutsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)
        searchExercise = findViewById(R.id.searchExercise)

        workoutsRecyclerView.layoutManager = LinearLayoutManager(this)
        pastWorkoutsAdapter = PastWorkoutsAdapter(filteredWorkoutsList)
        workoutsRecyclerView.adapter = pastWorkoutsAdapter

        // Set up back button
        btnBack.setOnClickListener {
            finish()
        }

        // Set up search functionality
        searchExercise.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterWorkouts(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do nothing
            }
        })

        fetchUserWorkouts()
    }

    private fun fetchUserWorkouts() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference
        val workoutsRef = database.child("users").child(userId).child("workouts")

        progressBar.visibility = View.VISIBLE

        workoutsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                workoutsList.clear()
                for (workoutSnapshot in snapshot.children) {
                    val workoutData = parseWorkoutSnapshot(workoutSnapshot)
                    workoutsList.add(workoutData)
                }

                // Sort the workoutsList by timestamp in descending order
                workoutsList.sortByDescending { it.timestamp }

                // Copy the workoutsList to filteredWorkoutsList
                filteredWorkoutsList.clear()
                filteredWorkoutsList.addAll(workoutsList)

                pastWorkoutsAdapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                progressBar.visibility = View.GONE
            }
        })
    }

    private fun filterWorkouts(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        filteredWorkoutsList.clear()
        if (lowerCaseQuery.isEmpty()) {
            filteredWorkoutsList.addAll(workoutsList)
        } else {
            for (workout in workoutsList) {
                if (workout.name.lowercase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredWorkoutsList.add(workout)
                }
            }
        }
        pastWorkoutsAdapter.notifyDataSetChanged()
    }

    private fun parseWorkoutSnapshot(workoutSnapshot: DataSnapshot): WorkoutData {
        val name = workoutSnapshot.child("name").getValue(String::class.java) ?: "Unnamed Workout"
        val date = workoutSnapshot.child("date").getValue(String::class.java) ?: ""
        val totalWeight = workoutSnapshot.child("totalWeight").getValue(Double::class.java) ?: 0.0
        val totalReps = workoutSnapshot.child("totalReps").getValue(Int::class.java) ?: 0
        val timestamp = workoutSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L

        val exercisesList = mutableListOf<ExerciseData>()

        val workoutExercisesSnapshot = workoutSnapshot.child("workout")
        for (exerciseSnapshot in workoutExercisesSnapshot.children) {
            val exerciseName = exerciseSnapshot.key ?: "Unnamed Exercise"
            val setsList = mutableListOf<SetData>()

            val setsSnapshot = exerciseSnapshot.child("sets")
            for (setSnapshot in setsSnapshot.children) {
                val reps = setSnapshot.child("reps").getValue(Int::class.java) ?: 0
                val weight = setSnapshot.child("weight").getValue(Double::class.java) ?: 0.0

                val setData = SetData(reps, weight)
                setsList.add(setData)
            }

            val exerciseData = ExerciseData(exerciseName, setsList)
            exercisesList.add(exerciseData)
        }

        return WorkoutData(name, date, totalWeight, totalReps, exercisesList, timestamp)
    }
}
