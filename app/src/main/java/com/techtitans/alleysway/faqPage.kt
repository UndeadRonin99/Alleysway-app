// Package declaration
package com.techtitans.alleysway

// Importing required classes
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

// FAQ page activity definition
class faqPage : AppCompatActivity() {
    private lateinit var btnBack : ImageView // Lateinit for back button, initialized later

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq_page) // Setting the layout for the FAQ page
        enableEdgeToEdge() // Enabling edge-to-edge UI optimization

        btnBack = findViewById(R.id.imageViewBack) // Linking back button with its view

        btnBack.setOnClickListener {
            finish() // Finish activity and return to previous one when back button is clicked
        }

        // Setup for first FAQ item
        val faqItem1 = findViewById<LinearLayout>(R.id.faqItem1)
        val answer1 = findViewById<TextView>(R.id.answer1)
        val arrow1 = findViewById<ImageView>(R.id.arrow1)

        faqItem1.setOnClickListener {
            if (answer1.visibility == View.GONE) { // Check if answer is hidden
                slideDown(answer1) // Animate to show answer
                rotateArrow(arrow1, 0f, 90f) // Rotate arrow to indicate expansion
            } else {
                slideUp(answer1) // Animate to hide answer
                rotateArrow(arrow1, 90f, 0f) // Rotate arrow to indicate collapsing
            }
        }

        val faqItem2 = findViewById<LinearLayout>(R.id.faqItem2)
        val answer2 = findViewById<TextView>(R.id.answer2)
        val arrow2 = findViewById<ImageView>(R.id.arrow2)

        faqItem2.setOnClickListener {
            if (answer2.visibility == View.GONE) {
                slideDown(answer2)
                rotateArrow(arrow2, 0f, 90f)
            } else {
                slideUp(answer2)
                rotateArrow(arrow2, 90f, 0f)
            }
        }

        val faqItem3 = findViewById<LinearLayout>(R.id.faqItem3)
        val answer3 = findViewById<TextView>(R.id.answer3)
        val arrow3 = findViewById<ImageView>(R.id.arrow3)

        faqItem3.setOnClickListener {
            if (answer3.visibility == View.GONE) {
                slideDown(answer3)
                rotateArrow(arrow3, 0f, 90f)
            } else {
                slideUp(answer3)
                rotateArrow(arrow3, 90f, 0f)
            }
        }

        val faqItem4 = findViewById<LinearLayout>(R.id.faqItem4)
        val answer4 = findViewById<TextView>(R.id.answer4)
        val arrow4 = findViewById<ImageView>(R.id.arrow4)

        faqItem4.setOnClickListener {
            if (answer4.visibility == View.GONE) {
                slideDown(answer4)
                rotateArrow(arrow4, 0f, 90f)
            } else {
                slideUp(answer4)
                rotateArrow(arrow4, 90f, 0f)
            }
        }

        val faqItem5 = findViewById<LinearLayout>(R.id.faqItem5)
        val answer5 = findViewById<TextView>(R.id.answer5)
        val arrow5 = findViewById<ImageView>(R.id.arrow5)

        faqItem5.setOnClickListener {
            if (answer5.visibility == View.GONE) {
                slideDown(answer5)
                rotateArrow(arrow5, 0f, 90f)
            } else {
                slideUp(answer5)
                rotateArrow(arrow5, 90f, 0f)
            }
        }

        val faqItem6 = findViewById<LinearLayout>(R.id.faqItem6)
        val answer6 = findViewById<TextView>(R.id.answer6)
        val arrow6 = findViewById<ImageView>(R.id.arrow6)

        faqItem6.setOnClickListener {
            if (answer6.visibility == View.GONE) {
                slideDown(answer6)
                rotateArrow(arrow6, 0f, 90f)
            } else {
                slideUp(answer6)
                rotateArrow(arrow6, 90f, 0f)
            }
        }
    }

    // Function to rotate an arrow icon, indicating the current state of FAQ item (collapsed or expanded)
    private fun rotateArrow(arrow: ImageView, fromDegrees: Float, toDegrees: Float) {
        val rotate = RotateAnimation(
            fromDegrees, toDegrees,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotate.duration = 300 // Set duration of the animation
        rotate.fillAfter = true // Make sure rotation does not reset after animation
        arrow.startAnimation(rotate) // Start the animation
    }

    // Function to slide down a view, used here to reveal an answer to an FAQ item
    private fun slideDown(view: View) {
        view.visibility = View.VISIBLE // Set visibility to VISIBLE
        val animate = TranslateAnimation(
            0f, 0f, -view.height.toFloat(), 0f
        )
        animate.duration = 300 // Animation duration
        view.startAnimation(animate) // Start the animation
    }

    // Function to slide up a view, used here to hide an answer to an FAQ item
    private fun slideUp(view: View) {
        val animate = TranslateAnimation(
            0f, 0f, 0f, -view.height.toFloat()
        )
        animate.duration = 300 // Animation duration
        view.startAnimation(animate) // Start the animation
        view.visibility = View.GONE // Set visibility to GONE after animation completes
    }
}
