package com.techtitans.alleysway

// Necessary Android and Kotlin imports
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

// WorkoutAdapter class definition with constructor parameters for handling exercises and user interactions
class WorkoutAdapter(
    private val exercises: MutableList<ExerciseData>, // List of exercises to be displayed
    private val onAddSet: ((ExerciseData) -> Unit)? = null, // Lambda function to handle adding sets
    private val onDeleteExercise: ((ExerciseData) -> Unit)? = null, // Lambda function to handle deleting exercises
    private val onUpdateTotalWeight: (() -> Unit)? = null, // Lambda function to update total weight when a set is added/modified
    private val isLogging: Boolean = true // Flag to switch between logging mode and read-only mode
) : RecyclerView.Adapter<WorkoutAdapter.ExerciseViewHolder>() { // Subclassing RecyclerView.Adapter

    // ViewHolder class to hold UI references
    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseName: TextView = itemView.findViewById(R.id.exerciseName) // TextView for exercise name
        val deleteExerciseButton: ImageButton? = itemView.findViewById(R.id.deleteExerciseButton) // Button to delete exercise
        val addSetButton: MaterialButton? = itemView.findViewById(R.id.addSetButton) // Button to add a new set
        val setRecyclerView: RecyclerView = itemView.findViewById(R.id.setRecyclerView) // RecyclerView for sets within an exercise
    }

    // Creates the ViewHolder for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val layoutId = if (isLogging) R.layout.item_exercise_log else R.layout.item_exercise_read_only // Layout selection based on logging flag
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false) // Inflate the layout
        return ExerciseViewHolder(view) // Return a new instance of ExerciseViewHolder
    }

    // Binds data to each ViewHolder
    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exerciseData = exercises[position] // Get the exercise data at this position

        // Set the exercise name in the TextView
        holder.exerciseName.text = exerciseData.name

        // Initialize and set up the adapter for the sets RecyclerView
        val setAdapter = SetAdapter(
            sets = exerciseData.sets,
            onUpdateTotalWeight = onUpdateTotalWeight,
            isLogging = isLogging
        )
        holder.setRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context) // Set layout manager for sets
        holder.setRecyclerView.adapter = setAdapter // Set adapter for sets

        if (isLogging) {
            // Handling for logging mode
            holder.deleteExerciseButton?.setOnClickListener {
                onDeleteExercise?.invoke(exerciseData) // Invoke delete exercise lambda when button is clicked
            }

            holder.addSetButton?.setOnClickListener {
                exerciseData.sets.add(SetData(reps = 0, weight = 0.0)) // Add a new set to the list
                setAdapter.notifyItemInserted(exerciseData.sets.size - 1) // Notify the adapter of the new item
                onUpdateTotalWeight?.invoke() // Invoke update total weight lambda
            }
        } else {
            // Handling for read-only mode, make delete and add buttons invisible
            holder.deleteExerciseButton?.visibility = View.GONE
            holder.addSetButton?.visibility = View.GONE
        }
    }

    // Return the size of the exercises list
    override fun getItemCount(): Int = exercises.size
}
