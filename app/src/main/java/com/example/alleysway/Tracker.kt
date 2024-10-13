package com.example.alleysway

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class Tracker : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var btnAddData: Button
    private lateinit var btnSetGoal: Button

    private lateinit var tvCurrentWeight: TextView
    private lateinit var tvGoalWeight: TextView
    private lateinit var tvDifference: TextView

    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tracker)

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Get references to buttons and text views
        btnAddData = findViewById(R.id.AddData)
        btnSetGoal = findViewById(R.id.btnGoals)
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight)
        tvGoalWeight = findViewById(R.id.tvGoalWeight)
        tvDifference = findViewById(R.id.tvDifference)

        // Initialize LineChart
        lineChart = findViewById(R.id.lineChart)

        // On clicking Add Data, show the bottom sheet dialog for adding weight data
        btnAddData.setOnClickListener {
            showBottomSheetDialogForData()
        }

        // On clicking Set Goal, show the bottom sheet dialog for setting a weight goal
        btnSetGoal.setOnClickListener {
            showBottomSheetDialogForGoal()
        }

        // Load the current weight, goal, and calculate the difference
        loadUserData()

        // Load weight data and display graph
        loadGraphData()
    }

    // Load user data to display the current weight, goal, and difference
    private fun loadUserData() {
        val userId = mAuth.currentUser?.uid ?: return

        // Fetch the latest weight entry
        databaseReference.child("users").child(userId).child("weight")
            .orderByChild("date").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if snapshot exists
                    if (!snapshot.exists()) {
                        tvCurrentWeight.text = "Now"
                        tvGoalWeight.text = "Target"
                        tvDifference.text = "Change"
                        return
                    }

                    val latestWeight = snapshot.children.firstOrNull()?.child("weight")?.getValue(String::class.java)?.toFloatOrNull()

                    if (latestWeight != null) {
                        tvCurrentWeight.text = "$latestWeight\nNow"

                        // Fetch the goal and calculate the difference
                        databaseReference.child("users").child(userId).child("goal").child("goal")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(goalSnapshot: DataSnapshot) {
                                    val goalWeight = goalSnapshot.getValue(String::class.java)?.toFloatOrNull()

                                    if (goalWeight != null) {
                                        tvGoalWeight.text = "$goalWeight\nTarget"
                                        val difference = latestWeight - goalWeight
                                        tvDifference.text = "${"%.2f".format(difference)}\nChange"
                                    } else {
                                        tvGoalWeight.text = "Target\n--"
                                        tvDifference.text = "Change\n--"
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@Tracker, "Error loading goal", Toast.LENGTH_SHORT).show()
                                }
                            })
                    } else {
                        tvCurrentWeight.text = "Now"
                        tvGoalWeight.text = "Target"
                        tvDifference.text = "Change"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Tracker, "Error loading weight data", Toast.LENGTH_SHORT).show()
                }
            })
    }



    // Load the weight data and display it on the line chart
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
                        showNoDataMessage()
                        return
                    }

                    val weightEntries = mutableListOf<Entry>()
                    val dates = mutableListOf<String>()

                    var index = 0f
                    for (childSnapshot in snapshot.children) {
                        val weight = childSnapshot.child("weight").getValue(String::class.java)?.toFloatOrNull()

                        if (weight != null ) {
                            weightEntries.add(Entry(index, weight))

                            index++
                        }
                    }

                    // Display the line chart with the weight data if entries exist
                    if (weightEntries.isNotEmpty()) {
                        lineChart.visibility = View.VISIBLE // Show the chart
                        displayLineGraph(weightEntries)
                    } else {
                        // Hide the chart and show a message
                        lineChart.visibility = View.GONE
                        showNoDataMessage()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Tracker, "Error loading data", Toast.LENGTH_SHORT).show()
                }
            })
    }


    // Method to show a message when no data is available
    private fun showNoDataMessage() {
        // Hide the chart
        lineChart.visibility = View.GONE

        // Display a message
        val noDataTextView: TextView = findViewById(R.id.noDataTextView)
        noDataTextView.visibility = View.VISIBLE
        noDataTextView.text = "Please enter data to see statistics"
    }



    private fun displayLineGraph(weightEntries: List<Entry>) {
        val userId = mAuth.currentUser?.uid ?: return
        lineChart.visibility = View.VISIBLE // Ensure the graph is visible when there is data

        // Fetch the weight goal
        databaseReference.child("users").child(userId).child("goal").child("goal")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val goalWeight = snapshot.getValue(String::class.java)?.toFloatOrNull()

                    // Create the dataset for the weight entries (this will display as a line)
                    val weightDataSet = LineDataSet(weightEntries, "Weight")
                    weightDataSet.color = resources.getColor(R.color.orange)
                    weightDataSet.lineWidth = 2f
                    weightDataSet.setDrawCircles(true)  // Keep circles on data points
                    weightDataSet.setDrawCircleHole(false)  // Remove the hole in the circles
                    weightDataSet.circleRadius = 4f  // Circle size
                    weightDataSet.setDrawValues(true)  // Show the values on the chart
                    weightDataSet.setDrawFilled(true)  // Optionally fill below the line
                    weightDataSet.fillColor = resources.getColor(R.color.orange)  // Fill color

                    val dataSets = mutableListOf<LineDataSet>()
                    dataSets.add(weightDataSet)

                    // If there is a goal weight, add a horizontal line for it
                    if (goalWeight != null) {
                        // Create entries for the goal line (a straight horizontal line)
                        val goalEntries = mutableListOf<Entry>()
                        goalEntries.add(Entry(0f, goalWeight)) // Starting point
                        goalEntries.add(Entry(weightEntries.size.toFloat() - 1, goalWeight)) // End point at the same level

                        val goalDataSet = LineDataSet(goalEntries, "Goal Weight")
                        goalDataSet.color = resources.getColor(R.color.green)  // Green color for goal line
                        goalDataSet.lineWidth = 1.5f
                        goalDataSet.setDrawCircles(false)  // No circles for goal line
                        goalDataSet.setDrawValues(false)
                        goalDataSet.enableDashedLine(10f, 5f, 0f)  // Dashed line

                        dataSets.add(goalDataSet)
                    }

                    // Create line data and set it to the chart
                    val lineData = LineData(dataSets as List<LineDataSet>)
                    lineChart.data = lineData

                    // Customize the x-axis (but no need to display labels)
                    val xAxis = lineChart.xAxis
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.setDrawLabels(false)  // Disable x-axis labels
                    xAxis.setDrawGridLines(false)
                    xAxis.setDrawAxisLine(false)

                    // Customize the y-axis
                    val leftAxis = lineChart.axisLeft
                    leftAxis.textColor = resources.getColor(R.color.white)
                    leftAxis.setDrawGridLines(false)
                    leftAxis.setDrawAxisLine(false)
                    lineChart.axisRight.isEnabled = false

                    // Customize the chart
                    lineChart.setDrawGridBackground(false)
                    lineChart.setBackgroundColor(resources.getColor(R.color.Gray))
                    lineChart.description.isEnabled = false
                    lineChart.legend.isEnabled = true
                    lineChart.setTouchEnabled(true)
                    lineChart.setDragEnabled(true)
                    lineChart.setScaleEnabled(true)
                    lineChart.setScaleXEnabled(true)
                    lineChart.setScaleYEnabled(true)

                    // Refresh the chart
                    lineChart.invalidate()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Tracker, "Error loading goal", Toast.LENGTH_SHORT).show()
                }
            })
    }



    // Save user data (weight and date) to Firebase
    private fun saveUserData(weight: String, date: String) {
        val userId = mAuth.currentUser?.uid ?: return
        val weightEntry = mapOf(
            "weight" to weight,
            "date" to date
        )

        databaseReference.child("users").child(userId).child("weight").push().setValue(weightEntry)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
                    loadUserData() // Refresh the data after saving
                    loadGraphData() // Refresh the graph after saving
                } else {
                    Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Formatter for the x-axis to show days of the week instead of dates



    // Show the bottom sheet dialog for adding weight data
    private fun showBottomSheetDialogForData() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_data, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO

        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val etWeight: EditText = view.findViewById(R.id.etWeight)
        val btnSaveData: Button = view.findViewById(R.id.btnSaveData)

        tvDate.setOnClickListener {
            showDatePickerDialog(tvDate)
        }

        btnSaveData.setOnClickListener {
            val weight = etWeight.text.toString().trim()
            val date = tvDate.text.toString().trim()

            if (weight.isNotEmpty() && date.isNotEmpty()) {
                saveUserData(weight, date)
                bottomSheetDialog.dismiss() // Close the bottom sheet when data is saved
            } else {
                Toast.makeText(this, "Please enter both weight and date", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }

    // Show the bottom sheet dialog for setting a weight goal
    private fun showBottomSheetDialogForGoal() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_set_goal, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO

        val etWeightGoal: EditText = view.findViewById(R.id.etWeightGoal)
        val btnSaveGoal: Button = view.findViewById(R.id.btnSaveGoal)

        btnSaveGoal.setOnClickListener {
            val weightGoal = etWeightGoal.text.toString().trim()

            if (weightGoal.isNotEmpty()) {
                saveUserGoal(weightGoal)
                bottomSheetDialog.dismiss() // Close the bottom sheet when goal is saved
            } else {
                Toast.makeText(this, "Please enter a weight goal", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }

    // Save user goal to Firebase
    private fun saveUserGoal(goal: String) {
        val userId = mAuth.currentUser?.uid ?: return
        val goalEntry = mapOf(
            "goal" to goal
        )

        databaseReference.child("users").child(userId).child("goal").setValue(goalEntry)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Goal saved", Toast.LENGTH_SHORT).show()
                    loadUserData() // Refresh the data after saving
                    loadGraphData() // Update the graph
                } else {
                    Toast.makeText(this, "Error saving goal", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Show DatePickerDialog when clicking the Date TextView
    private fun showDatePickerDialog(tvDate: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear)
                tvDate.text = selectedDate
            },
            year, month, day
        )
        datePickerDialog.show()
    }
}
