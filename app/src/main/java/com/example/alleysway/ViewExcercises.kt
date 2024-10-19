package com.example.alleysway

import android.app.Dialog
import android.os.Bundle
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

class ViewExcercises : AppCompatActivity() {
    private lateinit var expandableListView: ExpandableListView
    private lateinit var exerciseAdapter: GroupedExerciseAdapter
    private val exerciseMap: MutableMap<String, MutableList<Exercise>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_excercises)
        expandableListView = findViewById(R.id.expandableListView)

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

        // Set listener for clicking on an exercise
        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val muscleGroup = exerciseAdapter.getGroup(groupPosition) as String
            val exercise = exerciseMap[muscleGroup]?.get(childPosition)
            if (exercise != null) {
                showExerciseDetails(exercise)
            }
            true
        }
    }

    private fun showExerciseDetails(exercise: Exercise) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.exercise_details)

        val exerciseName = dialog.findViewById<TextView>(R.id.exerciseName)
        val exerciseGif = dialog.findViewById<ImageView>(R.id.exerciseGif)
        var tip1 = dialog.findViewById<TextView>(R.id.tip1)
        var tip2 = dialog.findViewById<TextView>(R.id.tip2)
        var tip3 = dialog.findViewById<TextView>(R.id.tip3)

        exerciseName.text = exercise.name

        // Load GIF using Glide
        Glide.with(this)
            .load(exercise.imageUrl)
            .into(exerciseGif)

        tip1.textColors
        // Populate tips
        if (exercise.tips.isNotEmpty()) tip1.text = exercise.tips[0]
        if (exercise.tips.size > 1) tip2.text = exercise.tips[1]
        if (exercise.tips.size > 2) tip3.text = exercise.tips[2]

        dialog.show()
    }
}