package com.techtitans.alleysway

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
    private val onAddSet: ((ExerciseData) -> Unit)? = null,
    private val onDeleteExercise: ((ExerciseData) -> Unit)? = null,
    private val onUpdateTotalWeight: (() -> Unit)? = null,
    private val isLogging: Boolean = true
) : RecyclerView.Adapter<WorkoutAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseName: TextView = itemView.findViewById(R.id.exerciseName)
        val deleteExerciseButton: ImageButton? = itemView.findViewById(R.id.deleteExerciseButton)
        val addSetButton: MaterialButton? = itemView.findViewById(R.id.addSetButton)
        val setRecyclerView: RecyclerView = itemView.findViewById(R.id.setRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val layoutId = if (isLogging) R.layout.item_exercise_log else R.layout.item_exercise_read_only
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exerciseData = exercises[position]

        // Set exercise name
        holder.exerciseName.text = exerciseData.name

        // Initialize SetAdapter
        val setAdapter = SetAdapter(
            sets = exerciseData.sets,
            onUpdateTotalWeight = onUpdateTotalWeight,
            isLogging = isLogging
        )

        holder.setRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.setRecyclerView.adapter = setAdapter

        if (isLogging) {
            // Existing functionality
            holder.deleteExerciseButton?.setOnClickListener {
                onDeleteExercise?.invoke(exerciseData)
            }

            holder.addSetButton?.setOnClickListener {
                exerciseData.sets.add(SetData(reps = 0, weight = 0.0))
                setAdapter.notifyItemInserted(exerciseData.sets.size - 1)
                onUpdateTotalWeight?.invoke()
            }
        } else {
            // Read-only mode
            holder.deleteExerciseButton?.visibility = View.GONE
            holder.addSetButton?.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = exercises.size
}
