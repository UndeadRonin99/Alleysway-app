package com.example.alleysway

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class GroupedExerciseAdapter(
    private val context: Context,
    private var exerciseMap: MutableMap<String, MutableList<Exercise>>
) : BaseExpandableListAdapter() {

    private var muscleGroups = exerciseMap.keys.toList()

    // Method to update data and refresh the list
    fun updateData(newExerciseMap: MutableMap<String, MutableList<Exercise>>) {
        exerciseMap = newExerciseMap
        muscleGroups = exerciseMap.keys.toList()
        notifyDataSetChanged()
    }

    // Get the group view (main muscle group)
    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        var view = convertView
        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.group_item, null)  // Use your custom group layout
        }
        val groupTitle = view!!.findViewById<TextView>(R.id.groupTitle)
        val muscleGroup = getGroup(listPosition) as String
        groupTitle.text = muscleGroup
        return view
    }

    // Get the child view (exercises under each group)
    override fun getChildView(
        groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        var view = convertView
        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.child_item, null)  // Use your custom child layout
        }
        val childTitle = view!!.findViewById<TextView>(R.id.childTitle)
        val exercise = getChild(groupPosition, childPosition) as Exercise
        childTitle.text = exercise.name
        return view
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
