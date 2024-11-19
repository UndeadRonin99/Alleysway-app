// Package declaration
package com.techtitans.alleysway

// Import statements
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.techtitans.alleysway.R
import com.techtitans.alleysway.model.Day
import com.techtitans.alleysway.model.TimeSlot
import java.time.format.DateTimeFormatter

/**
 * Adapter for displaying exercises grouped by muscle groups in an ExpandableListView.
 *
 * @param context The context in which the adapter is operating.
 * @param exerciseMap A map where keys are muscle groups and values are lists of exercises.
 */
class SelectableExerciseAdapter(
    private val context: Context,
    private var exerciseMap: Map<String, List<Exercise>> // Mutable map of exercises
) : BaseExpandableListAdapter() {

    // Set to track selected exercises
    private val selectedExercises = mutableSetOf<Exercise>()

    // List of muscle groups derived from the exercise map
    private val muscleGroups get() = exerciseMap.keys.toList()

    /**
     * Toggles the selection state of an exercise.
     *
     * @param exercise The exercise to toggle.
     */
    fun toggleSelection(exercise: Exercise) {
        if (selectedExercises.contains(exercise)) {
            selectedExercises.remove(exercise)
        } else {
            selectedExercises.add(exercise)
        }
        notifyDataSetChanged() // Refresh the list to reflect changes
    }

    /**
     * Retrieves the list of selected exercises.
     *
     * @return A list of selected exercises.
     */
    fun getSelectedExercises(): List<Exercise> {
        return selectedExercises.toList()
    }

    /**
     * Updates the adapter's data and refreshes the view.
     *
     * @param newExerciseMap The new map of exercises to display.
     */
    fun updateData(newExerciseMap: Map<String, List<Exercise>>) {
        exerciseMap = newExerciseMap
        notifyDataSetChanged()
    }

    /**
     * Provides the view for each muscle group header.
     */
    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView
        if (convertView == null) {
            // Inflate the group header layout
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.group_item, null)
        }
        val groupTitle = convertView!!.findViewById<TextView>(R.id.groupTitle)
        val muscleGroup = getGroup(listPosition) as String
        groupTitle.text = muscleGroup // Set the muscle group name
        return convertView
    }

    /**
     * Provides the view for each exercise item under a muscle group.
     */
    override fun getChildView(
        groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView
        if (convertView == null) {
            // Inflate the child item layout
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.child_item, null)
        }
        val childTitle = convertView!!.findViewById<TextView>(R.id.childTitle)
        val exercise = getChild(groupPosition, childPosition) as Exercise
        childTitle.text = exercise.name // Set the exercise name

        // Highlight selected exercises
        if (selectedExercises.contains(exercise)) {
            convertView.setBackgroundColor(Color.argb(255, 251, 103, 11)) // Highlight color
            childTitle.setTextColor(Color.BLACK)
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT) // Default background
            childTitle.setTextColor(Color.WHITE)
        }

        // Handle click events to select/unselect exercises
        convertView.setOnClickListener {
            toggleSelection(exercise) // Toggle selection state
        }

        return convertView
    }

    override fun getGroup(listPosition: Int): Any {
        return muscleGroups[listPosition] // Return the muscle group at the given position
    }

    override fun getGroupCount(): Int {
        return muscleGroups.size // Total number of muscle groups
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong() // Unique ID for each group
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val muscleGroup = muscleGroups[groupPosition]
        return exerciseMap[muscleGroup]?.size ?: 0 // Number of exercises in the group
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        val muscleGroup = muscleGroups[groupPosition]
        return exerciseMap[muscleGroup]!![childPosition] // Return the exercise at the given position
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong() // Unique ID for each child
    }

    override fun hasStableIds(): Boolean {
        return false // IDs are not stable
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true // Children are selectable
    }
}
