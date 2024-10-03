package com.example.alleysway

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class LoginPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        // Find the Login button by its ID
        val loginButton: Button = findViewById(R.id.btnLogin)

        // Set a click listener to navigate to HomePage
        loginButton.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        // Handle the SignUp button to navigate to RegisterPage
        val signUpButton: Button = findViewById(R.id.btnSignUpPage)
        signUpButton.setOnClickListener {
            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
        }
    }
}
