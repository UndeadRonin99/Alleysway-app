package com.techtitans.alleysway

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class faqPage : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq_page)

        // Setup FAQ item toggling
        val faqItem1 = findViewById<LinearLayout>(R.id.faqItem1)
        val answer1 = findViewById<TextView>(R.id.answer1)
        val arrow1 = findViewById<ImageView>(R.id.arrow1)

        faqItem1.setOnClickListener {
            if (answer1.visibility == View.GONE) {
                slideDown(answer1)  // Slide down to show the answer
                rotateArrow(arrow1, 0f, 90f)  // Rotate the arrow to point down
            } else {
                slideUp(answer1)  // Slide up to hide the answer
                rotateArrow(arrow1, 90f, 0f)  // Rotate the arrow to point right
            }
        }

        val faqItem2 = findViewById<LinearLayout>(R.id.faqItem2)
        val answer2 = findViewById<TextView>(R.id.answer2)
        val arrow2 = findViewById<ImageView>(R.id.arrow2)

        faqItem2.setOnClickListener {
            if (answer2.visibility == View.GONE) {
                slideDown(answer2)  // Slide down to show the answer
                rotateArrow(arrow2, 0f, 90f)  // Rotate the arrow to point down
            } else {
                slideUp(answer2)  // Slide up to hide the answer
                rotateArrow(arrow2, 90f, 0f)  // Rotate the arrow to point right
            }
        }

        val faqItem3 = findViewById<LinearLayout>(R.id.faqItem3)
        val answer3 = findViewById<TextView>(R.id.answer3)
        val arrow3 = findViewById<ImageView>(R.id.arrow3)

        faqItem3.setOnClickListener {
            if (answer3.visibility == View.GONE) {
                slideDown(answer3)  // Slide down to show the answer
                rotateArrow(arrow3, 0f, 90f)  // Rotate the arrow to point down
            } else {
                slideUp(answer3)  // Slide up to hide the answer
                rotateArrow(arrow3, 90f, 0f)  // Rotate the arrow to point right
            }
        }

        val faqItem4 = findViewById<LinearLayout>(R.id.faqItem4)
        val answer4 = findViewById<TextView>(R.id.answer4)
        val arrow4 = findViewById<ImageView>(R.id.arrow4)

        faqItem4.setOnClickListener {
            if (answer4.visibility == View.GONE) {
                slideDown(answer4)  // Slide down to show the answer
                rotateArrow(arrow4, 0f, 90f)  // Rotate the arrow to point down
            } else {
                slideUp(answer4)  // Slide up to hide the answer
                rotateArrow(arrow4, 90f, 0f)  // Rotate the arrow to point right
            }
        }

        val faqItem5 = findViewById<LinearLayout>(R.id.faqItem5)
        val answer5 = findViewById<TextView>(R.id.answer5)
        val arrow5 = findViewById<ImageView>(R.id.arrow5)

        faqItem5.setOnClickListener {
            if (answer5.visibility == View.GONE) {
                slideDown(answer5)  // Slide down to show the answer
                rotateArrow(arrow5, 0f, 90f)  // Rotate the arrow to point down
            } else {
                slideUp(answer5)  // Slide up to hide the answer
                rotateArrow(arrow5, 90f, 0f)  // Rotate the arrow to point right
            }
        }


    }

    // Function to animate the arrow rotation
    private fun rotateArrow(arrow: ImageView, fromDegrees: Float, toDegrees: Float) {
        val rotate = android.view.animation.RotateAnimation(
            fromDegrees, toDegrees,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotate.duration = 300
        rotate.fillAfter = true
        arrow.startAnimation(rotate)
    }

    // Function to slide down (show) the answer
    private fun slideDown(view: View) {
        view.visibility = View.VISIBLE
        val animate = TranslateAnimation(0f, 0f, -view.height.toFloat(), 0f)
        animate.duration = 300
        view.startAnimation(animate)
    }

    // Function to slide up (hide) the answer
    private fun slideUp(view: View) {
        val animate = TranslateAnimation(0f, 0f, 0f, -view.height.toFloat())
        animate.duration = 300
        view.startAnimation(animate)
        view.visibility = View.GONE
    }
}
