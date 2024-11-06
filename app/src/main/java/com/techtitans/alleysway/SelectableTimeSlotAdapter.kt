package com.techtitans.alleysway.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.techtitans.alleysway.R
import com.techtitans.alleysway.model.Day
import com.techtitans.alleysway.model.TimeSlot
import java.time.format.DateTimeFormatter

class SelectableTimeSlotAdapter(private val days: List<Day>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_ITEM = 1
    private val selectedTimeSlots = mutableSetOf<TimeSlot>()

    override fun getItemViewType(position: Int): Int {
        val flatPosition = flattenPosition(position)
        return if (flatPosition is Pair<*, *>) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_day_header, parent, false)
            DayViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_timeslot, parent, false)
            TimeSlotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val flatPosition = flattenPosition(position)
        if (holder is DayViewHolder && flatPosition is Pair<*, *>) {
            val day = flatPosition.first as Day
            val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
            val dateString = day.date.format(dateFormatter)
            holder.dayTextView.text = dateString
        }
        else if (holder is TimeSlotViewHolder && flatPosition is TimeSlot) {
            val timeSlot = flatPosition
            holder.timeTextView.text = timeSlot.startTime

            // Set visual state for selected or unselected
            if (selectedTimeSlots.contains(timeSlot)) {
                holder.timeSlotCard.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.orange)
                )
            } else {
                holder.timeSlotCard.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.white)
                )
            }

            holder.itemView.setOnClickListener {
                if (selectedTimeSlots.contains(timeSlot)) {
                    selectedTimeSlots.remove(timeSlot)
                    holder.timeSlotCard.setCardBackgroundColor(
                        ContextCompat.getColor(holder.itemView.context, R.color.white)
                    )
                } else {
                    selectedTimeSlots.add(timeSlot)
                    holder.timeSlotCard.setCardBackgroundColor(
                        ContextCompat.getColor(holder.itemView.context, R.color.orange)
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return days.sumOf { it.timeSlots.size + 1 } // Includes header for each day
    }

    fun getSelectedTimeSlots(): List<TimeSlot> {
        return selectedTimeSlots.toList()
    }

    private fun flattenPosition(position: Int): Any {
        var counter = 0
        for (day in days) {
            if (position == counter) return day to true
            counter++
            if (position < counter + day.timeSlots.size) return day.timeSlots[position - counter]
            counter += day.timeSlots.size
        }
        throw IndexOutOfBoundsException("Invalid position")
    }

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.day_text_view)
    }

    class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.time_text_view)
        val timeSlotCard: CardView = itemView.findViewById(R.id.time_slot_card)
    }
}

