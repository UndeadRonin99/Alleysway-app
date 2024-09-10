package com.example.alleysway

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val createAccountTextView = findViewById<TextView>(R.id.text_signup)

        createAccountTextView.setOnClickListener {
            navigateToRegisterPage()
        }
    }

        private fun navigateToRegisterPage() {
            startActivity(Intent(this, Register_page::class.java))
        }
    }
