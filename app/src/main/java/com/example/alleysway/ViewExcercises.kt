package com.example.alleysway

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
import com.google.firebase.database.*

class ViewExcercises : AppCompatActivity() {
    private lateinit var expandableListView: ExpandableListView
    private lateinit var exerciseAdapter: GroupedExerciseAdapter
    private lateinit var searchEditText: EditText
    private val exerciseMap: MutableMap<String, MutableList<Exercise>> = mutableMapOf()
    private var originalExerciseMap: MutableMap<String, MutableList<Exercise>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_excercises)

        expandableListView = findViewById(R.id.expandableListView)
        searchEditText = findViewById(R.id.searchExercise)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // Fetch exercises from Firebase
        val database = FirebaseDatabase.getInstance().getReference("exercises")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                exerciseMap.clear()
                for (exerciseSnapshot in snapshot.children) {
                    val exercise = exerciseSnapshot.getValue(Exercise::class.java)
                    if (exercise != null) {
                        // Group exercises by their mainMuscle and sort them
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

                // Sort each group alphabetically
                for (entry in exerciseMap) {
                    entry.value.sortBy { it.name }
                }

                // Set adapter for ExpandableListView
                exerciseAdapter = GroupedExerciseAdapter(this@ViewExcercises, exerciseMap)
                expandableListView.setAdapter(exerciseAdapter)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewExcercises, "Failed to load exercises", Toast.LENGTH_SHORT).show()
            }
        })

        // Add TextWatcher for search functionality
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterExercises(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Set listener for clicking on an exercise
        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val muscleGroup = exerciseAdapter.getGroup(groupPosition) as String
            val exercise = exerciseAdapter.getChild(groupPosition, childPosition) as Exercise
            showExerciseDetails(exercise)
            true
        }
    }

    // Method to filter exercises based on search query
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

    private fun showExerciseDetails(exercise: Exercise) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.exercise_details)

        val exerciseName = dialog.findViewById<TextView>(R.id.exerciseName)
        val exerciseGif = dialog.findViewById<ImageView>(R.id.exerciseGif)
        val tip1 = dialog.findViewById<TextView>(R.id.tip1)
        val tip2 = dialog.findViewById<TextView>(R.id.tip2)
        val tip3 = dialog.findViewById<TextView>(R.id.tip3)

        exerciseName.text = exercise.name

        // Load GIF using Glide
        Glide.with(this)
            .load(exercise.imageUrl)
            .into(exerciseGif)

        // Populate tips
        if (exercise.tips.isNotEmpty()) tip1.text = exercise.tips[0]
        if (exercise.tips.size > 1) tip2.text = exercise.tips[1]
        if (exercise.tips.size > 2) tip3.text = exercise.tips[2]

        dialog.show()
    }
}
