package com.example.alleysway

import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
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
import com.google.firebase.database.*
import com.example.alleysway.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Bookings : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var databaseReference: DatabaseReference
    private val liveHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    private val dayLabels = arrayOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    private var hourlyAttendanceData = mapOf<String, List<BarEntry>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookings)

        barChart = findViewById(R.id.popularTimesChart)
        databaseReference = FirebaseDatabase.getInstance().getReference("attendance")

        setupBarChartStyle()
        loadWeeklyData()
        setupDayButtons()
        setupChartValueClickListener()
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

    private fun loadHourlyDataForDay(day: String) {
        val hourlyData = hourlyAttendanceData[day] ?: listOf()
        val completeHourlyData = MutableList(24) { i -> BarEntry(i.toFloat(), 0f) }
        hourlyData.forEach { entry ->
            completeHourlyData[entry.x.toInt()] = entry
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

        dayButtons.forEach { (day, button) ->
            button.setOnClickListener {
                loadHourlyDataForDay(day)
            }
        }
    }

    private fun updateBarChart(entries: List<BarEntry>, labels: Array<String>) {
        val barDataSet = BarDataSet(entries, "Hourly Attendance")

        barDataSet.colors = entries.mapIndexed { i, entry ->
            when {
                entry.x.toInt() == liveHour -> Color.RED
                entry.y > 30 -> Color.parseColor("#FFA726")
                entry.y > 15 -> Color.parseColor("#FB8C00")
                else -> Color.parseColor("#1E88E5")
            }
        }

        barDataSet.setDrawValues(false)
        val barData = BarData(barDataSet)
        barChart.data = barData
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.invalidate()
    }

    private fun setupBarChartStyle() {
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setPinchZoom(false)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.textColor = Color.WHITE
        xAxis.textSize = 12f

        barChart.axisLeft.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setBackgroundColor(Color.parseColor("#27262C"))
        barChart.isDragEnabled = true
        barChart.setScaleEnabled(false)
        barChart.setVisibleXRangeMaximum(6f)
    }

    private fun setupChartValueClickListener() {
        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    barChart.xAxis.removeAllLimitLines()
                    val selectedHour = it.x.toInt()
                    val limitLine = LimitLine(selectedHour.toFloat(), "Time: ${String.format("%02d:00", selectedHour)}")
                    limitLine.lineColor = Color.RED
                    limitLine.textColor = Color.WHITE
                    limitLine.textSize = 12f
                    barChart.xAxis.addLimitLine(limitLine)
                    barChart.invalidate()
                }
            }

            override fun onNothingSelected() {
                barChart.xAxis.removeAllLimitLines()
                barChart.invalidate()
            }
        })
    }

    private fun generateHourLabels(): Array<String> {
        return Array(24) { i -> String.format("%02d:00", i) }
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