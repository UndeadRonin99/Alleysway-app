package com.example.alleysway

import android.os.Bundle
import android.widget.ExpandableListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Stronger_function_page_1 : AppCompatActivity() {

    private lateinit var expandableListView: ExpandableListView
    private lateinit var listGroup: List<String>
    private lateinit var listItem: HashMap<String, List<String>>
    private lateinit var adapter: ExpandableListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stronger_function_page1)

        // Adjust layout insets to fit properly within the system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the expandable list view
        expandableListView = findViewById(R.id.expandableListView)

        // Initialize the data for the expandable list
        listGroup = listOf(
            "Shoulders", "Back", "Chest", "Biceps", "Triceps",
            "Forearms", "Core", "Quads", "Hamstrings", "Glutes",
            "Calves", "Cardio", "Other"
        )

        listItem = hashMapOf(
            "Shoulders" to listOf("Overhead Press", "Lateral Raise", "Arnold Press"),
            "Back" to listOf("Pull-ups", "Deadlifts", "Bent-over Row"),
            "Chest" to listOf("Bench Press", "Chest Fly", "Push-ups"),
            "Biceps" to listOf("Barbell Curl", "Hammer Curl", "Concentration Curl"),
            "Triceps" to listOf("Triceps Dip", "Skull Crushers", "Triceps Pushdown"),
            "Forearms" to listOf("Wrist Curl", "Reverse Wrist Curl"),
            "Core" to listOf("Crunches", "Leg Raises", "Plank"),
            "Quads" to listOf("Squats", "Leg Press", "Lunges"),
            "Hamstrings" to listOf("Romanian Deadlift", "Leg Curl", "Glute Ham Raise"),
            "Glutes" to listOf("Hip Thrust", "Glute Bridge", "Bulgarian Split Squat"),
            "Calves" to listOf("Calf Raise", "Seated Calf Raise"),
            "Cardio" to listOf("Running", "Cycling", "Jump Rope"),
            "Other" to listOf("Stretching", "Mobility Drills", "Foam Rolling")
        )

        // Set up the expandable list adapter
        adapter = ExpandableListAdapter(this, listGroup, listItem)
        expandableListView.setAdapter(adapter)
    }
}
