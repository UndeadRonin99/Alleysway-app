// File: app/src/main/java/com/example/alleysway/MainActivity.kt
package com.techtitans.alleysway

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

/**
 * MainActivity serves as the entry point of the application, displaying a splash screen with a fade-in animation.
 * After the animation ends, it navigates to the LoginPage activity.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Set the layout for the activity

        // Find the ImageView that displays the logo
        val logoImageView: ImageView = findViewById(R.id.imageView2)

        // Create a fade-in animation for the logo's alpha property from 0 (transparent) to 1 (opaque)
        val fadeInAnimator = ObjectAnimator.ofFloat(logoImageView, "alpha", 0f, 1f).apply {
            duration = 1500 // Animation duration in milliseconds (1.5 seconds)
            interpolator = AccelerateInterpolator() // Accelerate the animation for a smooth effect
        }

        // Add a listener to handle events during the animation lifecycle
        fadeInAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                // Optional: Actions to perform when the animation starts
            }

            override fun onAnimationEnd(animation: Animator) {
                // After the animation ends, navigate to the LoginPage activity
                val intent = Intent(this@MainActivity, LoginPage::class.java)
                startActivity(intent) // Start the LoginPage activity
                finish() // Finish MainActivity so it's removed from the back stack
            }

            override fun onAnimationCancel(animation: Animator) {
                // Optional: Actions to perform if the animation is canceled
            }

            override fun onAnimationRepeat(animation: Animator) {
                // Optional: Actions to perform if the animation repeats
            }
        })

        // Start the fade-in animation
        fadeInAnimator.start()
    }
}
