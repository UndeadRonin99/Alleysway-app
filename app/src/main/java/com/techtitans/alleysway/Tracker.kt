// Package declaration for the app
package com.techtitans.alleysway

// Importing necessary Android and Firebase libraries
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date

// Activity class for tracking weight and displaying statistics
class Tracker : AppCompatActivity() {

    // Firebase references
    private lateinit var databaseReference: DatabaseReference // Reference to Firebase Realtime Database
    private lateinit var mAuth: FirebaseAuth // Firebase Authentication instance

    // UI components
    private lateinit var btnAddData: Button // Button to add new weight data
    private lateinit var btnSetGoal: Button // Button to set a weight goal
    private lateinit var btnHome: ImageView // Home navigation button
    private lateinit var btnWorkout: ImageView // Workout navigation button
    private lateinit var btnCamera: ImageView // Camera navigation button
    private lateinit var btnBooking: ImageView // Booking navigation button
    private lateinit var btnStats: Button // Button to view statistics
    private lateinit var tvCurrentWeight: TextView // TextView to display current weight
    private lateinit var tvGoalWeight: TextView // TextView to display goal weight
    private lateinit var tvDifference: TextView // TextView to display difference between current and goal weight
    private lateinit var lineChart: LineChart // LineChart to display weight graph
    private lateinit var noDataTextView: TextView // TextView to show when there's no data

    // Date format for parsing and displaying dates
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display for immersive UI
        setContentView(R.layout.activity_tracker) // Set the layout for this activity

