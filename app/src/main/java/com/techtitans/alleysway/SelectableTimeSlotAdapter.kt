// Package declaration
package com.techtitans.alleysway.adapter

// Import statements
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

/**
 * RecyclerView Adapter for displaying selectable time slots grouped by days.
 *
 * @param days List of Day objects, each containing a list of TimeSlots.
 */
class SelectableTimeSlotAdapter(private val days: List<Day>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // View type constants for headers and items
    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_ITEM = 1

    // Set to keep track of selected time slots
    private val selectedTimeSlots = mutableSetOf<TimeSlot>()

    /**
     * Determines the view type based on the position.
     * Returns VIEW_TYPE_HEADER for day headers and VIEW_TYPE_ITEM for time slots.
     */
    override fun getItemViewType(position: Int): Int {
        val flatPosition = flattenPosition(position)
        return if (flatPosition is Pair<*, *>) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    /**
     * Creates appropriate ViewHolder based on the view type.
     */
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

    /**
     * Binds data to the ViewHolder based on its type.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val flatPosition = flattenPosition(position)
        if (holder is DayViewHolder && flatPosition is Pair<*, *>) {
            val day = flatPosition.first as Day
            val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
            holder.dayTextView.text = day.date.format(dateFormatter)
        }
        else if (holder is TimeSlotViewHolder && flatPosition is TimeSlot) {
            val timeSlot = flatPosition
            holder.timeTextView.text = timeSlot.startTime

            // Update background color based on selection state
            if (selectedTimeSlots.contains(timeSlot)) {
                holder.timeSlotCard.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.orange)
                )
            } else {
                holder.timeSlotCard.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.white)
                )
            }

            // Toggle selection on click
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

    /**
     * Returns the total number of items, including headers.
     */
    override fun getItemCount(): Int {
        return days.sumOf { it.timeSlots.size + 1 } // +1 for each day header
    }

    /**
     * Retrieves the list of selected time slots.
     */
    fun getSelectedTimeSlots(): List<TimeSlot> {
        return selectedTimeSlots.toList()
    }

    /**
     * Flattens the position to determine if it's a header or a time slot.
     *
     * @param position The adapter position.
     * @return Pair of Day and Boolean for headers, or TimeSlot for items.
     */
    private fun flattenPosition(position: Int): Any {
        var counter = 0
        for (day in days) {
            if (position == counter) return day to true // Header
            counter++
            if (position < counter + day.timeSlots.size) return day.timeSlots[position - counter] // Item
            counter += day.timeSlots.size
        }
        throw IndexOutOfBoundsException("Invalid position")
    }

    /**
     * ViewHolder for day headers.
     */
    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.day_text_view)
    }

    /**
     * ViewHolder for time slots.
     */
    class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.time_text_view)
        val timeSlotCard: CardView = itemView.findViewById(R.id.time_slot_card)
    }
}
