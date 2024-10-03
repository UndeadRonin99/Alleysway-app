package com.example.alleysway

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the ImageView by its ID
        val logoImageView: ImageView = findViewById(R.id.imageView2)

        // Create an ObjectAnimator for the fade-in effect
        val fadeInAnimator = ObjectAnimator.ofFloat(logoImageView, "alpha", 0f, 1f).apply {
            duration = 1500 // Duration of the animation in milliseconds
            interpolator = AccelerateInterpolator() // Smooth fading effect
        }

        // Start the fade-in animation
        fadeInAnimator.start()
    }
}
