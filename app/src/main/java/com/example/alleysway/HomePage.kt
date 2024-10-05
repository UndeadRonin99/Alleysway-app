package com.example.alleysway

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat // Use GravityCompat for drawers

class HomePage : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Find the DrawerLayout and btnSettings
        drawerLayout = findViewById(R.id.drawer_layout)
        val btnSettings: ImageView = findViewById(R.id.btnSettings)

        // Open the sidebar (drawer) when the settings button is clicked
        btnSettings.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.openDrawer(GravityCompat.END) // Open the right drawer using GravityCompat.END
            }
        }

        // Close the sidebar (drawer) when the user clicks outside the sidebar
        drawerLayout.setDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: android.view.View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
            }

            override fun onDrawerClosed(drawerView: android.view.View) {
                super.onDrawerClosed(drawerView)
            }
        })
    }

    // Handle back button press to close the drawer if it's open
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END) // Close the right drawer using GravityCompat.END
        } else {
            super.onBackPressed() // Otherwise, follow the normal back button behavior
        }
    }
}
