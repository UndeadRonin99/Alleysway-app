// Package declaration for the app
package com.techtitans.alleysway

// Importing necessary Android and Firebase libraries
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Activity class for viewing exercises
class ViewExcercises : AppCompatActivity() {
    // UI components
    private lateinit var expandableListView: ExpandableListView // ExpandableListView to display grouped exercises
    private lateinit var exerciseAdapter: GroupedExerciseAdapter // Adapter for the ExpandableListView
    private lateinit var searchEditText: EditText // EditText for searching exercises

    // Data structures to hold exercises
    private val exerciseMap: MutableMap<String, MutableList<Exercise>> = mutableMapOf() // Maps muscle groups to their exercises
    private var originalExerciseMap: MutableMap<String, MutableList<Exercise>> = mutableMapOf() // Original map for resetting search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display for immersive UI
        setContentView(R.layout.activity_view_excercises) // Set the layout for the activity

        // Initialize UI components by finding them in the layout
        expandableListView = findViewById(R.id.expandableListView)
        searchEditText = findViewById(R.id.searchExercise)

        // Back button to finish the activity and return to the previous screen
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Close the activity when back button is clicked
        }

        // Reference to the "exercises" node in Firebase Database
        val database = FirebaseDatabase.getInstance().getReference("exercises")
        
        // Fetch exercises from Firebase
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                exerciseMap.clear() // Clear existing data
                // Iterate through each child in the "exercises" node
                for (exerciseSnapshot in snapshot.children) {
                    val exercise = exerciseSnapshot.getValue(Exercise::class.java) // Deserialize data into Exercise object
                    if (exercise != null) {
                        val muscleGroup = exercise.mainMuscle // Get the main muscle group of the exercise
                        // Group exercises by their mainMuscle
                        if (exerciseMap.containsKey(muscleGroup)) {
                            exerciseMap[muscleGroup]?.add(exercise) // Add to existing muscle group
                        } else {
                            exerciseMap[muscleGroup] = mutableListOf(exercise) // Create new muscle group
                        }
                    }
                }

                // Save a copy of the original exercise map for search functionality
                originalExerciseMap = HashMap(exerciseMap)

                // Sort exercises within each muscle group alphabetically by name
                for (entry in exerciseMap) {
                    entry.value.sortBy { it.name }
                }

                // Initialize the adapter with the grouped and sorted exercises
                exerciseAdapter = GroupedExerciseAdapter(this@ViewExcercises, exerciseMap)
                expandableListView.setAdapter(exerciseAdapter) // Set the adapter to the ExpandableListView
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors that occur while fetching data
                Toast.makeText(this@ViewExcercises, "Failed to load exercises", Toast.LENGTH_SHORT).show()
            }
        })

        // Add TextWatcher to the search EditText to filter exercises as the user types
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changes
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterExercises(s.toString()) // Filter exercises based on the current input
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed after text changes
            }
        })

        // Set listener for clicking on an exercise item to show its details
        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val muscleGroup = exerciseAdapter.getGroup(groupPosition) as String // Get the muscle group
            val exercise = exerciseAdapter.getChild(groupPosition, childPosition) as Exercise // Get the specific exercise
            showExerciseDetails(exercise) // Display the exercise details in a dialog
            true // Indicate that the click was handled
        }
    }

    // Method to filter exercises based on the search query
    private fun filterExercises(query: String) {
        val filteredMap: MutableMap<String, MutableList<Exercise>> = mutableMapOf() // Temporary map for filtered results

        if (query.isEmpty()) {
            filteredMap.putAll(originalExerciseMap) // If query is empty, show all exercises
        } else {
            // Iterate through each muscle group and filter exercises by name
            for ((muscleGroup, exercises) in originalExerciseMap) {
                val filteredExercises = exercises.filter {
                    it.name.contains(query, ignoreCase = true) // Case-insensitive search
                }
                if (filteredExercises.isNotEmpty()) {
                    filteredMap[muscleGroup] = filteredExercises.toMutableList() // Add filtered exercises to the map
                }
            }
        }

        exerciseAdapter.updateData(filteredMap) // Update the adapter with the filtered list
    }

    // Method to display exercise details in a dialog
    private fun showExerciseDetails(exercise: Exercise) {
        val dialog = Dialog(this) // Create a new dialog
        dialog.setContentView(R.layout.exercise_details) // Set the layout for the dialog

        // Initialize dialog UI components
        val exerciseName = dialog.findViewById<TextView>(R.id.exerciseName)
        val exerciseGif = dialog.findViewById<ImageView>(R.id.exerciseGif)
        val tip1 = dialog.findViewById<TextView>(R.id.tip1)
        val tip2 = dialog.findViewById<TextView>(R.id.tip2)
        val tip3 = dialog.findViewById<TextView>(R.id.tip3)

        exerciseName.text = exercise.name // Set the exercise name

        // Load the exercise GIF image using Glide
        Glide.with(this)
            .load(exercise.imageUrl) // URL of the exercise image
            .into(exerciseGif) // Load into the ImageView

        // Populate tips if available
        if (exercise.tips.isNotEmpty()) tip1.text = exercise.tips[0] // First tip
        if (exercise.tips.size > 1) tip2.text = exercise.tips[1] // Second tip
        if (exercise.tips.size > 2) tip3.text = exercise.tips[2] // Third tip

        dialog.show() // Display the dialog to the user
    }
}
