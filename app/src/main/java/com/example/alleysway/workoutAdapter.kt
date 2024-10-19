package com.example.alleysway

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(
    private val exercises: MutableList<ExerciseData>,
    private val onAddSetClick: (ExerciseData) -> Unit,
    private val onDeleteExerciseClick: (ExerciseData) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val exerciseName: TextView = view.findViewById(R.id.exerciseName)
        val setRecyclerView: RecyclerView = view.findViewById(R.id.setRecyclerView)
        val addSetButton: Button = view.findViewById(R.id.addSetButton)
        val deleteExerciseButton: ImageButton = view.findViewById(R.id.deleteExerciseButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_log, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.exerciseName.text = exercise.name

        // Set RecyclerView for the sets of the exercise
        val setAdapter = SetAdapter(exercise.sets)
        holder.setRecyclerView.adapter = setAdapter
        holder.setRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)

        // Add Set button
        holder.addSetButton.setOnClickListener {
            onAddSetClick(exercise)
        }

        // Delete Exercise button
        holder.deleteExerciseButton.setOnClickListener {
            onDeleteExerciseClick(exercise)
        }
    }

    override fun getItemCount(): Int {
        return exercises.size
    }
}

class SetAdapter(private val sets: MutableList<SetData>) :
    RecyclerView.Adapter<SetAdapter.SetViewHolder>() {

    inner class SetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val repsEditText: EditText = view.findViewById(R.id.repsEditText)
        val weightEditText: EditText = view.findViewById(R.id.weightEditText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_set_log, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val set = sets[position]
        holder.repsEditText.setText(set.reps.toString())
        holder.weightEditText.setText(set.weight.toString())

        // Update set data on text change
        holder.repsEditText.addTextChangedListener {
            set.reps = it.toString().toIntOrNull() ?: 0
        }
        holder.weightEditText.addTextChangedListener {
            set.weight = it.toString().toDoubleOrNull() ?: 0.0
        }
    }

    override fun getItemCount(): Int {
        return sets.size
    }
}
