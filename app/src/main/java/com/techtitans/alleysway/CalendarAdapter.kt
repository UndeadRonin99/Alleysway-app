package com.techtitans.alleysway

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.techtitans.alleysway.data.CalendarDay
import com.techtitans.alleysway.data.MonthData
import java.time.LocalDate


// Adapter for displaying the days in the calendar.
class CalendarAdapter(
    private val days: List<CalendarDay>// List of CalendarDay objects that contain data for each day.
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {


    // ViewHolder that holds the view for each individual day in the calendar.
    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)// Reference to the TextView displaying the day number.
    }
    
// Inflates the calendar day layout and creates a new ViewHolder instance.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)// Inflate the day item layout.
        return DayViewHolder(view)// Return a new ViewHolder with the inflated view.
    }
    
    // Binds the data for each calendar day to the ViewHolder.
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]// Get the CalendarDay object for the current position.
        Log.d("CalendarAdapter", "Position: $position, Date: ${day.date}, Attendance Count: ${day.attendanceCount}")
        if (day.date != null) {
            //holder.dayText.text = day.date.dayOfMonth.toString() // This line is commented out, but it could display the day number.
            val backgroundColor = getColorForAttendance(day.attendanceCount)// Determine the background color based on attendance count.
            holder.dayText.setBackgroundColor(backgroundColor)// Set the background color of the day TextView.
        } else {
            holder.dayText.text = ""// If there is no date, leave the TextView empty.
            holder.dayText.setBackgroundColor(Color.TRANSPARENT)// Set transparent background if there is no date.

        }
    }
    // Returns the total number of days in the list (i.e., number of calendar days).
    override fun getItemCount(): Int = days.size
    // Determines the background color based on the attendance count.
    private fun getColorForAttendance(count: Int): Int {
        return when (count) {
            0 -> Color.parseColor("#EEEEEE") // No attendance, light gray color.
            1 -> Color.parseColor("#FC670B")// Low attendance, orange color.
            else -> Color.parseColor("#196127")// High attendance, green color.
        }
    }
    // Function to get the date at a specific position in the list of days.
    fun getDateAtPosition(position: Int): LocalDate? {
        return days.getOrNull(position)?.date// Return the date for the given position, or null if it doesn't exist.
    }
}
// Adapter for displaying the month headers in the calendar.
class MonthHeaderAdapter(private val months: List<MonthData>) : RecyclerView.Adapter<MonthHeaderAdapter.MonthViewHolder>() {
    // ViewHolder that holds the view for each month header.
    inner class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val monthText: TextView = itemView.findViewById(R.id.monthText)// Reference to the TextView displaying the month name.
    }
    // Inflates the month header layout and creates a new ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month_header, parent, false)// Inflate the month header item layout.
        return MonthViewHolder(view)// Return a new ViewHolder with the inflated view.
    }
    // Binds the month data to the ViewHolder.
    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val monthData = months[position]// Get the MonthData object for the current position.
        holder.monthText.text = monthData.name// Set the month name in the TextView.

        // Set the width of the month header based on the number of days in the month.
        val dayWidth = 5 // Example fixed width per day in dp.
        val monthWidth = dayWidth * monthData.daysInMonth / 4  // Calculate the width based on the number of days (assuming 4 weeks per month).

        // Convert dp to pixels for setting width
        val displayMetrics = holder.itemView.context.resources.displayMetrics
        val monthWidthPx = (monthWidth * displayMetrics.density).toInt() // Convert calculated width to pixels.
        Log.d("MonthHeaderAdapter", "Setting width for ${monthData.name}: $monthWidthPx px")// Log the calculated width for debugging

        // Set the layout parameters for the month header to adjust its width.
        holder.monthText.layoutParams = ViewGroup.LayoutParams(monthWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Adjust the text size based on the width of the month header.
        holder.monthText.textSize = 10f
    }

    override fun getItemCount(): Int = months.size// Set the text size to a fixed value.
}























































































































































































































































































































