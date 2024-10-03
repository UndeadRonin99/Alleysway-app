package com.example.alleysway

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class RegisterPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)

        // Find the Register button by its ID
        val registerButton: Button = findViewById(R.id.btnRegister)

        // Set a click listener to navigate to HomePage
        registerButton.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        // Handle the Login button to navigate back to LoginPage
        val loginButton: Button = findViewById(R.id.btnLoginPage1)
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }
    }
}
