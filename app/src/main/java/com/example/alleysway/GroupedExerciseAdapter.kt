package com.example.alleysway

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class GroupedExerciseAdapter(
private val context: Context,
private val exerciseMap: Map<String, List<Exercise>>
) : BaseExpandableListAdapter() {

    private val muscleGroups = exerciseMap.keys.toList()

    // Get the group view (main muscle group)
    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.group_item, null)  // Use custom group layout
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
            convertView = inflater.inflate(R.layout.child_item, null)  // Use custom child layout
        }
        val childTitle = convertView!!.findViewById<TextView>(R.id.childTitle)
        val exercise = getChild(groupPosition, childPosition) as Exercise
        childTitle.text = exercise.name
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
