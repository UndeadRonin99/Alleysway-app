package com.example.alleysway

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class log_workout : AppCompatActivity() {

    private lateinit var exerciseAdapter: WorkoutAdapter
    private lateinit var totalWeight: TextView
    private lateinit var SaveWorkout: Button
    private val exerciseList = mutableListOf<ExerciseData>() // stores exercise data for logging sets

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_workout)

        val recyclerView: RecyclerView = findViewById(R.id.exerciseRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        totalWeight = findViewById(R.id.totalWeight)
        SaveWorkout = findViewById(R.id.btnSaveWorkout)

        // Initialize adapter for exercises
        exerciseAdapter = WorkoutAdapter(exerciseList, { exercise ->
            exercise.sets.add(SetData())
            exerciseAdapter.notifyDataSetChanged()
            updateTotalWeight()
        }, { exercise ->
            exerciseList.remove(exercise)
            exerciseAdapter.notifyDataSetChanged()
            updateTotalWeight()
        }, { updateTotalWeight() })

        recyclerView.adapter = exerciseAdapter

        val addExerciseButton: Button = findViewById(R.id.addExerciseButton)
        addExerciseButton.setOnClickListener {
            // Start Stronger_function_page_1 to select exercises
            val intent = Intent(this, Stronger_function_page_1::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_EXERCISES)
        }

        if (!strongerFunctionStarted) {
            // Start Stronger_function_page_1 to select exercises
            val intent = Intent(this, Stronger_function_page_1::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_EXERCISES)
        }

        SaveWorkout.setOnClickListener {
            val user = Firebase.auth.currentUser
            if (user != null) {
                saveWorkoutToFirebase(user.uid, FirebaseDatabase.getInstance().reference)
            }
            finish()
        }
    }

    // Function to save the workout to Firebase
    private fun saveWorkoutToFirebase(userId: String, database: DatabaseReference) {
        // Create a unique ID for the workout
        val workoutId = UUID.randomUUID().toString()

        // Calculate total weight and total reps
        var totalWeight = 0.0
        var totalReps = 0
        exerciseList.forEach { exercise ->
            exercise.sets.forEach { set ->
                totalWeight += set.reps * set.weight
                totalReps += set.reps
            }
        }
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        val formattedDate = dateFormat.format(currentDate)

        // Create a workout map
        val workoutMap = hashMapOf<String, Any>(
            "totalWeight" to totalWeight,
            "totalReps" to totalReps,
            "date" to formattedDate,
            "workout" to exerciseList.associate { exercise ->
                exercise.name to mapOf(
                    "sets" to exercise.sets.mapIndexed { index, set ->
                        "Set ${index + 1}" to mapOf(
                            "reps" to set.reps,
                            "weight" to set.weight
                        )
                    }
                )
            }
        )

        // Save workout under the user's profile
        database.child("users").child(userId).child("workouts").child(workoutId)
            .setValue(workoutMap)
            .addOnSuccessListener {
                // Successfully saved the workout
                showToast("Workout saved successfully")
            }
            .addOnFailureListener { e ->
                // Failed to save the workout
                showToast("Failed to save workout: ${e.message}")
            }
    }

    // Helper function to show a toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Receive exercises from Stronger_function_page_1
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_EXERCISES && resultCode == Activity.RESULT_OK) {
            val newExercises = data?.getParcelableArrayListExtra<Exercise>(EXTRA_SELECTED_EXERCISES)
            if (newExercises != null) {
                newExercises.forEach { exercise ->
                    exerciseList.add(ExerciseData(exercise.name))
                }
                exerciseAdapter.notifyDataSetChanged()
            }
        }
    }

    // Function to calculate and update the total weight and total reps
    private fun updateTotalWeight() {
        var totalWeightValue = 0.0
        var totalRepsValue = 0

        // Iterate through all exercises and sets
        exerciseList.forEach { exercise ->
            exercise.sets.forEach { set ->
                totalWeightValue += set.reps * set.weight  // Calculate total weight
                totalRepsValue += set.reps                 // Sum up total reps
            }
        }

        // Update the totalWeight TextView with the calculated values
        totalWeight.text = "Total Weight: ${totalWeightValue}kg\nTotal Reps: $totalRepsValue"
    }

    companion object {
        const val EXTRA_SELECTED_EXERCISES = "extra_selected_exercises"
        const val REQUEST_CODE_ADD_EXERCISES = 100
        var strongerFunctionStarted = false // This tracks whether stronger_function has been started
    }
}
