package com.techtitans.alleysway

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class ExerciseExpandableAdapter(
    private val context: Context,
    private val exerciseList: List<Exercise>
) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return exerciseList[listPosition].tips[expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(
        listPosition: Int, expandedListPosition: Int, isLastChild: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView
        val tip = getChild(listPosition, expandedListPosition) as String

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, null)
        }

        val tipTextView = convertView!!.findViewById<TextView>(android.R.id.text1)
        tipTextView.text = tip
        return convertView
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return exerciseList[listPosition].tips.size
    }

    override fun getGroup(listPosition: Int): Any {
        return exerciseList[listPosition]
    }

    override fun getGroupCount(): Int {
        return exerciseList.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView
        val exercise = getGroup(listPosition) as Exercise

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null)
        }

        val exerciseNameTextView = convertView!!.findViewById<TextView>(android.R.id.text1)
        exerciseNameTextView.text = exercise.name

        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}