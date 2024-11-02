package com.techtitans.alleysway

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class Tracker_graph : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var lineChart: LineChart
    private lateinit var noDataTextView: TextView
    private lateinit var btnHome: ImageView
    private lateinit var btnWorkout: ImageView
    private lateinit var btnCamera: ImageView
    private lateinit var btnBooking: ImageView

    private var startDate: Date? = null
    private var endDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tracker_graph)

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        btnCamera = findViewById(R.id.btnCamera)
        btnBooking = findViewById(R.id.btnBooking)
        btnHome = findViewById(R.id.btnHome)
        btnWorkout = findViewById(R.id.btnWorkout)

        // Set OnClickListeners for nav bar
        btnCamera.setOnClickListener {
            val intent = Intent(this, ScanQRCode::class.java)
            startActivity(intent)
        }
        btnWorkout.setOnClickListener {
            val intent = Intent(this, Workouts::class.java)
            startActivity(intent)
        }
        btnBooking.setOnClickListener {
            val intent = Intent(this, Bookings::class.java)
            startActivity(intent)
        }
        btnHome.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        // Initialize LineChart
        lineChart = findViewById(R.id.lineChart)
        noDataTextView = findViewById(R.id.noDataTextView)

        // Retrieve start and end dates from Intent
        val startDateString = intent.getStringExtra("startDate")
        val endDateString = intent.getStringExtra("endDate")

        // Parse dates
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        try {
            startDate = dateFormat.parse(startDateString!!)
            endDate = dateFormat.parse(endDateString!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
            return
        }

        // Set the heading text
        val tvHeading: TextView = findViewById(R.id.tvHeading)
        tvHeading.text = "My weight from $startDateString to $endDateString"

        // Load weight data and display graph
        loadGraphData()
    }

    private fun loadGraphData() {
        val userId = mAuth.currentUser?.uid ?: return

        // Define date formats
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val labelDateFormat = SimpleDateFormat("EEE (dd)", Locale.getDefault())

        // Retrieve weight data from Firebase
        databaseReference.child("users").child(userId).child("weight")
            .orderByChild("date")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        lineChart.visibility = View.GONE
                        noDataTextView.visibility = View.VISIBLE
                        noDataTextView.text = "No data available for the selected date range"
                        return
                    }

                    val dataList = mutableListOf<Pair<Date, Float>>()
                    for (childSnapshot in snapshot.children) {
                        val dateString = childSnapshot.child("date").getValue(String::class.java)
                        val weightString = childSnapshot.child("weight").getValue(String::class.java)

                        if (dateString != null && weightString != null) {
                            try {
                                val entryDate = dateFormat.parse(dateString)
                                if (entryDate != null && isDateInRange(entryDate)) {
                                    val weight = weightString.toFloatOrNull()
                                    if (weight != null) {
                                        dataList.add(Pair(entryDate, weight))
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    dataList.sortBy { it.first }
                    val weightEntries = mutableListOf<Entry>()
                    val dateLabels = mutableListOf<String>()
                    val weightValues = mutableListOf<Float>()

                    var index = 0f
                    for ((entryDate, weight) in dataList) {
                        weightEntries.add(Entry(index, weight))
                        dateLabels.add(labelDateFormat.format(entryDate))
                        weightValues.add(weight)
                        index++
                    }

                    if (weightEntries.isNotEmpty()) {
                        lineChart.visibility = View.VISIBLE
                        noDataTextView.visibility = View.GONE
                        displayLineGraph(weightEntries, dateLabels, weightValues)
                    } else {
                        lineChart.visibility = View.GONE
                        noDataTextView.visibility = View.VISIBLE
                        noDataTextView.text = "No data available for the selected date range"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Tracker_graph, "Error loading data", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun isDateInRange(date: Date): Boolean {
        return !(date.before(startDate) || date.after(endDate))
    }

    private fun displayLineGraph(
        weightEntries: List<Entry>,
        dateLabels: List<String>,
        weightValues: List<Float>
    ) {
        // Apply exponential smoothing
        val alpha = 0.5f
        val smoothedWeights = applyExponentialSmoothing(weightValues, alpha)
        val smoothedEntries = smoothedWeights.mapIndexed { index, weight ->
            Entry(index.toFloat(), weight)
        }

        // Create dataset for smoothed actual weight
        val weightDataSet = LineDataSet(smoothedEntries, "Weight")
        weightDataSet.lineWidth = 2f
        weightDataSet.color = resources.getColor(R.color.orange)
        weightDataSet.setDrawCircles(true)
        weightDataSet.setDrawCircleHole(false)
        weightDataSet.setCircleColors(resources.getColor(R.color.orange))
        weightDataSet.circleRadius = 4f
        weightDataSet.setDrawValues(false)
        weightDataSet.setDrawFilled(true)
        val drawable = ContextCompat.getDrawable(this, R.drawable.fade_orange)
        weightDataSet.fillDrawable = drawable
        weightDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        // Perform linear regression on smoothed data for prediction
        val (slope, intercept) = calculateLinearRegression(smoothedEntries)
        val predictedEntries = generatePredictedEntries(smoothedEntries, slope, intercept)

        // Ensure the predicted data starts from the last smoothed data point
        if (smoothedEntries.isNotEmpty() && predictedEntries.isNotEmpty()) {
            predictedEntries.add(0, smoothedEntries.last())
        }

        // Create dataset for predicted weight
        val predictedDataSet = LineDataSet(predictedEntries, "Predicted Weight")
        predictedDataSet.lineWidth = 2f
        predictedDataSet.color = resources.getColor(R.color.blue)
        predictedDataSet.setDrawCircles(false)
        predictedDataSet.setDrawValues(false)
        predictedDataSet.setDrawFilled(true)
        val predictedDrawable = ContextCompat.getDrawable(this, R.drawable.fade_blue)
        predictedDataSet.fillDrawable = predictedDrawable
        predictedDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        // Combine the datasets
        val dataSets = mutableListOf<ILineDataSet>()
        dataSets.add(weightDataSet)
        dataSets.add(predictedDataSet)

        // Create line data and set it to the chart
        val lineData = LineData(dataSets)
        lineChart.data = lineData

        // Update dateLabels to include labels for predicted dates
        val extendedDateLabels = updateDateLabels(dateLabels, predictedEntries.size)

        // Customize chart appearance
        configureChartAppearance(extendedDateLabels)

        // Display goal weight and best weight
        displayGoalWeight(weightEntries, lineData)
        displayMostRecentWeight(weightValues)


        // Refresh the chart
        lineChart.invalidate()
    }

    private fun applyExponentialSmoothing(data: List<Float>, alpha: Float): List<Float> {
        val smoothedData = mutableListOf<Float>()
        if (data.isNotEmpty()) {
            smoothedData.add(data[0])
            for (i in 1 until data.size) {
                val smoothedValue = alpha * data[i] + (1 - alpha) * smoothedData[i - 1]
                smoothedData.add(smoothedValue)
            }
        }
        return smoothedData
    }

    private fun calculateLinearRegression(entries: List<Entry>): Pair<Float, Float> {
        val n = entries.size
        if (n == 0) return Pair(0f, 0f)

        var sumX = 0f
        var sumY = 0f
        var sumXY = 0f
        var sumXSquare = 0f

        for (entry in entries) {
            val x = entry.x
            val y = entry.y
            sumX += x
            sumY += y
            sumXY += x * y
            sumXSquare += x * x
        }

        val slope = (n * sumXY - sumX * sumY) / (n * sumXSquare - sumX * sumX)
        val intercept = (sumY - slope * sumX) / n

        return Pair(slope, intercept)
    }

    private fun generatePredictedEntries(
        entries: List<Entry>,
        slope: Float,
        intercept: Float
    ): MutableList<Entry> {
        val predictedEntries = mutableListOf<Entry>()
        if (entries.isEmpty()) return predictedEntries

        val lastX = entries.last().x
        val numberOfPredictedPoints = 14
        for (i in 1..numberOfPredictedPoints) {
            val x = lastX + i
            val y = slope * x + intercept
            predictedEntries.add(Entry(x, y))
        }
        return predictedEntries
    }

    private fun updateDateLabels(originalLabels: List<String>, predictionSize: Int): List<String> {
        val extendedLabels = originalLabels.toMutableList()
        val dateFormat = SimpleDateFormat("EEE (dd)", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = endDate ?: Date()
        for (i in 1 until predictionSize) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            extendedLabels.add(dateFormat.format(calendar.time))
        }
        return extendedLabels
    }

    private fun configureChartAppearance(extendedDateLabels: List<String>) {
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.textColor = Color.BLACK
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.axisLineColor = Color.GRAY
        xAxis.valueFormatter = IndexAxisValueFormatter(extendedDateLabels)
        xAxis.textSize = 10f
        xAxis.setLabelCount(5, true)
        xAxis.labelRotationAngle = -30f

        val leftAxis = lineChart.axisLeft
        leftAxis.textColor = Color.BLACK
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawAxisLine(true)
        leftAxis.axisLineColor = Color.GRAY
        leftAxis.textSize = 12f
        lineChart.axisRight.isEnabled = false

        val legend = lineChart.legend
        legend.textColor = Color.BLACK
        legend.textSize = 14f
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        lineChart.setDrawGridBackground(false)
        lineChart.setBackgroundColor(Color.WHITE)
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setDragEnabled(true)
        lineChart.setScaleEnabled(true)
        lineChart.setScaleXEnabled(true)
        lineChart.setScaleYEnabled(true)
    }

    private fun displayGoalWeight(weightEntries: List<Entry>, lineData: LineData) {
        val userId = mAuth.currentUser?.uid ?: return
        databaseReference.child("users").child(userId).child("goal").child("goal")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val goalWeight = snapshot.getValue(String::class.java)?.toFloatOrNull()
                    if (goalWeight != null) {
                        // Calculate the maximum X value based on actual and predicted entries
                        val maxX = (weightEntries.size - 1).toFloat() + 14 // Extends for the predicted range

                        // Add goal line across the chart
                        val goalEntries = listOf(
                            Entry(0f, goalWeight),
                            Entry(maxX, goalWeight)
                        )
                        val goalDataSet = LineDataSet(goalEntries, "Goal")
                        goalDataSet.color = Color.GREEN
                        goalDataSet.lineWidth = 1.5f
                        goalDataSet.setDrawCircles(false)
                        goalDataSet.setDrawValues(false)
                        goalDataSet.enableDashedLine(10f, 5f, 0f)
                        lineData.addDataSet(goalDataSet)
                        lineChart.data = lineData
                        lineChart.invalidate()

                        // Calculate the closest weight to the goal from actual Firebase data only
                        val bestWeight = weightEntries.minByOrNull { entry ->
                            kotlin.math.abs(entry.y - goalWeight)
                        }?.y

                        // Update Best weight TextView
                        val tvBestWeight: TextView = findViewById(R.id.tvBestWeight)
                        tvBestWeight.text = "Best: ${bestWeight ?: "--"} kg"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Tracker_graph, "Error loading goal", Toast.LENGTH_SHORT).show()
                }
            })
    }



    private fun displayMostRecentWeight(weightValues: List<Float>) {
        val mostRecentWeight = weightValues.lastOrNull()
        val tvMostRecent: TextView = findViewById(R.id.tvMostRecent)
        tvMostRecent.text = "Most Recent: ${mostRecentWeight ?: "--"} kg"
    }
}