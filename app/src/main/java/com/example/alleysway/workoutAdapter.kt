package com.example.alleysway

import android.graphics.Color
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
    private val onDeleteExerciseClick: (ExerciseData) -> Unit,
    private val updateTotalWeight: () -> Unit  // Pass the updateTotalWeight callback
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
        val setAdapter = SetAdapter(exercise.sets, updateTotalWeight, { set ->
            // Remove the set when the delete button is clicked
            exercise.sets.remove(set)
            notifyDataSetChanged()  // Refresh the RecyclerView
            updateTotalWeight()  // Recalculate total weight after removing a set
        })


        holder.setRecyclerView.adapter = setAdapter
        holder.setRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)

        // Add Set button
        holder.addSetButton.setOnClickListener {
            onAddSetClick(exercise)
            updateTotalWeight() // Update weight after adding a set
        }

        // Delete Exercise button
        holder.deleteExerciseButton.setOnClickListener {
            onDeleteExerciseClick(exercise)
            updateTotalWeight() // Update weight after removing an exercise
        }
    }

    override fun getItemCount(): Int {
        return exercises.size
    }
}

class SetAdapter(
    private val sets: MutableList<SetData>,
    private val onTextChanged: () -> Unit,
    private val onRemoveSetClick: (SetData) -> Unit  // Callback for removing a set
) : RecyclerView.Adapter<SetAdapter.SetViewHolder>() {

    inner class SetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val setNumber: TextView = view.findViewById(R.id.SetNumber)
        val repsEditText: EditText = view.findViewById(R.id.repsEditText)
        val weightEditText: EditText = view.findViewById(R.id.weightEditText)
        val removeSetButton: ImageButton = view.findViewById(R.id.deleteSetButton)
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

        holder.setNumber.text = "${position+1}"
        holder.setNumber.setTextColor(Color.WHITE)

        // TextChangeListener for reps EditText
        holder.repsEditText.addTextChangedListener {
            val reps = it.toString().toIntOrNull() ?: 0
            set.reps = reps
            onTextChanged() // Update total weight dynamically
        }

        // TextChangeListener for weight EditText
        holder.weightEditText.addTextChangedListener {
            val weight = it.toString().toDoubleOrNull() ?: 0.0
            set.weight = weight
            onTextChanged() // Update total weight dynamically
        }
        holder.removeSetButton.setOnClickListener {
            onRemoveSetClick(set)
        }
    }

    override fun getItemCount(): Int {
        return sets.size
    }
}
