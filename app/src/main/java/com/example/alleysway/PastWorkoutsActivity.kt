package com.example.alleysway

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class PastWorkoutsActivity : AppCompatActivity() {

    private lateinit var workoutsRecyclerView: RecyclerView
    private lateinit var pastWorkoutsAdapter: PastWorkoutsAdapter
    private lateinit var progressBar: ProgressBar
    private val workoutsList = mutableListOf<WorkoutData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_workouts)

        workoutsRecyclerView = findViewById(R.id.workoutsRecyclerView)
        progressBar = findViewById(R.id.progressBar)

        workoutsRecyclerView.layoutManager = LinearLayoutManager(this)
        pastWorkoutsAdapter = PastWorkoutsAdapter(workoutsList)
        workoutsRecyclerView.adapter = pastWorkoutsAdapter

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

                pastWorkoutsAdapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                progressBar.visibility = View.GONE
            }
        })
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