        // Initialize Firebase Auth and Database references
        mAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Initialize UI components by finding them in the layout
        btnAddData = findViewById(R.id.AddData)
        btnSetGoal = findViewById(R.id.btnGoals)
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight)
        tvGoalWeight = findViewById(R.id.tvGoalWeight)
        tvDifference = findViewById(R.id.tvDifference)
        btnStats = findViewById(R.id.btnStats)
        lineChart = findViewById(R.id.lineChart)
        noDataTextView = findViewById(R.id.noDataTextView)

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

        // Initialize and set OnClickListener for the Tracker navigation button
        val btnTracker: ImageView = findViewById(R.id.btnTracker)
        btnTracker.setOnClickListener {
            val intent = Intent(this, Tracker::class.java)
            startActivity(intent) // Navigate to Tracker activity
        }

        // Set OnClickListener for the Add Data button to show a bottom sheet dialog
        btnAddData.setOnClickListener {
            showBottomSheetDialogForData() // Show dialog to add new weight data
        }

        // Set OnClickListener for the Stats button to show a date range dialog
        btnStats.setOnClickListener {
            showDateRangeDialog() // Show dialog to select date range for statistics
        }

        // Set OnClickListener for the Set Goal button to show a bottom sheet dialog
        btnSetGoal.setOnClickListener {
            showBottomSheetDialogForGoal() // Show dialog to set a weight goal
        }

        // Load and display user data: current weight, goal, and difference
        loadUserData()

        // Load weight data from Firebase and display it on the graph
        loadGraphData()
    }

    /**
     * Loads user data including the latest weight entry, goal weight, and calculates the difference.
     */
    private fun loadUserData() {
        val userId = mAuth.currentUser?.uid ?: return // Get current user's UID or return if not logged in

        // Reference to the user's weight data in Firebase
        databaseReference.child("users").child(userId).child("weight")
            .orderByChild("date").limitToLast(1) // Query the latest weight entry based on date
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if any weight data exists
                    if (!snapshot.exists()) {
                        // Set default texts if no weight data is available
                        tvCurrentWeight.text = "Now"
                        tvGoalWeight.text = "Target"
                        tvDifference.text = "Change"
                        return
                    }

                    // Retrieve the latest weight entry
                    val latestWeight = snapshot.children.firstOrNull()?.child("weight")
                        ?.getValue(String::class.java)?.toFloatOrNull()

                    if (latestWeight != null) {
                        // Display the latest weight
                        tvCurrentWeight.text = "$latestWeight\nNow"

                        // Fetch the user's goal weight from Firebase
                        databaseReference.child("users").child(userId).child("goal").child("goal")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(goalSnapshot: DataSnapshot) {
                                    val goalWeight =
                                        goalSnapshot.getValue(String::class.java)?.toFloatOrNull()

                                    if (goalWeight != null) {
                                        // Display the goal weight
                                        tvGoalWeight.text = "$goalWeight\nTarget"
                                        // Calculate and display the difference between current and goal weight
                                        val difference = latestWeight - goalWeight
                                        tvDifference.text = "${"%.2f".format(difference)}\nChange"
                                    } else {
                                        // Set default texts if no goal weight is set
                                        tvGoalWeight.text = "Target\n--"
                                        tvDifference.text = "Change\n--"
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle errors while fetching goal weight
                                    Toast.makeText(
                                        this@Tracker,
                                        "Error loading goal",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    } else {
                        // Set default texts if no valid weight data is available
                        tvCurrentWeight.text = "Now"
                        tvGoalWeight.text = "Target"
                        tvDifference.text = "Change"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle errors while fetching weight data
                    Toast.makeText(this@Tracker, "Error loading weight data", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    /**
     * Loads the user's weight data from Firebase and displays it on the LineChart.
     */
    private fun loadGraphData() {
        val userId = mAuth.currentUser?.uid ?: return // Get current user's UID or return if not logged in

        // Reference to the user's weight data in Firebase
        databaseReference.child("users").child(userId).child("weight")
            .orderByChild("date")
            .limitToLast(14) // Retrieve the last 14 weight entries
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if any weight data exists
                    if (!snapshot.exists()) {
                        // Hide the chart and show a "no data" message
                        lineChart.visibility = View.GONE
                        showNoDataMessage()
                        return
                    }

                    val weightEntries = mutableListOf<Entry>() // List to hold chart entries for weight
                    val weightDataList = mutableListOf<Pair<String, Float>>() // Pair of date and weight

                    // Iterate through each weight entry in Firebase
                    for (childSnapshot in snapshot.children) {
                        val weight = childSnapshot.child("weight").getValue(String::class.java)
                            ?.toFloatOrNull() // Retrieve weight value
                        val dateString = childSnapshot.child("date").getValue(String::class.java) // Retrieve date string

                        if (weight != null && dateString != null) {
                            weightDataList.add(Pair(dateString, weight)) // Add to the list if both values are valid
                        }
                    }

                    // Sort the list by date to ensure chronological order
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    weightDataList.sortBy { dateFormat.parse(it.first) }

                    // Prepare the entries for the chart
                    var index = 0f // Initialize index for X-axis positioning
                    for ((_, weight) in weightDataList) {
                        weightEntries.add(Entry(index, weight)) // Create a new Entry for the chart
                        index++ // Increment index for next entry
                    }

                    // Display the line chart with the weight data if entries exist
                    if (weightEntries.isNotEmpty()) {
                        lineChart.visibility = View.VISIBLE // Show the chart
                        displayLineGraph(weightEntries) // Display the graph with data
                    } else {
                        // Hide the chart and show a "no data" message
                        lineChart.visibility = View.GONE
                        showNoDataMessage()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle errors while fetching weight data
                    Toast.makeText(this@Tracker, "Error loading data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /**
     * Displays a "no data" message when there is no weight data to show.
     */
    private fun showNoDataMessage() {
        // Hide the chart
        lineChart.visibility = View.GONE

        // Display a message indicating no data is available
        noDataTextView.visibility = View.VISIBLE
        noDataTextView.text = "Please enter data to see statistics"
    }

    /**
     * Displays the weight data on the LineChart.
     *
     * @param weightEntries List of Entry objects representing weight over time.
     */
    private fun displayLineGraph(weightEntries: List<Entry>) {
        val userId = mAuth.currentUser?.uid ?: return // Get current user's UID or return if not logged in
        lineChart.visibility = View.VISIBLE // Ensure the graph is visible when there is data

        // Fetch the user's weight goal from Firebase
        databaseReference.child("users").child(userId).child("goal").child("goal")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val goalWeight = snapshot.getValue(String::class.java)?.toFloatOrNull() // Retrieve goal weight

                    // Create the dataset for the weight entries
                    val weightDataSet = LineDataSet(weightEntries, "Weight") // Initialize LineDataSet with weight data
                    weightDataSet.lineWidth = 2f // Set line width
                    weightDataSet.color = resources.getColor(R.color.orange) // Set line color
                    weightDataSet.setDrawCircles(true) // Enable drawing circles at data points
                    weightDataSet.setDrawCircleHole(false) // Disable circle holes
                    weightDataSet.setCircleColors(resources.getColor(R.color.orange)) // Set circle color
                    weightDataSet.circleRadius = 4f // Set circle radius
                    weightDataSet.setDrawValues(true) // Enable drawing values on data points
                    weightDataSet.valueTextColor = resources.getColor(R.color.white) // Set value text color
                    weightDataSet.setDrawFilled(true) // Enable filling under the line
                    weightDataSet.fillColor = resources.getColor(R.color.orange) // Set fill color

                    val dataSets = mutableListOf<ILineDataSet>() // List to hold multiple datasets
                    dataSets.add(weightDataSet) // Add weight dataset to the list

                    // If there is a goal weight, add a horizontal line for it
                    if (goalWeight != null) {
                        val goalEntries = mutableListOf<Entry>()
                        goalEntries.add(Entry(0f, goalWeight)) // Start point of the goal line
                        goalEntries.add(Entry(weightEntries.size.toFloat() - 1, goalWeight)) // End point of the goal line

                        val goalDataSet = LineDataSet(goalEntries, "Goal Weight") // Initialize LineDataSet for goal weight
                        goalDataSet.color = resources.getColor(R.color.green) // Set line color for goal
                        goalDataSet.lineWidth = 1.5f // Set line width for goal
                        goalDataSet.setDrawCircles(false) // Disable circles on the goal line
                        goalDataSet.setDrawValues(false) // Disable drawing values on the goal line
                        goalDataSet.enableDashedLine(10f, 5f, 0f) // Set dashed line pattern for the goal line

                        dataSets.add(goalDataSet) // Add goal dataset to the list
                    }

                    // Create LineData object with the combined datasets and set it to the chart
                    val lineData = LineData(dataSets) // Initialize LineData with datasets
                    lineChart.data = lineData // Set data to the LineChart

                    // Customize the x-axis appearance
                    val xAxis = lineChart.xAxis
                    xAxis.position = XAxis.XAxisPosition.BOTTOM // Position the XAxis at the bottom
                    xAxis.granularity = 1f // Set granularity to 1 to avoid overlapping labels
                    xAxis.setDrawLabels(false) // Hide x-axis labels
                    xAxis.setDrawGridLines(false) // Disable grid lines
                    xAxis.setDrawAxisLine(false) // Disable axis line

                    // Customize the y-axis appearance
                    val leftAxis = lineChart.axisLeft
                    leftAxis.textColor = resources.getColor(R.color.white) // Set text color for YAxis labels
                    leftAxis.setDrawGridLines(false) // Disable grid lines
                    leftAxis.setDrawAxisLine(false) // Disable axis line
                    lineChart.axisRight.isEnabled = false // Disable the right YAxis

                    // Customize the chart's legend
                    val legend = lineChart.legend
                    legend.textColor = resources.getColor(R.color.white) // Set text color for legend

                    // Customize the overall chart appearance
                    lineChart.setDrawGridBackground(false) // Disable grid background
                    lineChart.setBackgroundColor(resources.getColor(R.color.Gray)) // Set background color of the chart
                    lineChart.description.isEnabled = false // Disable the chart description
                    lineChart.legend.isEnabled = true // Enable the legend
                    lineChart.setTouchEnabled(true) // Enable touch gestures
                    lineChart.isDragEnabled = true // Enable dragging
                    lineChart.setScaleEnabled(true) // Enable scaling
                    lineChart.isScaleXEnabled = true // Enable horizontal scaling
                    lineChart.isScaleYEnabled = true // Enable vertical scaling

                    // Refresh the chart to display the new data
                    lineChart.invalidate()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle errors while fetching the goal weight
                    Toast.makeText(this@Tracker, "Error loading goal", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /**
     * Saves the user's weight data to Firebase.
     *
     * @param weight The weight value entered by the user.
     * @param date The date corresponding to the weight entry.
     */
    private fun saveUserData(weight: String, date: String) {
        val userId = mAuth.currentUser?.uid ?: return // Get current user's UID or return if not logged in

        // Create a map representing the weight entry
        val weightEntry = mapOf(
            "weight" to weight,
            "date" to date
        )

        // Push the weight entry to Firebase under the user's "weight" node
        databaseReference.child("users").child(userId).child("weight").push().setValue(weightEntry)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Show success message and refresh data if saving is successful
                    Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
                    loadUserData() // Refresh the current weight and difference
                    loadGraphData() // Refresh the graph with the new weight data
                } else {
                    // Show error message if saving fails
                    Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Displays a bottom sheet dialog for adding new weight data.
     */
    private fun showBottomSheetDialogForData() {
        // Inflate the custom layout for adding data
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_data, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO

        // Initialize UI components within the bottom sheet
        val tvDate: TextView = view.findViewById(R.id.tvDate) // TextView to display selected date
        val etWeight: EditText = view.findViewById(R.id.etWeight) // EditText to enter weight
        val btnSaveData: Button = view.findViewById(R.id.btnSaveData) // Button to save data

        // Set OnClickListener for the date TextView to show a DatePickerDialog
        tvDate.setOnClickListener {
            showDatePickerDialog(tvDate) // Show DatePickerDialog to select a date
        }

        // Set OnClickListener for the Save Data button to save the entered weight and date
        btnSaveData.setOnClickListener {
            val weight = etWeight.text.toString().trim() // Get entered weight
            val date = tvDate.text.toString().trim() // Get selected date

            // Validate that both weight and date are entered
            if (weight.isNotEmpty() && date.isNotEmpty()) {
                saveUserData(weight, date) // Save the data to Firebase
                bottomSheetDialog.dismiss() // Close the bottom sheet after saving
            } else {
                // Show error message if fields are empty
                Toast.makeText(this, "Please enter both weight and date", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the bottom sheet dialog
        bottomSheetDialog.show()
    }

    /**
     * Displays a bottom sheet dialog for setting a weight goal.
     */
    private fun showBottomSheetDialogForGoal() {
        // Inflate the custom layout for setting a goal
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_set_goal, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO

        // Initialize UI components within the bottom sheet
        val etWeightGoal: EditText = view.findViewById(R.id.etWeightGoal) // EditText to enter goal weight
        val btnSaveGoal: Button = view.findViewById(R.id.btnSaveGoal) // Button to save the goal

        // Set OnClickListener for the Save Goal button to save the entered goal weight
        btnSaveGoal.setOnClickListener {
            val weightGoal = etWeightGoal.text.toString().trim() // Get entered goal weight

            // Validate that a goal weight is entered
            if (weightGoal.isNotEmpty()) {
                saveUserGoal(weightGoal) // Save the goal to Firebase
                bottomSheetDialog.dismiss() // Close the bottom sheet after saving
            } else {
                // Show error message if the field is empty
                Toast.makeText(this, "Please enter a weight goal", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the bottom sheet dialog
        bottomSheetDialog.show()
    }

    /**
     * Saves the user's weight goal to Firebase.
     *
     * @param goal The weight goal entered by the user.
     */
    private fun saveUserGoal(goal: String) {
        val userId = mAuth.currentUser?.uid ?: return // Get current user's UID or return if not logged in

        // Create a map representing the goal entry
        val goalEntry = mapOf(
            "goal" to goal
        )

        // Set the goal entry in Firebase under the user's "goal" node
        databaseReference.child("users").child(userId).child("goal").setValue(goalEntry)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Show success message and refresh data if saving is successful
                    Toast.makeText(this, "Goal saved", Toast.LENGTH_SHORT).show()
                    loadUserData() // Refresh the goal and difference
                    loadGraphData() // Refresh the graph if necessary
                } else {
                    // Show error message if saving fails
                    Toast.makeText(this, "Error saving goal", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Displays a DatePickerDialog to allow the user to select a date.
     *
     * @param tvDate The TextView that displays the selected date.
     */
    private fun showDatePickerDialog(tvDate: TextView) {
        val calendar = Calendar.getInstance() // Get current date
        val year = calendar.get(Calendar.YEAR) // Current year
        val month = calendar.get(Calendar.MONTH) // Current month
        val day = calendar.get(Calendar.DAY_OF_MONTH) // Current day

        // Initialize and show the DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this,
            R.style.CustomDatePickerDialogTheme, // Custom theme for the DatePickerDialog
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the selected date and set it to the TextView
                val selectedDate = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear)
                tvDate.text = selectedDate
            },
            year, month, day
        )
        datePickerDialog.show() // Display the DatePickerDialog
    }

    /**
     * Displays a bottom sheet dialog for selecting a date range to view statistics.
     */
    private fun showDateRangeDialog() {
        // Inflate the custom layout for selecting date range
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_select_dates, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO

        // Initialize UI components within the bottom sheet
        val tvStartDate: TextView = view.findViewById(R.id.tvStartDate) // TextView to display selected start date
        val tvEndDate: TextView = view.findViewById(R.id.tvEndDate) // TextView to display selected end date
        val btnOK: Button = view.findViewById(R.id.btnOK) // Button to confirm date range selection

        var startDate: Date? = null // Variable to hold the selected start date
        var endDate: Date? = null // Variable to hold the selected end date

        // Set OnClickListener for the Start Date TextView to show a DatePickerDialog
        tvStartDate.setOnClickListener {
            val calendar = Calendar.getInstance() // Get current date
            val year = calendar.get(Calendar.YEAR) // Current year
            val month = calendar.get(Calendar.MONTH) // Current month
            val day = calendar.get(Calendar.DAY_OF_MONTH) // Current day

            // Initialize and show the DatePickerDialog for start date
            val datePickerDialog = DatePickerDialog(
                this,
                R.style.CustomDatePickerDialogTheme, // Custom theme for the DatePickerDialog
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format the selected date and set it to the Start Date TextView
                    val selectedDateStr = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear)
                    tvStartDate.text = selectedDateStr
                    startDate = dateFormat.parse(selectedDateStr) // Parse the selected date string into a Date object

                    // Check if the selected start date is after the currently selected end date
                    if (endDate != null && startDate != null && startDate!!.after(endDate)) {
                        Toast.makeText(this, "Start date cannot be after end date", Toast.LENGTH_SHORT).show()
                        tvStartDate.text = "" // Clear the invalid start date selection
                        startDate = null // Reset the start date variable
                    }
                },
                year, month, day
            )
            datePickerDialog.show() // Display the DatePickerDialog
        }

        // Set OnClickListener for the End Date TextView to show a DatePickerDialog
        tvEndDate.setOnClickListener {
            val calendar = Calendar.getInstance() // Get current date
            val year = calendar.get(Calendar.YEAR) // Current year
            val month = calendar.get(Calendar.MONTH) // Current month
            val day = calendar.get(Calendar.DAY_OF_MONTH) // Current day

            // Initialize and show the DatePickerDialog for end date
            val datePickerDialog = DatePickerDialog(
                this,
                R.style.CustomDatePickerDialogTheme, // Custom theme for the DatePickerDialog
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format the selected date and set it to the End Date TextView
                    val selectedDateStr = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear)
                    tvEndDate.text = selectedDateStr
                    endDate = dateFormat.parse(selectedDateStr) // Parse the selected date string into a Date object

                    // Check if the selected end date is before the currently selected start date
                    if (startDate != null && endDate != null && endDate!!.before(startDate)) {
                        Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show()
                        tvEndDate.text = "" // Clear the invalid end date selection
                        endDate = null // Reset the end date variable
                    }
                },
                year, month, day
            )
            datePickerDialog.show() // Display the DatePickerDialog
        }

        // Set OnClickListener for the OK button to confirm the selected date range
        btnOK.setOnClickListener {
            val startDateStr = tvStartDate.text.toString().trim() // Get the selected start date string
            val endDateStr = tvEndDate.text.toString().trim() // Get the selected end date string

            // Validate that both start and end dates are selected
            if (startDateStr.isNotEmpty() && endDateStr.isNotEmpty()) {
                // Create an Intent to navigate to the Tracker_graph activity with the selected date range
                val intent = Intent(this, Tracker_graph::class.java)
                intent.putExtra("startDate", startDateStr) // Pass the start date as an extra
                intent.putExtra("endDate", endDateStr) // Pass the end date as an extra
                startActivity(intent) // Start the Tracker_graph activity
                bottomSheetDialog.dismiss() // Close the bottom sheet dialog
            } else {
                // Show error message if either date is not selected
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the bottom sheet dialog
        bottomSheetDialog.show()
    }
}
