package com.example.alleysway

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class SelectableExerciseAdapter(
    private val context: Context,
    private var exerciseMap: Map<String, List<Exercise>> // Make exerciseMap mutable
) : BaseExpandableListAdapter() {

    private val selectedExercises = mutableSetOf<Exercise>()  // Track selected exercises
    private val muscleGroups get() = exerciseMap.keys.toList()  // Dynamic list of muscle groups

    // Toggle selection for an exercise
    fun toggleSelection(exercise: Exercise) {
        if (selectedExercises.contains(exercise)) {
            selectedExercises.remove(exercise)
        } else {
            selectedExercises.add(exercise)
        }
        notifyDataSetChanged()
    }

    // Get selected exercises
    fun getSelectedExercises(): List<Exercise> {
        return selectedExercises.toList()
    }

    // Update the dataset in the adapter and refresh the ExpandableListView
    fun updateData(newExerciseMap: Map<String, List<Exercise>>) {
        exerciseMap = newExerciseMap
        notifyDataSetChanged()
    }

    // Get the group view (main muscle group)
    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.group_item, null)
        }
        val groupTitle = convertView!!.findViewById<TextView>(R.id.groupTitle)
        val muscleGroup = getGroup(listPosition) as String
        groupTitle.text = muscleGroup
        return convertView
    }

    // Get the child view (exercises under each group)
    override fun getChildView(
        groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.child_item, null)
        }
        val childTitle = convertView!!.findViewById<TextView>(R.id.childTitle)
        val exercise = getChild(groupPosition, childPosition) as Exercise
        childTitle.text = exercise.name

        // Update the background color or style based on selection
        if (selectedExercises.contains(exercise)) {
            convertView.setBackgroundColor(Color.argb(255, 251, 103, 11))  // Highlight selected exercises
            childTitle.setTextColor(Color.BLACK)
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT)  // Reset background for unselected exercises
            childTitle.setTextColor(Color.WHITE)
        }

        return convertView
    }

    override fun getGroup(listPosition: Int): Any {
        return muscleGroups[listPosition]
    }

    override fun getGroupCount(): Int {
        return muscleGroups.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val muscleGroup = muscleGroups[groupPosition]
        return exerciseMap[muscleGroup]?.size ?: 0
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        val muscleGroup = muscleGroups[groupPosition]
        return exerciseMap[muscleGroup]!![childPosition]
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}
