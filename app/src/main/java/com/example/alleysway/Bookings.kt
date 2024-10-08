package com.example.alleysway

import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.database.*
import com.example.alleysway.R
import java.text.SimpleDateFormat
import java.util.Locale

class Bookings : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var databaseReference: DatabaseReference
    private val liveHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)  // Current hour

    private val dayLabels = arrayOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    private var hourlyAttendanceData = mapOf<String, List<BarEntry>>()  // Stores hourly data for each day

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookings)

        barChart = findViewById(R.id.popularTimesChart)
        databaseReference = FirebaseDatabase.getInstance().getReference("attendance")

        setupBarChartStyle()
        loadWeeklyData()
        setupDayButtons()  // Setup buttons for day selection
    }

    private fun loadWeeklyData() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempHourlyData = mutableMapOf<String, List<BarEntry>>()

                for (daySnapshot in snapshot.children) {
                    val dateKey = daySnapshot.key ?: continue  // Example: "2024-10-08"
                    val dayOfWeek = getDayOfWeek(dateKey)  // Get day of the week (e.g., "TUE" for Tuesday)
                    val hourlyDataForDay = mutableListOf<BarEntry>()

                    for (hourSnapshot in daySnapshot.children) {
                        val hour = hourSnapshot.key?.toInt() ?: 0  // Get the hour (e.g., 18 for 18:00)
                        val count = hourSnapshot.getValue(Int::class.java) ?: 0
                        hourlyDataForDay.add(BarEntry(hour.toFloat(), count.toFloat()))  // Use the hour as the X-value
                    }

                    tempHourlyData[dayOfWeek] = hourlyDataForDay  // Map data to correct day of the week
                }

                hourlyAttendanceData = tempHourlyData  // Store hourly data
                val currentDay = getDayOfWeek(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time))  // Get current day
                loadHourlyDataForDay(currentDay)  // Load data for the current day
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Bookings, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadHourlyDataForDay(day: String) {
        val hourlyData = hourlyAttendanceData[day] ?: listOf()
        val completeHourlyData = Array(24) { i -> BarEntry(i.toFloat(), 0f) }.toMutableList()  // Fill with zeros
        hourlyData.forEach { entry ->
            completeHourlyData[entry.x.toInt()] = entry  // Replace zeros with actual data where available
        }
        updateBarChart(completeHourlyData, generateHourLabels())
    }

    private fun setupDayButtons() {
        val dayButtons = mapOf(
            "MON" to findViewById<Button>(R.id.btnMonday),
            "TUE" to findViewById<Button>(R.id.btnTuesday),
            "WED" to findViewById<Button>(R.id.btnWednesday),
            "THU" to findViewById<Button>(R.id.btnThursday),
            "FRI" to findViewById<Button>(R.id.btnFriday),
            "SAT" to findViewById<Button>(R.id.btnSaturday),
            "SUN" to findViewById<Button>(R.id.btnSunday)
        )

        for ((day, button) in dayButtons) {
            button.setOnClickListener {
                loadHourlyDataForDay(day)
            }
        }
    }

    private fun updateBarChart(entries: List<BarEntry>, labels: Array<String>) {
        val barDataSet = BarDataSet(entries, "Hourly Attendance")
        barDataSet.color = Color.parseColor("#1E88E5")  // Default bar color (blue)
        barDataSet.setDrawValues(false)  // Disable value text on bars

        // Highlight the live hour in red, only for the current day
        entries.find { it.x.toInt() == liveHour }?.let {
            barDataSet.setColors(*IntArray(entries.size) { i ->
                if (i == liveHour) Color.RED else Color.parseColor("#1E88E5")
            })
        }

        val barData = BarData(barDataSet)
        barChart.data = barData
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.invalidate()  // Refresh the chart with the new data
    }

    private fun setupBarChartStyle() {
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setPinchZoom(false)

        // Styling x-axis (moved to the bottom)
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM  // Moved to the bottom
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 12f

        // Disable left Y-axis
        barChart.axisLeft.isEnabled = false  // Disable the left Y-axis

        // Disable right Y-axis
        barChart.axisRight.isEnabled = false

        // Remove the legend
        barChart.legend.isEnabled = false

        // Set background color
        barChart.setBackgroundColor(Color.WHITE)

        // Enable horizontal scrolling
        barChart.isDragEnabled = true
        barChart.setScaleEnabled(false)  // Disable zooming in/out
        barChart.setVisibleXRangeMaximum(6f)  // Show only 6 hours at a time
    }

    // Show the hours in HH:00 format
    private fun generateHourLabels(): Array<String> {
        return Array(24) { i -> String.format("%02d:00", i) }  // Labels: 00:00, 01:00, ..., 23:00
    }

    // Function to convert a date string to a day of the week (e.g., "2024-10-08" -> "TUE")
    private fun getDayOfWeek(dateString: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateString)
        val calendar = Calendar.getInstance()
        calendar.time = date

        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            Calendar.SUNDAY -> "SUN"
            else -> "MON"  // Default to Monday in case of error
        }
    }

    // Live sample data load
    private fun loadSampleData() {
        val sampleData = mapOf(
            "2024-10-08" to mapOf(
                "06" to 5,  // 5 people checked in at 6 AM
                "09" to 15,  // 15 people checked in at 9 AM
                "12" to 25,  // 25 people checked in at 12 PM
                "18" to 50,  // 50 people checked in at 6 PM (Live hour)
                "19" to 45,  // 45 people checked in at 7 PM
                "20" to 30   // 30 people checked in at 8 PM
            )
            // Add more days and data here...
        )

        val databaseReference = FirebaseDatabase.getInstance().getReference("attendance")
        databaseReference.setValue(sampleData).addOnSuccessListener {
            Toast.makeText(this, "Sample data loaded successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load sample data", Toast.LENGTH_SHORT).show()
        }
    }
}
