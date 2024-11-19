// Package declaration for the app
package com.techtitans.alleysway

// Importing necessary Android and Firebase libraries
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

// Activity class for displaying the weight tracking graph
class Tracker_graph : AppCompatActivity() {

    // Firebase references
    private lateinit var databaseReference: DatabaseReference // Reference to Firebase Realtime Database
    private lateinit var mAuth: FirebaseAuth // Firebase Authentication instance

    // UI components
    private lateinit var lineChart: LineChart // LineChart for displaying weight data
    private lateinit var noDataTextView: TextView // TextView to show when there's no data
    private lateinit var btnHome: ImageView // ImageView for Home button
    private lateinit var btnWorkout: ImageView // ImageView for Workout button
    private lateinit var btnCamera: ImageView // ImageView for Camera button
    private lateinit var btnBooking: ImageView // ImageView for Booking button

    // Date range for the graph
    private var startDate: Date? = null // Start date for tracking
    private var endDate: Date? = null // End date for tracking

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display for immersive UI
        setContentView(R.layout.activity_tracker_graph) // Set the layout for the activity

        // Initialize Firebase Auth and Database references
        mAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Initialize navigation buttons by finding them in the layout
        btnCamera = findViewById(R.id.btnCamera)
        btnBooking = findViewById(R.id.btnBooking)
        btnHome = findViewById(R.id.btnHome)
        btnWorkout = findViewById(R.id.btnWorkout)

