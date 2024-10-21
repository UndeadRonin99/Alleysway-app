package com.example.alleysway

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

class Stronger_function_page_1 : AppCompatActivity() {

    private lateinit var expandableListView: ExpandableListView
    private lateinit var exerciseAdapter: SelectableExerciseAdapter
    private lateinit var addButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var backButton: ImageView  // Back button reference
    private val exerciseMap: MutableMap<String, MutableList<Exercise>> = mutableMapOf()
    private var originalExerciseMap: MutableMap<String, MutableList<Exercise>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stronger_function_page1)

        expandableListView = findViewById(R.id.expandableListView)
        addButton = findViewById(R.id.addExercisesBtn)
        searchEditText = findViewById(R.id.searchExercise)
        backButton = findViewById(R.id.btnBack)  // Initialize the back button

        // Back button functionality: Go back to the Workouts page
        backButton.setOnClickListener {
            val intent = Intent(this, Workouts::class.java)  // Change to the correct Workouts activity class
            startActivity(intent)
            finish()  // Optional: finish this activity to prevent returning back to this screen
        }

        // Fetch exercises from Firebase
        val database = FirebaseDatabase.getInstance().getReference("exercises")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                exerciseMap.clear()
                for (exerciseSnapshot in snapshot.children) {
                    val exercise = exerciseSnapshot.getValue(Exercise::class.java)
                    if (exercise != null) {
                        val muscleGroup = exercise.mainMuscle
                        if (exerciseMap.containsKey(muscleGroup)) {
                            exerciseMap[muscleGroup]?.add(exercise)
                        } else {
                            exerciseMap[muscleGroup] = mutableListOf(exercise)
                        }
                    }
                }

                // Save original exerciseMap for searching
                originalExerciseMap = HashMap(exerciseMap)

                // Set adapter for ExpandableListView
                exerciseAdapter = SelectableExerciseAdapter(this@Stronger_function_page_1, exerciseMap)
                expandableListView.setAdapter(exerciseAdapter)

                // Handle item click to select/unselect exercises
                expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
                    val muscleGroup = exerciseAdapter.getGroup(groupPosition) as String
                    val exercise = exerciseMap[muscleGroup]?.get(childPosition)
                    if (exercise != null) {
                        exerciseAdapter.toggleSelection(exercise)
                    }
                    true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Stronger_function_page_1, "Failed to load exercises", Toast.LENGTH_SHORT).show()
            }
        })

        // Handle search input
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterExercises(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Handle "Add Exercises" button click
        addButton.setOnClickListener {
            val selectedExercises = exerciseAdapter.getSelectedExercises()
            if (selectedExercises.isNotEmpty()) {
                // Log selected exercises
                Log.d("Stronger_function_page_1", "Selected exercises: ${selectedExercises.size}")

                val intent = Intent(this, log_workout::class.java)
                intent.putParcelableArrayListExtra(log_workout.EXTRA_SELECTED_EXERCISES, ArrayList(selectedExercises))

                setResult(Activity.RESULT_OK, intent)
                finish() // This will take the user back to the log_workout without closing it
            } else {
                Toast.makeText(this, "No exercises selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Filter exercises based on search query
    private fun filterExercises(query: String) {
        val filteredMap: MutableMap<String, MutableList<Exercise>> = mutableMapOf()

        if (query.isEmpty()) {
            filteredMap.putAll(originalExerciseMap)  // Show the original list if query is empty
        } else {
            for ((muscleGroup, exercises) in originalExerciseMap) {
                val filteredExercises = exercises.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                if (filteredExercises.isNotEmpty()) {
                    filteredMap[muscleGroup] = filteredExercises.toMutableList()
                }
            }
        }

        exerciseAdapter.updateData(filteredMap)  // Update adapter with filtered list
    }

    companion object {
        var logWorkoutStarted = false // This tracks whether log_workout has been started
    }
}
