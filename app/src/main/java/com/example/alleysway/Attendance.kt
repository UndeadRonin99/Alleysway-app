package com.example.alleysway

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alleysway.data.CalendarDay
import com.example.alleysway.data.MonthData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

class Attendance : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var monthHeaderRecyclerView: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private val attendanceData = mutableMapOf<LocalDate, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        monthHeaderRecyclerView = findViewById(R.id.monthHeaderRecyclerView)
        recyclerView = findViewById(R.id.calendarRecyclerView)

        monthHeaderRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val layoutManager = GridLayoutManager(this, 7, RecyclerView.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        // Set up monthHeaderRecyclerView
        val months = listOf(
            MonthData("Jan", 31),
            MonthData("Feb", 28), // or 29 for leap years
            MonthData("Mar", 31),
            MonthData("Apr", 30),
            MonthData("May", 31),
            MonthData("Jun", 30),
            MonthData("Jul", 31),
            MonthData("Aug", 31),
            MonthData("Sep", 30),
            MonthData("Oct", 31),
            MonthData("Nov", 30),
            MonthData("Dec", 31)
        )
        val monthAdapter = MonthHeaderAdapter(months)
        monthHeaderRecyclerView.adapter = monthAdapter

        // Fetch attendance data and set up the calendar
        val userId = Firebase.auth.currentUser?.uid

        if (userId != null) {
            fetchAttendanceData(userId)
        }

        // Synchronize scrolling
        synchronizeScrolling(monthHeaderRecyclerView, recyclerView)
    }

    private fun synchronizeScrolling(headerView: RecyclerView, mainView: RecyclerView) {
        headerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                mainView.scrollBy(dx, dy)
            }
        })

        mainView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                headerView.scrollBy(dx, dy)
            }
        })
    }

//    private fun updateMonthHeader(date: LocalDate) {
//        val monthName = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
//        val year = date.year
//
//        val monthHeaderView = findViewById<TextView>(R.id.monthHeader)
//        monthHeaderView.text = "$monthName $year"
//    }

    private fun setupCalendar() {
        val startMonth = YearMonth.now().minusMonths(6) // Adjust as needed
        val endMonth = YearMonth.now()

        val startDate = startMonth.atDay(1)
        val endDate = endMonth.atEndOfMonth()

        val days = generateCalendarDays(startDate, endDate, attendanceData)

        adapter = CalendarAdapter(days)
        recyclerView.adapter = adapter
    }

    private fun fetchAttendanceData(userId: String) {
        val database = FirebaseDatabase.getInstance().reference
        val attendanceRef = database.child("users").child(userId).child("attendance")

        attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val dateStr = childSnapshot.key
                    val value = childSnapshot.getValue(Boolean::class.java)
                    if (dateStr != null && value == true) {
                        val date = LocalDate.parse(dateStr)
                        attendanceData[date] = attendanceData.getOrDefault(date, 0) + 1
                    }
                }
                // After fetching data, set up the calendar
                Log.d("AttendanceActivity", "Attendance Data: $attendanceData")
                setupCalendar()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun generateCalendarDays(
        startDate: LocalDate,
        endDate: LocalDate,
        attendanceData: Map<LocalDate, Int>
    ): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()

        // Define today as the last relevant date
        val today = LocalDate.now()
        val finalDate = if (endDate.isBefore(today)) endDate else today

        // Adjust the start date to align with full weeks (Sunday to Saturday)
        val currentStartDate = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        var currentDate = currentStartDate

        while (currentDate <= finalDate) {
            if (currentDate.isBefore(startDate) || currentDate.isAfter(finalDate)) {
                days.add(CalendarDay(date = null)) // Empty day slot
            } else {
                val attendanceCount = attendanceData.getOrDefault(currentDate, 0)
                days.add(CalendarDay(date = currentDate, attendanceCount = attendanceCount))
            }
            currentDate = currentDate.plusDays(1)
        }
        return days
    }
}