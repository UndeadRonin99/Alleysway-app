package com.techtitans.alleysway

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logoImageView: ImageView = findViewById(R.id.imageView2)

        val fadeInAnimator = ObjectAnimator.ofFloat(logoImageView, "alpha", 0f, 1f).apply {
            duration = 1500
            interpolator = AccelerateInterpolator()
        }

        fadeInAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                val intent = Intent(this@MainActivity, LoginPage::class.java)
                startActivity(intent)
                finish()
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })

        fadeInAnimator.start()
    }
}
