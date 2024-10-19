package com.example.alleysway

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class log_workout : AppCompatActivity() {

    private lateinit var exerciseAdapter: WorkoutAdapter
    private val exerciseList = mutableListOf<ExerciseData>() // stores excercise data for logging sets

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_workout)

        val recyclerView: RecyclerView = findViewById(R.id.exerciseRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter for exercises
        exerciseAdapter = WorkoutAdapter(exerciseList, { exercise ->
            exercise.sets.add(SetData())
            exerciseAdapter.notifyDataSetChanged()
        }, { exercise ->
            exerciseList.remove(exercise)
            exerciseAdapter.notifyDataSetChanged()
        })

        recyclerView.adapter = exerciseAdapter

        val addExerciseButton: Button = findViewById(R.id.addExerciseButton)
        addExerciseButton.setOnClickListener {
            // Start Stronger_function_page_1 to select exercises
            val intent = Intent(this, Stronger_function_page_1::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_EXERCISES)
        }
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

    companion object {
        const val EXTRA_SELECTED_EXERCISES = "extra_selected_exercises"
        const val REQUEST_CODE_ADD_EXERCISES = 100
    }
}