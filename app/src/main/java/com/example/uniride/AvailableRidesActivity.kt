package com.example.uniride

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uniride.databinding.ActivityAvailableRidesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AvailableRidesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAvailableRidesBinding
    private lateinit var ridesAdapter: AvailableRidesAdapter
    private val db = FirebaseFirestore.getInstance()
    private var searchCriteria: RideSearchCriteria? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvailableRidesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get search criteria from intent
        searchCriteria = intent.getParcelableExtra("searchCriteria")

        setupUI()
        searchRides()
    }

    private fun setupUI() {
        binding.apply {
            // Format the route text
            routeText.text = "${searchCriteria?.pickupLocation} → ${searchCriteria?.dropLocation}"

            // Format date and passengers text
            val dateStr = searchCriteria?.date ?: ""
            val passengers = searchCriteria?.passengers ?: 1
            datePassengersText.text = "$dateStr, $passengers passenger(s)"

            // Set current date in header
            dateHeaderText.text = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                .format(Date())

            // Setup back button
            backButton.setOnClickListener { finish() }

            // Setup RecyclerView
            ridesAdapter = AvailableRidesAdapter { ride -> handleBooking(ride) }
            ridesRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@AvailableRidesActivity)
                adapter = ridesAdapter
            }
        }
    }
    private fun searchRides() {
        binding.loadingProgressBar.visibility = View.VISIBLE  // Add this ProgressBar to your layout

        searchCriteria?.let { criteria ->
            db.collection("rides")
                .whereEqualTo("startLocation", criteria.pickupLocation.trim())
                .whereEqualTo("destination", criteria.dropLocation.trim())
                .whereEqualTo("date", criteria.date)
                .whereEqualTo("status", "active")
                .whereGreaterThanOrEqualTo("availableSeats", criteria.passengers)
                .get()
                .addOnSuccessListener { documents ->
                    binding.loadingProgressBar.visibility = View.GONE

                    if (documents.isEmpty) {
                        updateEmptyState(true)
                        return@addOnSuccessListener
                    }

                    val rides = mutableListOf<AvailableRide>()
                    var processedDocs = 0

                    // Process each ride document
                    documents.forEach { doc ->
                        try {
                            val ride = doc.toObject(AvailableRide::class.java).copy(id = doc.id)
                            // Fetch driver details
                            fetchDriverDetails(ride) { completeRide ->
                                rides.add(completeRide)
                                processedDocs++

                                if (processedDocs == documents.size()) {
                                    // Sort rides by some criteria (e.g., time or price)
                                    val sortedRides = rides.sortedBy { it.startTime }
                                    ridesAdapter.submitList(sortedRides)
                                    updateEmptyState(rides.isEmpty())
                                }
                            }
                        } catch (e: Exception) {
                            processedDocs++
                            Log.e("AvailableRides", "Error processing ride document: ${e.message}")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    binding.loadingProgressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Error searching rides: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateEmptyState(true)
                }
        } ?: run {
            Toast.makeText(this, "Invalid search criteria", Toast.LENGTH_SHORT).show()
            updateEmptyState(true)
        }
    }

    private fun fetchDriverDetails(ride: AvailableRide, onComplete: (AvailableRide) -> Unit) {
        db.collection("users").document(ride.driverId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val updatedRide = ride.copy(
                        driverName = doc.getString("name") ?: "Unknown Driver",
                        driverRating = doc.getDouble("rating") ?: 0.0,
                        driverPhoto = doc.getString("photoUrl") ?: ""
                    )
                    onComplete(updatedRide)
                } else {
                    onComplete(ride.copy(driverName = "Unknown Driver"))
                }
            }
            .addOnFailureListener {
                onComplete(ride.copy(driverName = "Unknown Driver"))
            }
    }

    private fun handleBooking(ride: AvailableRide) {
        // Show booking confirmation dialog
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Booking")
            .setMessage("Would you like to book this ride?\nPayment Mode: Cash")
            .setPositiveButton("Book") { _, _ ->
                bookRide(ride)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun bookRide(ride: AvailableRide) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Check if user is logged in
        if (currentUser == null) {
            Toast.makeText(this, "Please login to book a ride", Toast.LENGTH_SHORT).show()
            // Optionally redirect to login
            // startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        // Show loading indicator
        val loadingDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Booking Ride")
            .setMessage("Please wait...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        db.collection("bookings")
            .whereEqualTo("rideId", ride.id)
            .whereEqualTo("passengerId", currentUser.uid)
            .get()
            .addOnSuccessListener { bookingSnapshot ->
                if (!bookingSnapshot.isEmpty) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this,
                        "You already have a booking for this ride",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                val booking = hashMapOf(
                    "rideId" to ride.id,
                    "passengerId" to currentUser.uid,
                    "driverId" to ride.driverId,  // Add driver ID for permissions
                    "seats" to searchCriteria?.passengers,
                    "status" to "confirmed",
                    "bookedAt" to FieldValue.serverTimestamp(),
                    "pickupLocation" to ride.startLocation,
                    "dropLocation" to ride.destination,
                    "price" to ride.price,
                    "date" to ride.date
                )

                db.runTransaction { transaction ->
                    val rideRef = db.collection("rides").document(ride.id)
                    val rideDoc = transaction.get(rideRef)

                    // Verify ride still exists and has enough seats
                    if (!rideDoc.exists()) {
                        throw Exception("Ride no longer available")
                    }

                    val currentSeats = rideDoc.getLong("availableSeats") ?: 0
                    val requestedSeats = searchCriteria?.passengers ?: 1

                    if (currentSeats < requestedSeats) {
                        throw Exception("Not enough seats available")
                    }

                    // Update available seats
                    transaction.update(rideRef, "availableSeats", currentSeats - requestedSeats)

                    // Add booking document
                    val bookingRef = db.collection("bookings").document()
                    transaction.set(bookingRef, booking)
                }
                    .addOnSuccessListener {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Ride booked successfully!", Toast.LENGTH_SHORT).show()
                        sendNotificationToDriver(ride.driverId)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        loadingDialog.dismiss()
                        Toast.makeText(
                            this,
                            "Booking failed: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()

                        if (e.message == "PERMISSION_DENIED") {
                            // Force re-authentication
                            FirebaseAuth.getInstance().signOut()
                            // Redirect to login
                            // startActivity(Intent(this, LoginActivity::class.java))
                        }
                    }
                    .addOnFailureListener { e ->
                        loadingDialog.dismiss()
                        Toast.makeText(
                            this,
                            "Error checking existing bookings: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
    }

    private fun sendNotificationToDriver(driverId: String) {
        // Implement FCM notification here
        // This is a placeholder for notification implementation
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            if (isEmpty) {
                emptyStateLayout.visibility = View.VISIBLE
                ridesRecyclerView.visibility = View.GONE
            } else {
                emptyStateLayout.visibility = View.GONE
                ridesRecyclerView.visibility = View.VISIBLE
            }
        }
    }
}