import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.alleysway.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Bookings : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookings)

        barChart = findViewById(R.id.popularTimesChart)
        databaseReference = FirebaseDatabase.getInstance().getReference("attendance")

        loadPopularTimesData()
    }

    private fun loadPopularTimesData() {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val attendanceRef = databaseReference.child(todayDate)

        attendanceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = ArrayList<BarEntry>()
                var hour = 0
                for (hourSnapshot in snapshot.children) {
                    val count = hourSnapshot.getValue(Int::class.java) ?: 0
                    entries.add(BarEntry(hour.toFloat(), count.toFloat()))
                    hour++
                }

                val barDataSet = BarDataSet(entries, "Popular Times")
                val barData = BarData(barDataSet)
                barChart.data = barData
                barChart.invalidate() // Refresh chart
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Bookings, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
