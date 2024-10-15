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
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

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
                                        dateLabels.add(dateString)
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
        // Create the dataset for the weight entries
        val weightDataSet = LineDataSet(weightEntries, "Weight")
        weightDataSet.lineWidth = 2f
        weightDataSet.color = resources.getColor(R.color.orange)
        weightDataSet.setDrawCircles(true)
        weightDataSet.setDrawCircleHole(false)
        weightDataSet.setCircleColors(resources.getColor(R.color.orange))
        weightDataSet.circleRadius = 4f
        weightDataSet.setDrawValues(true)
        weightDataSet.valueTextColor = Color.BLACK
        weightDataSet.setDrawFilled(true)
        // Set gradient fill
        val drawable = ContextCompat.getDrawable(this, R.drawable.fade_orange)
        weightDataSet.fillDrawable = drawable
        // Set mode to cubic bezier for smooth lines
        weightDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        val dataSets = mutableListOf<LineDataSet>()
        dataSets.add(weightDataSet)

        // Create line data and set it to the chart
        val lineData = LineData(dataSets as List<LineDataSet>)
        lineChart.data = lineData

        // Customize the x-axis to display date labels
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.textColor = Color.BLACK
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels)
        xAxis.labelRotationAngle = -45f
        xAxis.textSize = 12f

        // Customize the y-axis
        val leftAxis = lineChart.axisLeft
        leftAxis.textColor = Color.BLACK
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawAxisLine(false)
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
                            Entry(weightEntries.size - 1f, goalWeight)
                        )
                        val goalDataSet = LineDataSet(goalEntries, "Goal Weight")
                        goalDataSet.color = Color.GREEN
                        goalDataSet.lineWidth = 1.5f
                        goalDataSet.setDrawCircles(false)
                        goalDataSet.setDrawValues(false)
                        goalDataSet.enableDashedLine(10f, 5f, 0f)
                        dataSets.add(goalDataSet)
                        lineChart.data = LineData(dataSets as List<LineDataSet>)
                        lineChart.invalidate()

                        // Calculate best weight
                        val bestWeight = weightEntries.minByOrNull { entry ->
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
}
