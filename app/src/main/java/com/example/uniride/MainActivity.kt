package com.example.uniride

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide the action bar
        supportActionBar?.hide()

        // Adjust the logo or header programmatically (if needed)
        val logo = findViewById<ImageView>(R.id.logoImage)
        logo.setImageResource(R.drawable.taxi_logo) // Replace with your image resource

        // Add delay and transition to another activity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, login::class.java)) // Replace `NextActivity` with the target activity
            finish()
        }, 3000) // 3 seconds delay
    }
}
