// Package declaration for the project
package com.techtitans.alleysway

// Necessary Android imports for components and Firebase services
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

// Define log_workout activity class that inherits from AppCompatActivity
class log_workout : AppCompatActivity() {

    // Lateinit vars for managing UI components and data
    private lateinit var exerciseAdapter: WorkoutAdapter // Adapter for handling exercises in RecyclerView
    private lateinit var totalWeight: TextView // TextView to display total weight
    private lateinit var saveWorkout: MaterialButton // Button to trigger saving the workout
    private val exerciseList = mutableListOf<ExerciseData>() // MutableList to store exercises during the workout

    // onCreate method to setup the activity when it is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Setup edge-to-edge experience
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE) // Adjust the window when the keyboard is visible

        setContentView(R.layout.activity_log_workout) // Set the layout for this activity

        // Initialize RecyclerView and its components
        val recyclerView: RecyclerView = findViewById(R.id.exerciseRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        totalWeight = findViewById(R.id.totalWeight)
        saveWorkout = findViewById(R.id.btnSaveWorkout)

        // Setup the exercise adapter with callbacks for adding and removing sets/exercises
        exerciseAdapter = WorkoutAdapter(
            exercises = exerciseList,
            onAddSet = { exercise ->
                exercise.sets.add(SetData()) // Add a new set
                exerciseAdapter.notifyDataSetChanged() // Notify adapter to refresh the view
                updateTotalWeight() // Update the total weight display
            },
            onDeleteExercise = { exercise ->
                exerciseList.remove(exercise) // Remove the exercise from the list
                exerciseAdapter.notifyDataSetChanged() // Notify adapter to refresh the view
                updateTotalWeight() // Update the total weight display
            },
            onUpdateTotalWeight = { updateTotalWeight() }, // Callback to update total weight
            isLogging = true // Flag to specify this is a logging session
        )

        recyclerView.adapter = exerciseAdapter // Set adapter for the RecyclerView

        // Setup button to add new exercises
        val addExerciseButton: MaterialButton = findViewById(R.id.addExerciseButton)
        addExerciseButton.setOnClickListener {
            // Start Stronger_function_page_1 activity for result to select exercises
            val intent = Intent(this, Stronger_function_page_1::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_EXERCISES)
        }

        // Automatically start exercise selection if it has not started before
        if (!strongerFunctionStarted) {
            val intent = Intent(this, Stronger_function_page_1::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_EXERCISES)
        }

        // Setup save workout button with AlertDialog for workout naming
        saveWorkout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Enter Workout Name") // Dialog title

            val input = EditText(this) // EditText for input
            input.hint = "Workout Name" // Set hint for EditText
            builder.setView(input) // Add EditText to AlertDialog

            builder.setPositiveButton("OK") { dialog, which ->
                val workoutName = input.text.toString().trim()
                if (workoutName.isEmpty()) {
                    showToast("Please enter a workout name.") // Show toast if input is empty
                } else {
                    val user = Firebase.auth.currentUser
                    if (user != null) {
                        saveWorkoutToFirebase(user.uid, FirebaseDatabase.getInstance().reference, workoutName) // Save workout to Firebase
                    }
                    finish() // Finish the activity
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel() // Cancel the dialog
            }

            builder.show() // Show the AlertDialog
        }
    }

    // Function to save the workout data to Firebase
    private fun saveWorkoutToFirebase(userId: String, database: DatabaseReference, workoutName: String) {
        val workoutId = UUID.randomUUID().toString() // Generate a unique ID for the workout

        // Calculate total weight and reps across all exercises and sets
        var totalWeightValue = 0.0
        var totalRepsValue = 0
        exerciseList.forEach { exercise ->
            exercise.sets.forEach { set ->
                totalWeightValue += set.reps * set.weight // Calculate weight lifted per set
                totalRepsValue += set.reps // Accumulate reps
            }
        }

        // Format the current date
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        val formattedDate = dateFormat.format(currentDate)
        val timestamp = System.currentTimeMillis() // Get the current timestamp

        // Prepare the workout data as a map for Firebase
        val workoutMap = hashMapOf<String, Any>(
            "name" to workoutName,
            "totalWeight" to totalWeightValue,
            "totalReps" to totalRepsValue,
            "date" to formattedDate,
            "timestamp" to timestamp, // Add timestamp
            "workout" to exerciseList.associate { exercise ->
                exercise.name to mapOf(
                    "sets" to exercise.sets.mapIndexed { index, set ->
                        "Set ${index + 1}" to mapOf(
                            "reps" to set.reps,
                            "weight" to set.weight
                        )
                    }.toMap()
                )
            }
        )

        // Save workout to the user's profile in Firebase
        database.child("users").child(userId).child("workouts").child(workoutId)
            .setValue(workoutMap)
            .addOnSuccessListener {
                showToast("Workout saved successfully") // Show success message
            }
            .addOnFailureListener { e ->
                showToast("Failed to save workout: ${e.message}") // Show failure message
            }
    }

    // Helper function to display a toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Handle the result from Stronger_function_page_1 with selected exercises
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_EXERCISES && resultCode == Activity.RESULT_OK) {
            val newExercises = data?.getParcelableArrayListExtra<Exercise>(EXTRA_SELECTED_EXERCISES)
            if (newExercises != null) {
                newExercises.forEach { exercise ->
                    exerciseList.add(ExerciseData(exercise.name)) // Add new exercise to the list
                }
                exerciseAdapter.notifyDataSetChanged() // Refresh the adapter
            }
        }
    }

    // Function to calculate and display the total weight and total reps for the workout
    private fun updateTotalWeight() {
        var totalWeightValue = 0.0
        var totalRepsValue = 0

        exerciseList.forEach { exercise ->
            exercise.sets.forEach { set ->
                totalWeightValue += set.reps * set.weight  // Calculate total weight lifted
                totalRepsValue += set.reps                 // Calculate total reps performed
            }
        }

        totalWeight.text = "Total Weight: ${totalWeightValue}kg   Total Reps: $totalRepsValue" // Display the total weight and reps
    }

    // Static values and flags used in the class
    companion object {
        const val EXTRA_SELECTED_EXERCISES = "extra_selected_exercises" // Constant for intent extra
        const val REQUEST_CODE_ADD_EXERCISES = 100 // Request code for starting activity for result
        var strongerFunctionStarted = false // Flag to track if the exercise selection activity has been started
    }
}
