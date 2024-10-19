package com.example.alleysway

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ExpandableListView
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
    private val exerciseMap: MutableMap<String, MutableList<Exercise>> = mutableMapOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stronger_function_page1)

        expandableListView = findViewById(R.id.expandableListView)
        addButton = findViewById(R.id.addExercisesBtn)

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

        // Handle "Add Exercises" button click
        addButton.setOnClickListener {
            val selectedExercises = exerciseAdapter.getSelectedExercises()
            // Add selected exercises to the workout
            // (You can handle how to store or use the selected exercises here)
            if(selectedExercises.isNotEmpty()){
                Toast.makeText(this, "${selectedExercises.size} exercises added to the workout!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Stronger_function_page_1::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this, "No exercises selected", Toast.LENGTH_SHORT).show()
            }
        }
    }
}