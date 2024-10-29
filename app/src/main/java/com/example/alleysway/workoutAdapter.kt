package com.example.alleysway

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class WorkoutAdapter(
    private val exercises: MutableList<ExerciseData>,
    private val onAddSet: (ExerciseData) -> Unit,
    private val onDeleteExercise: (ExerciseData) -> Unit,
    private val onUpdateTotalWeight: () -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseName: TextView = itemView.findViewById(R.id.exerciseName)
        val deleteExerciseButton: ImageButton = itemView.findViewById(R.id.deleteExerciseButton)
        val addSetButton: MaterialButton = itemView.findViewById(R.id.addSetButton)
        val setRecyclerView: RecyclerView = itemView.findViewById(R.id.setRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise_log, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exerciseData = exercises[position]

        // Set exercise name
        holder.exerciseName.text = exerciseData.name

        // Delete exercise
        holder.deleteExerciseButton.setOnClickListener {
            onDeleteExercise(exerciseData)
        }

        // Initialize SetAdapter only once per ExerciseViewHolder
        val setAdapter = SetAdapter(exerciseData.sets, {
            onUpdateTotalWeight()
        })

        holder.setRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.setRecyclerView.adapter = setAdapter

        // Handle Add Set button
        holder.addSetButton.setOnClickListener {
            exerciseData.sets.add(SetData())
            setAdapter.notifyItemInserted(exerciseData.sets.size - 1)
            onUpdateTotalWeight()
        }
    }

    override fun getItemCount(): Int = exercises.size
}
