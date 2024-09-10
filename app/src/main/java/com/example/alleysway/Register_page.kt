package com.example.alleysway

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Register_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_page)

        val loginTextView = findViewById<TextView>(R.id.text_login)
        loginTextView.setOnClickListener {
            navigateToLoginPage()
        }
    }
        private fun navigateToLoginPage() {
            startActivity(Intent(this, MainActivity::class.java))
        }
//showng matt how to do commits here
}