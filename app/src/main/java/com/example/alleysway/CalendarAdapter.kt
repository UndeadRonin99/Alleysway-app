package com.example.alleysway

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alleysway.data.CalendarDay
import com.example.alleysway.data.MonthData
import java.time.LocalDate

class CalendarAdapter(
    private val days: List<CalendarDay>
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        Log.d("CalendarAdapter", "Position: $position, Date: ${day.date}, Attendance Count: ${day.attendanceCount}")
        if (day.date != null) {
            //holder.dayText.text = day.date.dayOfMonth.toString()
            val backgroundColor = getColorForAttendance(day.attendanceCount)
            holder.dayText.setBackgroundColor(backgroundColor)
        } else {
            holder.dayText.text = ""
            holder.dayText.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int = days.size

    private fun getColorForAttendance(count: Int): Int {
        return when (count) {
            0 -> Color.parseColor("#EEEEEE") // No attendance
            1 -> Color.parseColor("#FC670B")
            else -> Color.parseColor("#196127")
        }
    }

    fun getDateAtPosition(position: Int): LocalDate? {
        return days.getOrNull(position)?.date
    }
}

class MonthHeaderAdapter(private val months: List<MonthData>) : RecyclerView.Adapter<MonthHeaderAdapter.MonthViewHolder>() {

    inner class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val monthText: TextView = itemView.findViewById(R.id.monthText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month_header, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val monthData = months[position]
        holder.monthText.text = monthData.name

        // Set width based on the number of days in the month
        val dayWidth = 5 // Example fixed width per day in dp
        val monthWidth = dayWidth * monthData.daysInMonth / 4 // Assuming 4 weeks per month

        // Convert dp to pixels for setting width
        val displayMetrics = holder.itemView.context.resources.displayMetrics
        val monthWidthPx = (monthWidth * displayMetrics.density).toInt()
        Log.d("MonthHeaderAdapter", "Setting width for ${monthData.name}: $monthWidthPx px")


        holder.monthText.layoutParams = ViewGroup.LayoutParams(monthWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Adjust text size based on width
        holder.monthText.textSize = 10f
    }

    override fun getItemCount(): Int = months.size
}
