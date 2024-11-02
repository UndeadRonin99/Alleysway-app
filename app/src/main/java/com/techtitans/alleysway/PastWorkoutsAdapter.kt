// File: app/src/main/java/com/example/alleysway/PastWorkoutsAdapter.kt
package com.techtitans.alleysway

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PastWorkoutsAdapter(
    private val workoutsList: List<WorkoutData>
) : RecyclerView.Adapter<PastWorkoutsAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutName: TextView = itemView.findViewById(R.id.workoutName)
        val workoutDate: TextView = itemView.findViewById(R.id.workoutDate)
        val totalWeight: TextView = itemView.findViewById(R.id.totalWeight)
        val totalReps: TextView = itemView.findViewById(R.id.totalReps)
        val exercisesRecyclerView: RecyclerView = itemView.findViewById(R.id.exercisesRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workoutsList[position]

        holder.workoutName.text = workout.name
        holder.workoutDate.text = workout.date
        holder.totalWeight.text = "Total Weight: ${workout.totalWeight} kg"
        holder.totalReps.text = "Total Reps: ${workout.totalReps}"

        // Set up the exercises RecyclerView
        val exercisesAdapter = WorkoutAdapter(
            exercises = workout.exercises,
            isLogging = false
        )
        holder.exercisesRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.exercisesRecyclerView.adapter = exercisesAdapter
    }

    override fun getItemCount(): Int = workoutsList.size
}
