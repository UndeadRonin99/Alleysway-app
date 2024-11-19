// Package declaration indicating the namespace of the class
package com.techtitans.alleysway

// Importing necessary Android and Firebase libraries
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Activity class for selecting exercises to add to a workout.
 *
 * This activity displays a list of exercises grouped by muscle groups using an ExpandableListView.
 * Users can search for exercises, select multiple exercises, and add them to log a workout.
 */
class Stronger_function_page_1 : AppCompatActivity() {

    // UI components
    private lateinit var expandableListView: ExpandableListView // ExpandableListView to display grouped exercises
    private lateinit var exerciseAdapter: SelectableExerciseAdapter // Custom adapter for handling exercise selection
    private lateinit var addButton: Button // Button to add selected exercises to the workout
    private lateinit var searchEditText: EditText // EditText for searching exercises
    private lateinit var backButton: ImageView // ImageView acting as a back button

    // Data structures to hold exercises
    private val exerciseMap: MutableMap<String, MutableList<Exercise>> = mutableMapOf() // Maps muscle groups to their exercises
    private var originalExerciseMap: MutableMap<String, MutableList<Exercise>> = mutableMapOf() // Original map for resetting search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display for immersive UI
        setContentView(R.layout.activity_stronger_function_page1) // Set the layout for this activity

        // Initialize UI components by finding them in the layout
        expandableListView = findViewById(R.id.expandableListView)
        addButton = findViewById(R.id.addExercisesBtn)
        searchEditText = findViewById(R.id.searchExercise)
        backButton = findViewById(R.id.btnBack) // Initialize the back button

        // Set OnClickListener for the back button to navigate back to the Workouts page
        backButton.setOnClickListener {
            val intent = Intent(this, Workouts::class.java) // Intent to navigate to Workouts activity
            startActivity(intent) // Start the Workouts activity
            finish() // Optional: Finish this activity to prevent returning back to it
        }

        // Reference to the "exercises" node in Firebase Realtime Database
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

                // Initialize the adapter with the grouped exercises
                exerciseAdapter = SelectableExerciseAdapter(this@Stronger_function_page_1, exerciseMap)
                expandableListView.setAdapter(exerciseAdapter) // Set the adapter to the ExpandableListView

                // Handle child item clicks to select or unselect exercises
                expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
                    val muscleGroup = exerciseAdapter.getGroup(groupPosition) as String // Get the muscle group
                    val exercise = exerciseMap[muscleGroup]?.get(childPosition) // Get the specific exercise
                    if (exercise != null) {
                        exerciseAdapter.toggleSelection(exercise) // Toggle selection state of the exercise
                    }
                    true // Indicate that the click was handled
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors that occur while fetching data
                Toast.makeText(this@Stronger_function_page_1, "Failed to load exercises", Toast.LENGTH_SHORT).show()
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

        // Set OnClickListener for the "Add Exercises" button to add selected exercises to the workout
        addButton.setOnClickListener {
            val selectedExercises = exerciseAdapter.getSelectedExercises() // Get list of selected exercises
            if (selectedExercises.isNotEmpty()) {
                // Log the number of selected exercises for debugging
                Log.d("Stronger_function_page_1", "Selected exercises: ${selectedExercises.size}")

                // Create an Intent to pass the selected exercises back to the log_workout activity
                val intent = Intent(this, log_workout::class.java)
                intent.putParcelableArrayListExtra(log_workout.EXTRA_SELECTED_EXERCISES, ArrayList(selectedExercises)) // Pass selected exercises as an ArrayList

                setResult(Activity.RESULT_OK, intent) // Set the result to OK with the Intent
                finish() // Finish this activity to return to the previous one (log_workout)
            } else {
                // Show a message if no exercises are selected
                Toast.makeText(this, "No exercises selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Filters the exercises based on the search query entered by the user.
     *
     * @param query The search query string entered by the user.
     */
    private fun filterExercises(query: String) {
        val filteredMap: MutableMap<String, MutableList<Exercise>> = mutableMapOf() // Temporary map for filtered results

        if (query.isEmpty()) {
            filteredMap.putAll(originalExerciseMap) // If query is empty, show all exercises
        } else {
            // Iterate through each muscle group and filter exercises by name
            for ((muscleGroup, exercises) in originalExerciseMap) {
                val filteredExercises = exercises.filter {
                    it.name.contains(query, ignoreCase = true) // Case-insensitive search for exercise name
                }
                if (filteredExercises.isNotEmpty()) {
                    filteredMap[muscleGroup] = filteredExercises.toMutableList() // Add filtered exercises to the map
                }
            }
        }

        exerciseAdapter.updateData(filteredMap) // Update the adapter with the filtered list
    }

    companion object {
        var logWorkoutStarted = false // Tracks whether the log_workout activity has been started
    }
}
