package com.example.uniride

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "HomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "Setting content view")
            setContentView(R.layout.activity_home)

            Log.d(TAG, "Initializing fragments")
            val takeRideFragment = TakeRideFragment()
            val offerRideFragment = OfferRideFragment()
            val profileFragment = ProfileFragment()

            Log.d(TAG, "Finding bottom navigation view")
            val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                ?: throw Exception("Bottom navigation view not found")

            Log.d(TAG, "Setting initial fragment")
            setCurrentFragment(takeRideFragment)

            Log.d(TAG, "Setting up navigation listener")
            bottomNavigation.setOnItemSelectedListener { item ->
                try {
                    when(item.itemId) {
                        R.id.nav_take_ride -> {
                            Log.d(TAG, "Navigating to take ride")
                            setCurrentFragment(takeRideFragment)
                        }
                        R.id.nav_offer_ride -> {
                            Log.d(TAG, "Navigating to offer ride")
                            setCurrentFragment(offerRideFragment)
                        }
                        R.id.nav_profile -> {
                            Log.d(TAG, "Navigating to profile")
                            setCurrentFragment(profileFragment)
                        }
                        else -> {
                            Log.w(TAG, "Unknown navigation item: ${item.itemId}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in navigation", e)
                    Toast.makeText(this, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        try {
            Log.d(TAG, "Replacing fragment: ${fragment.javaClass.simpleName}")
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment)
                commit()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting fragment", e)
            Toast.makeText(this, "Error loading screen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}