package com.example.uniride

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class OfferRideFragment : Fragment() {
    private lateinit var vehicleTypeDropdown: AutoCompleteTextView
    private lateinit var vehicleNumberEdit: TextInputEditText
    private lateinit var startLocationEdit: TextInputEditText
    private lateinit var destinationEdit: TextInputEditText
    private lateinit var dateEdit: TextInputEditText
    private lateinit var timeEdit: TextInputEditText
    private lateinit var seatsEdit: TextInputEditText
    private lateinit var priceEdit: TextInputEditText

    private val vehicleTypes = arrayOf("Car", "SUV", "Bike")
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_offer_ride, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupVehicleTypeDropdown()
        setupDateTimePickers()
        setupPublishButton()
        //testDatabaseConnection() // Add this to test Firestore connection
    }

//    private fun testDatabaseConnection() {
//        db.collection("rides")
//            .get()
//            .addOnSuccessListener { result ->
//                showSuccess("Database connected successfully! Found ${result.size()} rides.")
//            }
//            .addOnFailureListener { e ->
//                showError("Database connection failed: ${e.message}")
//            }
//    }

    private fun initializeViews(view: View) {
        vehicleTypeDropdown = view.findViewById(R.id.vehicleTypeDropdown)
        vehicleNumberEdit = view.findViewById(R.id.vehicleNumberEdit)
        startLocationEdit = view.findViewById(R.id.startLocationEdit)
        destinationEdit = view.findViewById(R.id.destinationEdit)
        dateEdit = view.findViewById(R.id.dateEdit)
        timeEdit = view.findViewById(R.id.timeEdit)
        seatsEdit = view.findViewById(R.id.seatsEdit)
        priceEdit = view.findViewById(R.id.priceEdit)
    }

    private fun setupVehicleTypeDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vehicleTypes)
        vehicleTypeDropdown.setAdapter(adapter)
    }

    private fun setupDateTimePickers() {
        dateEdit.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateEdit.setText(dateFormat.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        timeEdit.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                timeEdit.setText(timeFormat.format(calendar.time))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
    }

    private fun setupPublishButton() {
        view?.findViewById<View>(R.id.publishButton)?.setOnClickListener {
            if (validateInputs()) {
                publishRide()
            }
        }
    }

    private fun validateInputs(): Boolean {
        when {
            vehicleTypeDropdown.text.isNullOrBlank() -> {
                showError("Please select vehicle type")
                return false
            }
            vehicleNumberEdit.text.isNullOrBlank() -> {
                showError("Please enter vehicle number")
                return false
            }
            startLocationEdit.text.isNullOrBlank() -> {
                showError("Please enter starting point")
                return false
            }
            destinationEdit.text.isNullOrBlank() -> {
                showError("Please enter destination")
                return false
            }
            dateEdit.text.isNullOrBlank() -> {
                showError("Please select date")
                return false
            }
            timeEdit.text.isNullOrBlank() -> {
                showError("Please select time")
                return false
            }
            seatsEdit.text.isNullOrBlank() -> {
                showError("Please enter available seats")
                return false
            }
            priceEdit.text.isNullOrBlank() -> {
                showError("Please enter price per seat")
                return false
            }
        }
        return true
    }

    private fun publishRide() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showError("Please login to publish a ride")
            return
        }

        val ride = hashMapOf(
            "driverId" to currentUser.uid,
            "vehicleType" to vehicleTypeDropdown.text.toString(),
            "vehicleNumber" to vehicleNumberEdit.text.toString(),
            "startLocation" to startLocationEdit.text.toString(),
            "destination" to destinationEdit.text.toString(),
            "date" to dateEdit.text.toString(),
            "time" to timeEdit.text.toString(),
            "availableSeats" to seatsEdit.text.toString().toInt(),
            "pricePerSeat" to priceEdit.text.toString().toDouble(),
            "status" to "active",
            "createdAt" to FieldValue.serverTimestamp(),
            "passengerIds" to listOf<String>()
        )

        db.collection("rides")
            .add(ride)
            .addOnSuccessListener {
                showSuccess("Ride published successfully!")
                clearInputs()
            }
            .addOnFailureListener { e ->
                showError("Failed to publish ride: ${e.message}")
            }
    }

    private fun clearInputs() {
        vehicleTypeDropdown.text.clear()
        vehicleNumberEdit.text?.clear()
        startLocationEdit.text?.clear()
        destinationEdit.text?.clear()
        dateEdit.text?.clear()
        timeEdit.text?.clear()
        seatsEdit.text?.clear()
        priceEdit.text?.clear()
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    // Add these functions to your OfferRideFragment or create a RideRepository class
    fun updateRideStatus(rideId: String, newStatus: RideStatus) {
        db.collection("rides")
            .document(rideId)
            .update("status", newStatus.toMap())
            .addOnSuccessListener {
                showSuccess("Ride status updated successfully")
            }
            .addOnFailureListener { e ->
                showError("Failed to update ride status: ${e.message}")
            }
    }

    fun cancelRide(rideId: String, reason: String) {
        updateRideStatus(rideId, RideStatus.Cancelled(reason))
    }

    fun startRide(rideId: String) {
        updateRideStatus(rideId, RideStatus.InProgress(Date()))
    }

    fun completeRide(rideId: String) {
        updateRideStatus(rideId, RideStatus.Completed(Date()))
    }
}