        // Set OnClickListeners for navigation buttons to handle user interactions
        btnCamera.setOnClickListener {
            val intent = Intent(this, ScanQRCode::class.java)
            startActivity(intent) // Navigate to ScanQRCode activity
        }
        btnWorkout.setOnClickListener {
            val intent = Intent(this, Workouts::class.java)
            startActivity(intent) // Navigate to Workouts activity
        }
        btnBooking.setOnClickListener {
            val intent = Intent(this, Bookings::class.java)
            startActivity(intent) // Navigate to Bookings activity
        }
        btnHome.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent) // Navigate to HomePage activity
        }

        // Initialize Tracker button and set its OnClickListener
        val btnTracker: ImageView = findViewById(R.id.btnTracker)
        btnTracker.setOnClickListener {
            val intent = Intent(this, Tracker::class.java)
            startActivity(intent) // Navigate to Tracker activity
        }

        // Initialize LineChart and no data TextView by finding them in the layout
        lineChart = findViewById(R.id.lineChart)
        noDataTextView = findViewById(R.id.noDataTextView)

        // Retrieve start and end dates from the Intent that started this activity
        val startDateString = intent.getStringExtra("startDate")
        val endDateString = intent.getStringExtra("endDate")

        // Parse the date strings into Date objects using the specified format
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        try {
            startDate = dateFormat.parse(startDateString!!) // Parse start date
            endDate = dateFormat.parse(endDateString!!) // Parse end date
        } catch (e: Exception) {
            e.printStackTrace() // Print stack trace for debugging
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show() // Show error message to user
            return // Exit the method if date parsing fails
        }

        // Set the heading text to display the date range
        val tvHeading: TextView = findViewById(R.id.tvHeading)
        tvHeading.text = "My weight from $startDateString to $endDateString"

        // Load weight data from Firebase and display the graph
        loadGraphData()
    }

    // Method to load weight data from Firebase and prepare the graph
    private fun loadGraphData() {
        val userId = mAuth.currentUser?.uid ?: return // Get current user's UID or return if not logged in

        // Define date formats for parsing and labeling
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val labelDateFormat = SimpleDateFormat("EEE (dd)", Locale.getDefault())

        // Reference to the "weight" node under the current user in Firebase
        databaseReference.child("users").child(userId).child("weight")
            .orderByChild("date") // Order the data by date
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if there is any weight data
                    if (!snapshot.exists()) {
                        lineChart.visibility = View.GONE // Hide the chart if no data
                        noDataTextView.visibility = View.VISIBLE // Show the "no data" message
                        noDataTextView.text = "No data available for the selected date range"
                        return // Exit the method as there's no data to display
                    }

                    val dataList = mutableListOf<Pair<Date, Float>>() // List to hold date and weight pairs

                    // Iterate through each weight entry in Firebase
                    for (childSnapshot in snapshot.children) {
                        val dateString = childSnapshot.child("date").getValue(String::class.java) // Get the date string
                        val weightString = childSnapshot.child("weight").getValue(String::class.java) // Get the weight string

                        if (dateString != null && weightString != null) {
                            try {
                                val entryDate = dateFormat.parse(dateString) // Parse the date string
                                if (entryDate != null && isDateInRange(entryDate)) { // Check if the date is within the selected range
                                    val weight = weightString.toFloatOrNull() // Parse the weight string to Float
                                    if (weight != null) {
                                        dataList.add(Pair(entryDate, weight)) // Add the date and weight to the list
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace() // Print stack trace for debugging
                            }
                        }
                    }

                    dataList.sortBy { it.first } // Sort the list by date

                    val weightEntries = mutableListOf<Entry>() // List to hold chart entries for weight
                    val dateLabels = mutableListOf<String>() // List to hold labels for the X-axis
                    val weightValues = mutableListOf<Float>() // List to hold weight values for display

                    var index = 0f // Initialize index for X-axis positioning
                    for ((entryDate, weight) in dataList) {
                        weightEntries.add(Entry(index, weight)) // Create a new Entry for the chart
                        dateLabels.add(labelDateFormat.format(entryDate)) // Add formatted date label
                        weightValues.add(weight) // Add weight value to the list
                        index++ // Increment index for next entry
                    }

                    if (weightEntries.isNotEmpty()) {
                        lineChart.visibility = View.VISIBLE // Show the chart
                        noDataTextView.visibility = View.GONE // Hide the "no data" message
                        displayLineGraph(weightEntries, dateLabels, weightValues) // Display the graph with data
                    } else {
                        lineChart.visibility = View.GONE // Hide the chart if no valid entries
                        noDataTextView.visibility = View.VISIBLE // Show the "no data" message
                        noDataTextView.text = "No data available for the selected date range"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle any errors that occur while fetching data
                    Toast.makeText(this@Tracker_graph, "Error loading data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Helper method to check if a date is within the selected start and end dates
    private fun isDateInRange(date: Date): Boolean {
        return !(date.before(startDate) || date.after(endDate)) // Return true if date is within range
    }

    // Method to display the line graph with weight data
    private fun displayLineGraph(
        weightEntries: List<Entry>, // List of chart entries for actual weight
        dateLabels: List<String>, // List of labels for the X-axis
        weightValues: List<Float> // List of weight values for display
    ) {
        // Apply exponential smoothing to the weight data for a smoother trend line
        val alpha = 0.5f // Smoothing factor
        val smoothedWeights = applyExponentialSmoothing(weightValues, alpha) // Get smoothed weights
        val smoothedEntries = smoothedWeights.mapIndexed { index, weight ->
            Entry(index.toFloat(), weight) // Create new Entries for smoothed data
        }

        // Create dataset for smoothed actual weight
        val weightDataSet = LineDataSet(smoothedEntries, "Weight") // Initialize LineDataSet with smoothed data
        weightDataSet.lineWidth = 2f // Set line width
        weightDataSet.color = resources.getColor(R.color.orange) // Set line color
        weightDataSet.setDrawCircles(true) // Enable drawing circles at data points
        weightDataSet.setDrawCircleHole(false) // Disable circle holes
        weightDataSet.setCircleColors(resources.getColor(R.color.orange)) // Set circle color
        weightDataSet.circleRadius = 4f // Set circle radius
        weightDataSet.setDrawValues(false) // Disable drawing values on data points
        weightDataSet.setDrawFilled(true) // Enable filling under the line
        val drawable = ContextCompat.getDrawable(this, R.drawable.fade_orange) // Get drawable for fill
        weightDataSet.fillDrawable = drawable // Set fill drawable
        weightDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Set line mode to smooth curves

        // Perform linear regression on smoothed data for prediction
        val (slope, intercept) = calculateLinearRegression(smoothedEntries) // Calculate slope and intercept
        val predictedEntries = generatePredictedEntries(smoothedEntries, slope, intercept) // Generate predicted data

        // Ensure the predicted data starts from the last smoothed data point
        if (smoothedEntries.isNotEmpty() && predictedEntries.isNotEmpty()) {
            predictedEntries.add(0, smoothedEntries.last()) // Add the last smoothed entry as the first predicted entry
        }

        // Create dataset for predicted weight
        val predictedDataSet = LineDataSet(predictedEntries, "Predicted Weight") // Initialize LineDataSet with predicted data
        predictedDataSet.lineWidth = 2f // Set line width
        predictedDataSet.color = resources.getColor(R.color.blue) // Set line color
        predictedDataSet.setDrawCircles(false) // Disable drawing circles at data points
        predictedDataSet.setDrawValues(false) // Disable drawing values on data points
        predictedDataSet.setDrawFilled(true) // Enable filling under the line
        val predictedDrawable = ContextCompat.getDrawable(this, R.drawable.fade_blue) // Get drawable for fill
        predictedDataSet.fillDrawable = predictedDrawable // Set fill drawable
        predictedDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Set line mode to smooth curves

        // Combine the actual and predicted datasets
        val dataSets = mutableListOf<ILineDataSet>() // List to hold multiple datasets
        dataSets.add(weightDataSet) // Add actual weight dataset
        dataSets.add(predictedDataSet) // Add predicted weight dataset

        // Create LineData object with the combined datasets and set it to the chart
        val lineData = LineData(dataSets) // Initialize LineData with datasets
        lineChart.data = lineData // Set data to the LineChart

        // Update date labels to include labels for predicted dates
        val extendedDateLabels = updateDateLabels(dateLabels, predictedEntries.size) // Get extended labels

        // Customize the appearance of the chart (axes, legend, etc.)
        configureChartAppearance(extendedDateLabels) // Apply chart appearance settings

        // Display additional information like goal weight and best weight
        displayGoalWeight(weightEntries, lineData) // Display the goal weight line and best weight
        displayMostRecentWeight(weightValues) // Display the most recent weight

        // Refresh the chart to display the new data
        lineChart.invalidate() // Refresh the LineChart
    }

    // Method to apply exponential smoothing to a list of weight values
    private fun applyExponentialSmoothing(data: List<Float>, alpha: Float): List<Float> {
        val smoothedData = mutableListOf<Float>() // List to hold smoothed data
        if (data.isNotEmpty()) {
            smoothedData.add(data[0]) // Initialize with the first data point
            for (i in 1 until data.size) {
                val smoothedValue = alpha * data[i] + (1 - alpha) * smoothedData[i - 1] // Apply smoothing formula
                smoothedData.add(smoothedValue) // Add smoothed value to the list
            }
        }
        return smoothedData // Return the smoothed data
    }

    // Method to calculate linear regression parameters (slope and intercept)
    private fun calculateLinearRegression(entries: List<Entry>): Pair<Float, Float> {
        val n = entries.size // Number of data points
        if (n == 0) return Pair(0f, 0f) // Return default if no data

        var sumX = 0f // Sum of X values
        var sumY = 0f // Sum of Y values
        var sumXY = 0f // Sum of X*Y
        var sumXSquare = 0f // Sum of X^2

        // Calculate the sums required for linear regression
        for (entry in entries) {
            val x = entry.x
            val y = entry.y
            sumX += x
            sumY += y
            sumXY += x * y
            sumXSquare += x * x
        }

        // Calculate slope (m) and intercept (b) using the linear regression formulas
        val slope = (n * sumXY - sumX * sumY) / (n * sumXSquare - sumX * sumX)
        val intercept = (sumY - slope * sumX) / n

        return Pair(slope, intercept) // Return slope and intercept as a Pair
    }

    // Method to generate predicted weight entries based on linear regression parameters
    private fun generatePredictedEntries(
        entries: List<Entry>, // List of actual weight entries
        slope: Float, // Slope from linear regression
        intercept: Float // Intercept from linear regression
    ): MutableList<Entry> {
        val predictedEntries = mutableListOf<Entry>() // List to hold predicted entries
        if (entries.isEmpty()) return predictedEntries // Return empty list if no data

        val lastX = entries.last().x // Get the last X value from actual data
        val numberOfPredictedPoints = 14 // Number of days to predict

        // Generate predicted data points
        for (i in 1..numberOfPredictedPoints) {
            val x = lastX + i // Increment X value for prediction
            val y = slope * x + intercept // Calculate predicted Y value
            predictedEntries.add(Entry(x, y)) // Add the predicted entry to the list
        }
        return predictedEntries // Return the list of predicted entries
    }

    // Method to update date labels to include labels for predicted dates
    private fun updateDateLabels(originalLabels: List<String>, predictionSize: Int): List<String> {
        val extendedLabels = originalLabels.toMutableList() // Create a mutable copy of original labels
        val dateFormat = SimpleDateFormat("EEE (dd)", Locale.getDefault()) // Define date format
        val calendar = Calendar.getInstance()
        calendar.time = endDate ?: Date() // Set calendar to end date or current date

        // Add labels for predicted dates
        for (i in 1 until predictionSize) {
            calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to the next day
            extendedLabels.add(dateFormat.format(calendar.time)) // Add formatted date label
        }
        return extendedLabels // Return the extended list of labels
    }

    // Method to configure the appearance of the LineChart
    private fun configureChartAppearance(extendedDateLabels: List<String>) {
        val xAxis = lineChart.xAxis // Access the XAxis of the chart
        xAxis.position = XAxis.XAxisPosition.BOTTOM // Position the XAxis at the bottom
        xAxis.granularity = 1f // Set granularity to 1 to avoid overlapping labels
        xAxis.textColor = Color.BLACK // Set text color for XAxis labels
        xAxis.setDrawGridLines(false) // Disable grid lines
        xAxis.setDrawAxisLine(true) // Enable axis line
        xAxis.axisLineColor = Color.GRAY // Set color for the axis line
        xAxis.valueFormatter = IndexAxisValueFormatter(extendedDateLabels) // Set custom formatter for labels
        xAxis.textSize = 10f // Set text size for XAxis labels
        xAxis.setLabelCount(5, true) // Set the number of labels to display
        xAxis.labelRotationAngle = -30f // Rotate labels for better readability

        val leftAxis = lineChart.axisLeft // Access the left YAxis of the chart
        leftAxis.textColor = Color.BLACK // Set text color for YAxis labels
        leftAxis.setDrawGridLines(false) // Disable grid lines
        leftAxis.setDrawAxisLine(true) // Enable axis line
        leftAxis.axisLineColor = Color.GRAY // Set color for the axis line
        leftAxis.textSize = 12f // Set text size for YAxis labels
        lineChart.axisRight.isEnabled = false // Disable the right YAxis

        val legend = lineChart.legend // Access the legend of the chart
        legend.textColor = Color.BLACK // Set text color for legend
        legend.textSize = 14f // Set text size for legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP // Align legend vertically at the top
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER // Align legend horizontally at the center
        legend.orientation = Legend.LegendOrientation.HORIZONTAL // Set legend orientation to horizontal
        legend.setDrawInside(false) // Draw legend outside the chart

        lineChart.setDrawGridBackground(false) // Disable grid background
        lineChart.setBackgroundColor(Color.WHITE) // Set background color of the chart
        lineChart.description.isEnabled = false // Disable the chart description
        lineChart.setTouchEnabled(true) // Enable touch gestures
        lineChart.setDragEnabled(true) // Enable dragging
        lineChart.setScaleEnabled(true) // Enable scaling
        lineChart.setScaleXEnabled(true) // Enable horizontal scaling
        lineChart.setScaleYEnabled(true) // Enable vertical scaling
    }

    // Method to display the goal weight line and the best weight achieved
    private fun displayGoalWeight(weightEntries: List<Entry>, lineData: LineData) {
        val userId = mAuth.currentUser?.uid ?: return // Get current user's UID or return if not logged in

        // Reference to the user's goal weight in Firebase
        databaseReference.child("users").child(userId).child("goal").child("goal")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val goalWeight = snapshot.getValue(String::class.java)?.toFloatOrNull() // Get the goal weight
                    if (goalWeight != null) {
                        // Calculate the maximum X value based on actual and predicted entries
                        val maxX = (weightEntries.size - 1).toFloat() + 14 // Extend for the predicted range

                        // Create a goal line across the chart
                        val goalEntries = listOf(
                            Entry(0f, goalWeight), // Start of the goal line
                            Entry(maxX, goalWeight) // End of the goal line
                        )
                        val goalDataSet = LineDataSet(goalEntries, "Goal") // Initialize LineDataSet for goal
                        goalDataSet.color = Color.GREEN // Set line color for goal
                        goalDataSet.lineWidth = 1.5f // Set line width
                        goalDataSet.setDrawCircles(false) // Disable circles on goal line
                        goalDataSet.setDrawValues(false) // Disable drawing values on the line
                        goalDataSet.enableDashedLine(10f, 5f, 0f) // Set dashed line pattern
                        lineData.addDataSet(goalDataSet) // Add goal dataset to the chart data
                        lineChart.data = lineData // Update the chart with the new data
                        lineChart.invalidate() // Refresh the chart

                        // Calculate the closest weight to the goal from actual Firebase data only
                        val bestWeight = weightEntries.minByOrNull { entry ->
                            kotlin.math.abs(entry.y - goalWeight) // Find the weight closest to the goal
                        }?.y

                        // Update Best weight TextView with the closest weight to the goal
                        val tvBestWeight: TextView = findViewById(R.id.tvBestWeight)
                        tvBestWeight.text = "Best: ${bestWeight ?: "--"} kg" // Display best weight or placeholder
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle any errors that occur while fetching the goal weight
                    Toast.makeText(this@Tracker_graph, "Error loading goal", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Method to display the most recent weight entry
    private fun displayMostRecentWeight(weightValues: List<Float>) {
        val mostRecentWeight = weightValues.lastOrNull() // Get the last weight entry or null
        val tvMostRecent: TextView = findViewById(R.id.tvMostRecent) // Find the TextView for displaying most recent weight
        tvMostRecent.text = "Most Recent: ${mostRecentWeight ?: "--"} kg" // Display the most recent weight or placeholder
    }
}
