package com.techtitans.alleysway
// Import statements for required libraries and modules.

import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale


// Main class for managing the Bookings activity.
class Bookings : AppCompatActivity() {
    // Variables to store UI elements and data.
    private lateinit var barChart: BarChart// Chart to display hourly attendance data.
    private lateinit var databaseReference: DatabaseReference// Firebase reference for data operations.
    private lateinit var btnMakeBooking: ImageView// Button for navigating to booking creation.
    private lateinit var txtNumberOfPTs: TextView// TextView for displaying the number of available personal trainers.


     // Constants for current time and day of the week.
    private val liveHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)// Current hour of the day.
    private val todayDayOfWeek = getCurrentDayOfWeek() // Get today's day in the same format as your data (e.g., "MON", "TUE") // Current day of the week in a short string format.
    private val dayLabels = arrayOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")// Day labels for the week.

    // Variables for attendance data and calculations.
    private var hourlyAttendanceData = mapOf<String, List<BarEntry>>()// Hourly attendance data by day.
    private var noDays: Int = 0// Placeholder for the number of days (not used directly here).
    private lateinit var quartiles: List<Float>// Quartiles for categorizing attendance levels.

// onCreate is the entry point of the activity.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)// Load the layout for this activity.
        setContentView(R.layout.activity_bookings) // Enables edge-to-edge UI mode for a modern look.
        enableEdgeToEdge()

        
// Initialize UI elements and set up event listeners.
        txtNumberOfPTs = findViewById(R.id.txtnumberOfPTs)

        txtNumberOfPTs.text = "${getNoPTs()} PTs available" // Display the number of personal trainers.
        barChart = findViewById(R.id.popularTimesChart) // Chart for displaying attendance trends.
        databaseReference = FirebaseDatabase.getInstance().getReference("attendance")// Firebase reference for attendance data.
        btnMakeBooking = findViewById(R.id.ptImage)

// Navigate to the MakeBooking activity when the button is clicked.
        btnMakeBooking.setOnClickListener {
            val intent = Intent(this, MakeBooking::class.java)
            startActivity(intent)
        }
// Initialize chart style and data.
        setupBarChartStyle()// Configure the chart's appearance.
        loadWeeklyData()// Load and process weekly attendance data.

// Setup navigation and interactive elements.
        setupDayButtons()// Configure buttons for selecting days.
        setupChartValueClickListener()// Handle interactions with the chart.




  // Initialize navigation buttons for other activities.
        val btnScan: ImageView = findViewById(R.id.btnCamera)
        btnScan.setOnClickListener {
            // Navigate to the Bookings activity
            val intent = Intent(this, ScanQRCode::class.java)
            startActivity(intent) }

        val btnHome: ImageView = findViewById(R.id.btnHome)
        btnHome.setOnClickListener {
            // Navigate to the Bookings activity
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent) }

        val btnWorkout: ImageView = findViewById(R.id.btnWorkout)
        btnWorkout.setOnClickListener {
            // Navigate to the Bookings activity
            val intent = Intent(this, Workouts::class.java)
            startActivity(intent) }

        val btnTracker: ImageView = findViewById(R.id.btnTracker)
        btnTracker.setOnClickListener {
            // Navigate to the Bookings activity
            val intent = Intent(this, Tracker::class.java)
            startActivity(intent) }
    }

     // Fetches the number of personal trainers by counting Firebase users with an "admin" role.
    private fun getNoPTs(): Int {
        var noPts = 0

        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val role = userSnapshot.child("role").getValue(String::class.java)
                    if (role == "admin") {
                        noPts++
                    }
                }
                txtNumberOfPTs.text = "$noPts PTs available"// Update the UI with the count.
            }

            override fun onCancelled(error: DatabaseError) {
               // Handle potential database errors.
            }
        })

        return noPts// Returns the count (though it might not be accurate when called due to asynchronous operations).
    }

    // Load and process weekly attendance data for display.
    private fun loadWeeklyData() {
        // Hardcoded attendance data for demonstration purposes.
        val popularTimesData = mapOf(
            "MON" to listOf(1, 2, 2, 3, 3, 2, 1, 1, 2, 3, 4, 4, 2, 1, 1),
            "TUE" to listOf(1, 2, 2, 3, 3, 2, 1, 1, 2, 3, 4, 4, 4, 3, 2),
            "WED" to listOf(1, 2, 2, 3, 3, 2, 1, 1, 2, 3, 4, 4, 2, 1, 1),
            "THU" to listOf(1, 2, 2, 3, 3, 2, 1, 1, 2, 3, 4, 4, 2, 1, 1),
            "FRI" to listOf(1, 2, 2, 3, 3, 2, 1, 1, 2, 3, 4, 4, 0, 0, 0), // Fills missing hours with 0s
            "SAT" to listOf(1, 1, 2, 3, 3, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0), // Fills missing hours with 0s
            "SUN" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0) // Closed
             // Other days omitted for brevity...
        )
  
