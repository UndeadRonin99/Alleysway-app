package com.example.alleysway

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

class Bookings : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var databaseReference: DatabaseReference
    private lateinit var btnMakeBooking: ImageView
    private lateinit var txtNumberOfPTs: TextView

    private val liveHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    private val todayDayOfWeek = getCurrentDayOfWeek() // Get today's day in the same format as your data (e.g., "MON", "TUE")
    private val dayLabels = arrayOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    private var hourlyAttendanceData = mapOf<String, List<BarEntry>>()
    private var noDays: Int = 0
    private lateinit var quartiles: List<Float>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookings)
        enableEdgeToEdge()

        txtNumberOfPTs = findViewById(R.id.txtnumberOfPTs)

        txtNumberOfPTs.text = "${getNoPTs()} PTs available"
        barChart = findViewById(R.id.popularTimesChart)
        databaseReference = FirebaseDatabase.getInstance().getReference("attendance")
        btnMakeBooking = findViewById(R.id.ptImage)


        btnMakeBooking.setOnClickListener {
            val intent = Intent(this, MakeBooking::class.java)
            startActivity(intent)
        }

        setupBarChartStyle()
        loadWeeklyData()
        setupDayButtons()
        setupChartValueClickListener()



        // Inside onCreate method
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
                txtNumberOfPTs.text = "$noPts PTs available"
            }

            override fun onCancelled(error: DatabaseError) {
                // Error handling
            }
        })

        return noPts
    }

    private fun loadWeeklyData() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempHourlyData = mutableMapOf<String, List<BarEntry>>()

                for (daySnapshot in snapshot.children) {
                    val dateKey = daySnapshot.key ?: continue
                    val dayOfWeek = getDayOfWeek(dateKey)
                    val hourlyDataForDay = mutableListOf<BarEntry>()

                    for (hourSnapshot in daySnapshot.children) {
                        val hour = hourSnapshot.key?.toInt() ?: 0
                        val count = hourSnapshot.getValue(Int::class.java) ?: 0
                        hourlyDataForDay.add(BarEntry(hour.toFloat(), count.toFloat()))
                    }
                    noDays++
                    tempHourlyData[dayOfWeek] = hourlyDataForDay
                }

                hourlyAttendanceData = tempHourlyData
                val currentDay = getDayOfWeek(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time))
                loadHourlyDataForDay(currentDay)
            }

            override fun onCancelled(error: DatabaseError) {
                // Error handling
            }
        })
    }

    private fun calculateQuartiles(data: List<Float>): List<Float> {
        val sortedData = data.sorted()
        val quartiles = mutableListOf<Float>()

        if (sortedData.isNotEmpty()) {
            // First quartile (25th percentile)
            quartiles.add(sortedData[(sortedData.size * 25 / 100)])

            // Second quartile (50th percentile or median)
            quartiles.add(sortedData[(sortedData.size * 50 / 100)])

            // Third quartile (75th percentile)
            quartiles.add(sortedData[(sortedData.size * 75 / 100)])
        }
        return quartiles
    }


    private fun loadHourlyDataForDay(day: String) {
        val hourlyData = hourlyAttendanceData[day]?.map { it.y } ?: listOf()
        quartiles = calculateQuartiles(hourlyData)

        val completeHourlyData = MutableList(15) { i -> BarEntry((i+6).toFloat(), 0f) }
        hourlyData.forEachIndexed { index, value ->
            completeHourlyData[index] = BarEntry(index.toFloat(), value)
        }

        // Now update the chart with quartile-based labels
        updateBarChart(completeHourlyData, generateHourLabels(), day)
    }

    private fun setupDayButtons() {
        val dayButtons = mapOf(
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
            button.backgroundTintList = getColorStateList(R.color.defaultButtonColor) // Default color
        }

        // Highlight the selected day button
        dayButtons[selectedDay]?.backgroundTintList = getColorStateList(R.color.highlightButtonColor) // Highlight color
    }

    private fun updateBarChart(entries: List<BarEntry>, labels: Array<String>, currentDay: String) {
        val barDataSet = BarDataSet(entries, "Hourly Attendance")

        barDataSet.colors = entries.mapIndexed { i, entry ->
            when {
                currentDay == todayDayOfWeek && entry.x.toInt() + 6 == liveHour -> Color.parseColor("#ff0000") // Current hour
                entry.y <= quartiles[0] -> Color.parseColor("#FFB850") // Not Busy
                entry.y <= quartiles[1] -> Color.parseColor("#FB8C00") // Starting to get busy
                entry.y <= quartiles[2] -> Color.parseColor("#FFA726") // Busy
                else -> Color.parseColor("#FF6F00") // Very Busy
            }
        }

        barDataSet.setDrawValues(false)
        val barData = BarData(barDataSet)

        // Set bar width to allow spacing between bars
        barData.barWidth = 0.5f

        // Apply data to the chart
        barChart.data = barData
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.setFitBars(true) // Makes the bars fit within the view
        barChart.invalidate()
    }

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

    private fun setupBarChartStyle() {
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setPinchZoom(false)

        // Enable dragging and horizontal scrolling
        barChart.isDragEnabled = true
        barChart.setScaleEnabled(false) // If you don't want scaling, keep it false
        barChart.setExtraOffsets(10f, 0f, 10f, 0f) // Add space to the left and right of the chart

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.textColor = Color.WHITE
        xAxis.textSize = 12f
        xAxis.spaceMin = 0.5f // Space on the left side
        xAxis.spaceMax = 0.5f // Space on the right side

        barChart.axisLeft.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setBackgroundColor(Color.parseColor("#27262C"))
    }

    private fun setupChartValueClickListener() {
        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    barChart.xAxis.removeAllLimitLines()

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
                    limitLine.lineColor = Color.RED
                    limitLine.textColor = Color.WHITE
                    limitLine.textSize = 12f

                    // Determine if the selected bar is on the left or right side of the chart
                    val midPoint = (barChart.highestVisibleX + barChart.lowestVisibleX) / 2
                    limitLine.labelPosition = if (it.x > midPoint) {
                        LimitLine.LimitLabelPosition.LEFT_TOP // Move label to the left for bars on the right
                    } else {
                        LimitLine.LimitLabelPosition.RIGHT_TOP // Move label to the right for bars on the left
                    }

                    // Add the limit line to the x-axis
                    barChart.xAxis.addLimitLine(limitLine)
                    barChart.invalidate() // Redraw the chart
                }
            }

            override fun onNothingSelected() {
                barChart.xAxis.removeAllLimitLines()
                barChart.invalidate()
            }
        })
    }

    private fun generateHourLabels(): Array<String> {
        return Array(15) { i -> String.format("%02d:00", i + 6) }
    }

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
            else -> "MON"
        }
    }


}
