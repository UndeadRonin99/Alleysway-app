package com.example.alleysway

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
                    // Check if snapshot exists
                    if (!snapshot.exists()) {
                        // Hide the chart and show a message
                        lineChart.visibility = View.GONE
                        noDataTextView.visibility = View.VISIBLE
                        noDataTextView.text = "No data available for the selected date range"
                        return
                    }

                    val weightEntries = mutableListOf<Entry>()
                    val dateLabels = mutableListOf<String>()
                    val weightValues = mutableListOf<Float>()

                    var index = 0f

                    for (childSnapshot in snapshot.children) {
                        val dateString = childSnapshot.child("date").getValue(String::class.java)
                        val weightString = childSnapshot.child("weight").getValue(String::class.java)

                        if (dateString != null && weightString != null) {
                            try {
                                val entryDate = dateFormat.parse(dateString)
                                if (entryDate != null && isDateInRange(entryDate)) {
                                    val weight = weightString.toFloatOrNull()
                                    if (weight != null) {
                                        weightEntries.add(Entry(index, weight))
                                        val labelString = labelDateFormat.format(entryDate)
                                        dateLabels.add(labelString)
                                        weightValues.add(weight)
                                        index++
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    // Display the line chart with the weight data if entries exist
                    if (weightEntries.isNotEmpty()) {
                        lineChart.visibility = View.VISIBLE // Show the chart
                        noDataTextView.visibility = View.GONE // Hide the "no data" message
                        displayLineGraph(weightEntries, dateLabels, weightValues)
                    } else {
                        // Hide the chart and show a message
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
        // Create the dataset for the actual weight entries
        val weightDataSet = LineDataSet(weightEntries, "Weight")
        weightDataSet.lineWidth = 2f
        weightDataSet.color = resources.getColor(R.color.orange)
        weightDataSet.setDrawCircles(true)
        weightDataSet.setDrawCircleHole(false)
        weightDataSet.setCircleColors(resources.getColor(R.color.orange))
        weightDataSet.circleRadius = 4f
        weightDataSet.setDrawValues(false)
        weightDataSet.setDrawFilled(true)
        // Set gradient fill for actual data
        val drawable = ContextCompat.getDrawable(this, R.drawable.fade_orange)
        weightDataSet.fillDrawable = drawable
        weightDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        // Perform linear regression on the existing data
        val (slope, intercept) = calculateLinearRegression(weightEntries)

        // Generate predicted entries for the next month
        val predictedEntries = generatePredictedEntries(weightEntries, slope, intercept)

        // Ensure the predicted data starts from the last actual data point
        if (weightEntries.isNotEmpty() && predictedEntries.isNotEmpty()) {
            predictedEntries.add(0, weightEntries.last())
        }


        // Create the dataset for the predicted weight entries
        val predictedDataSet = LineDataSet(predictedEntries, "Predicted Weight")
        predictedDataSet.lineWidth = 2f
        predictedDataSet.color = resources.getColor(R.color.blue)
        predictedDataSet.setDrawCircles(false)
        predictedDataSet.setDrawValues(false)
        predictedDataSet.setDrawFilled(true)
        // Set gradient fill for predicted data
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
        val extendedDateLabels = dateLabels.toMutableList()
        val dateFormat = SimpleDateFormat("EEE (dd)", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = endDate ?: Date()
        for (i in 1 until predictedEntries.size) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val label = dateFormat.format(calendar.time)
            extendedDateLabels.add(label)
        }

        // Customize the x-axis
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

        // Customize the y-axis
        val leftAxis = lineChart.axisLeft
        leftAxis.textColor = Color.BLACK
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawAxisLine(true)
        leftAxis.axisLineColor = Color.GRAY
        leftAxis.textSize = 12f
        lineChart.axisRight.isEnabled = false

        // Customize the chart's legend
        val legend = lineChart.legend
        legend.textColor = Color.BLACK
        legend.textSize = 14f
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        // Customize the chart
        lineChart.setDrawGridBackground(false)
        lineChart.setBackgroundColor(Color.WHITE)
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setDragEnabled(true)
        lineChart.setScaleEnabled(true)
        lineChart.setScaleXEnabled(true)
        lineChart.setScaleYEnabled(true)

        // Fetch and display the goal weight, and calculate the best weight
        val userId = mAuth.currentUser?.uid ?: return
        databaseReference.child("users").child(userId).child("goal").child("goal")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val goalWeight = snapshot.getValue(String::class.java)?.toFloatOrNull()
                    if (goalWeight != null) {
                        // Add goal line
                        val goalEntries = listOf(
                            Entry(0f, goalWeight),
                            Entry((weightEntries.size + predictedEntries.size - 1).toFloat(), goalWeight)
                        )
                        val goalDataSet = LineDataSet(goalEntries, "Goal Weight")
                        goalDataSet.color = Color.GREEN
                        goalDataSet.lineWidth = 1.5f
                        goalDataSet.setDrawCircles(false)
                        goalDataSet.setDrawValues(false)
                        goalDataSet.enableDashedLine(10f, 5f, 0f)
                        lineData.addDataSet(goalDataSet)
                        lineChart.data = lineData
                        lineChart.invalidate()

                        // Calculate best weight
                        val combinedEntries = weightEntries + predictedEntries.subList(1, predictedEntries.size)
                        val bestWeight = combinedEntries.minByOrNull { entry ->
                            kotlin.math.abs(entry.y - goalWeight)
                        }?.y

                        // Update Best weight TextView
                        val tvBestWeight: TextView = findViewById(R.id.tvBestWeight)
                        tvBestWeight.text = "Best: ${bestWeight ?: "--"} kg"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Tracker_graph, "Error loading goal", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        // Update Most Recent weight entry
        val mostRecentWeight = weightValues.lastOrNull()
        val tvMostRecent: TextView = findViewById(R.id.tvMostRecent)
        tvMostRecent.text = "Most Recent: ${mostRecentWeight ?: "--"} kg"

        // Refresh the chart
        lineChart.invalidate()
    }


    // Linear Regression Function
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

    // Generate Predicted Entries
    private fun generatePredictedEntries(
        entries: List<Entry>,
        slope: Float,
        intercept: Float
    ): MutableList<Entry> {
        val predictedEntries = mutableListOf<Entry>()
        if (entries.isEmpty()) return predictedEntries

        val lastX = entries.last().x
        val numberOfPredictedPoints = 14 // Predict for the next x days
        for (i in 1..numberOfPredictedPoints) {
            val x = lastX + i
            val y = slope * x + intercept
            predictedEntries.add(Entry(x, y))
        }
        return predictedEntries
    }


}