// Convert raw data into a list of BarEntries for the chart.
        hourlyAttendanceData = popularTimesData.mapValues { (_, hourlyData) ->
            // Map each hourly attendance value into a BarEntry object with x-values starting from 6 AM.
            hourlyData.mapIndexed { index, value -> BarEntry((index + 6).toFloat(), value.toFloat()) }
        }
        
       // Load the data for the current day to display by default.
        val currentDay = getCurrentDayOfWeek()// Get the current day of the week (e.g., "MON").
        loadHourlyDataForDay(currentDay)// Load the hourly attendance data for the current day.
    }



    private fun calculateQuartiles(data: List<Float>): List<Float> {
        val sortedData = data.sorted()// Sort the data in ascending order.
        val quartiles = mutableListOf<Float>()// Initialize an empty list to store quartile values.

        if (sortedData.isNotEmpty()) {
           // Calculate the first quartile (25th percentile).
            quartiles.add(sortedData[(sortedData.size * 25 / 100)])

             // Calculate the second quartile (50th percentile or median).
            quartiles.add(sortedData[(sortedData.size * 50 / 100)])

            // Calculate the third quartile (75th percentile).
            quartiles.add(sortedData[(sortedData.size * 75 / 100)])
        }
        return quartiles// Return the list of calculated quartiles.
    }


    private fun loadHourlyDataForDay(day: String) {
        val hourlyData = hourlyAttendanceData[day]?.map { it.y } ?: listOf()
            // Extract y-values (attendance counts) for the selected day. Default to an empty list if null.

        quartiles = calculateQuartiles(hourlyData)
// Calculate the quartiles for the day's attendance data.

        val completeHourlyData = MutableList(15) { i -> BarEntry((i+6).toFloat(), 0f) }
            // Initialize a list with 15 hours (6 AM to 8 PM) with zero attendance.

        hourlyData.forEachIndexed { index, value ->
            completeHourlyData[index] = BarEntry(index.toFloat(), value)
        }

        // Now update the chart with quartile-based labels
        updateBarChart(completeHourlyData, generateHourLabels(), day)
    }

    private fun setupDayButtons() {
        val dayButtons = mapOf(
             // Map day strings to corresponding button views.
            "MON" to findViewById(R.id.btnMonday),
            "TUE" to findViewById(R.id.btnTuesday),
            "WED" to findViewById(R.id.btnWednesday),
            "THU" to findViewById(R.id.btnThursday),
            "FRI" to findViewById(R.id.btnFriday),
            "SAT" to findViewById(R.id.btnSaturday),
            "SUN" to findViewById<Button>(R.id.btnSunday)
        )

        // Highlight today's button on app load
        highlightSelectedButton(todayDayOfWeek, dayButtons)

        dayButtons.forEach { (day, button) ->
            button.setOnClickListener {
                // Load the data for the clicked day
                loadHourlyDataForDay(day)

                // Highlight the selected button
                highlightSelectedButton(day, dayButtons)
            }
        }
    }

    private fun highlightSelectedButton(selectedDay: String, dayButtons: Map<String, Button>) {
        // Reset all button background tints to default
        dayButtons.forEach { (_, button) ->
            // Reset all buttons to the default background color.
            button.backgroundTintList = getColorStateList(R.color.defaultButtonColor) // Default color
        }

    // Change the background color of the selected day's button.
        dayButtons[selectedDay]?.backgroundTintList = getColorStateList(R.color.highlightButtonColor) // Create a dataset for the chart
    }

    private fun updateBarChart(entries: List<BarEntry>, labels: Array<String>, currentDay: String) {
        val barDataSet = BarDataSet(entries, "Hourly Attendance")

        barDataSet.colors = entries.mapIndexed { i, entry ->
            when {
        // Set colors for bars based on busyness and whether the hour is the current live hour.
                currentDay == todayDayOfWeek && entry.x.toInt() + 6 == liveHour -> Color.parseColor("#ff0000") // Current hour
                entry.y <= quartiles[0] -> Color.parseColor("#FFB850") // Not Busy
                entry.y <= quartiles[1] -> Color.parseColor("#FB8C00") // Starting to get busy
                entry.y <= quartiles[2] -> Color.parseColor("#FFA726") // Busy
                else -> Color.parseColor("#FF6F00") // Very Busy
            }
        }

        barDataSet.setDrawValues(false)// Disable values displayed on bars.
        val barData = BarData(barDataSet) // Create the bar data object.

        // Set bar width to allow spacing between bars
        barData.barWidth = 0.5f// Adjust the width of bars.

      
        barChart.data = barData// Apply data to the chart.
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)// Set custom x-axis labels.
        barChart.setFitBars(true) // Ensure bars fit within the view.
        barChart.invalidate()// Redraw the chart.
    }
  // Get the current day of the week as a string.
    private fun getCurrentDayOfWeek(): String {
        // Get the current day of the week as a string (e.g., "MON", "TUE")
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            Calendar.SUNDAY -> "SUN"
            else -> "MON"
        }
    }
