package com.techtitans.alleysway

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for displaying a list of past workouts in a RecyclerView.
 *
 * @param workoutsList List of WorkoutData representing each past workout.
 */
class PastWorkoutsAdapter(
    private val workoutsList: List<WorkoutData>
) : RecyclerView.Adapter<PastWorkoutsAdapter.WorkoutViewHolder>() {

    /**
     * ViewHolder class for individual workout items.
     *
     * @param itemView The view representing a single workout item.
     */
    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutName: TextView = itemView.findViewById(R.id.workoutName)
        val workoutDate: TextView = itemView.findViewById(R.id.workoutDate)
        val totalWeight: TextView = itemView.findViewById(R.id.totalWeight)
        val totalReps: TextView = itemView.findViewById(R.id.totalReps)
        val exercisesRecyclerView: RecyclerView = itemView.findViewById(R.id.exercisesRecyclerView)
    }

    /**
     * Inflates the workout item layout and creates a ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    /**
     * Binds workout data to the ViewHolder, including setting up the nested RecyclerView for exercises.
     */
    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workoutsList[position]

        // Set workout details
        holder.workoutName.text = workout.name
        holder.workoutDate.text = workout.date
        holder.totalWeight.text = "Total Weight: ${workout.totalWeight} kg"
        holder.totalReps.text = "Total Reps: ${workout.totalReps}"

        // Initialize the exercises RecyclerView with WorkoutAdapter
        val exercisesAdapter = WorkoutAdapter(
            exercises = workout.exercises,
            isLogging = false // Read-only mode for past workouts
        )
        holder.exercisesRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.exercisesRecyclerView.adapter = exercisesAdapter
    }

    /**
     * Returns the total number of workouts in the list.
     */
    override fun getItemCount(): Int = workoutsList.size
}