// Configure the appearance of the bar chart.
    private fun setupBarChartStyle() {
        barChart.description.isEnabled = false// Disable the chart description text.
        barChart.setDrawGridBackground(false) // Disable the background grid.
        barChart.setDrawBarShadow(false)// Disable shadows behind bars.
        barChart.setPinchZoom(false)// Disable pinch-to-zoom functionality.

       // Enable dragging but disable scaling to focus on horizontal scrolling.
        barChart.isDragEnabled = true
        barChart.setScaleEnabled(false) // If you don't want scaling, keep it false

        // Increase the bottom offset to raise the x-axis labels from the bottom
        // Adjust extra spacing around the chart to optimize layout.
        barChart.setExtraOffsets(10f, 0f, 10f, 20f) // Adjust the last value to control the bottom space

        // Configure x-axis properties.
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM// Position x-axis labels at the bottom.
        xAxis.setDrawGridLines(false) // Disable grid lines for x-axis.
        xAxis.setDrawAxisLine(false)// Hide the axis line.
        xAxis.textColor = Color.WHITE// Set label text color to white for better visibility.
        xAxis.textSize = 12f// Increase label text size for readability.
        xAxis.spaceMin = 0.5f // Space on the left side
        xAxis.spaceMax = 0.5f // Space on the right side

        // Optional: If you want to raise the labels further from the x-axis line itself
        xAxis.yOffset = 5f // Adjust this if needed for more spacing above the x-axis line

        // Ensure the y-axis starts at 0
        // Configure y-axis properties.
        barChart.axisLeft.apply {
            axisMinimum = 0f  // Explicitly set the minimum value for the y-axis
        }
        barChart.axisLeft.isEnabled = false// Hide the left y-axis.
        barChart.axisRight.isEnabled = false// Hide the right y-axis.
        // Configure the chart legend and background.
        barChart.legend.isEnabled = false// Disable the legend.
        barChart.setBackgroundColor(Color.parseColor("#27262C"))// Set the chart's background color.
    }



    private fun setupChartValueClickListener() {
        // Set up a listener for user interactions with chart values.
        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    barChart.xAxis.removeAllLimitLines()// Remove any existing limit lines.

                    // Get the selected hour and the corresponding busy count
                    val selectedHour = it.x.toInt()
                    val busyCount = it.y.toInt() // how busy it is (attendance count)

                    // Determine the busyness label based on quartiles
                    val busynessLabel = when {
                        busyCount <= quartiles[0] -> "Not Busy"  // 1st quartile
                        busyCount <= quartiles[1] -> "Starting to Get Busy"  // 2nd quartile
                        busyCount <= quartiles[2] -> "Busy"  // 3rd quartile
                        else -> "Very Busy"  // 4th quartile
                    }

                    // Create the limit line label
                    val limitLineText = "Time: ${String.format("%02d:00", selectedHour + 6)}, $busynessLabel"
                    val limitLine = LimitLine(selectedHour.toFloat(), limitLineText)

                    // Set the appearance of the limit line
                    // Configure the limit line's appearance.
                    limitLine.lineColor = Color.RED
                    limitLine.textColor = Color.WHITE
                    limitLine.textSize = 12f

                // Adjust label position based on bar position relative to the chart's midpoint.
                    val midPoint = (barChart.highestVisibleX + barChart.lowestVisibleX) / 2
                    limitLine.labelPosition = if (it.x > midPoint) {
                        LimitLine.LimitLabelPosition.LEFT_TOP // Move label to the left for bars on the right  // Align to the left for right-side bars.
                    } else {
                        LimitLine.LimitLabelPosition.RIGHT_TOP // Move label to the right for bars on the left
                    }

                    // Add the limit line to the x-axis
                    // Add the limit line to the chart's x-axis.
                    barChart.xAxis.addLimitLine(limitLine)
                    barChart.invalidate() // Redraw the chart
                }
            }

            override fun onNothingSelected() {
                // Clear limit lines when no bar is selected.
                barChart.xAxis.removeAllLimitLines()
                barChart.invalidate()
            }
        })
    }

    private fun generateHourLabels(): Array<String> {
            // Generate labels for each hour (6 AM to 8 PM).
        return Array(15) { i -> String.format("%02d:00", i + 6) }
    }

    private fun getDayOfWeek(dateString: String): String {
         // Convert a date string (e.g., "yyyy-MM-dd") into a day of the week string.
        // Map day abbreviations to their corresponding buttons in the UI.
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
            else -> "MON"
        }
    }


}
