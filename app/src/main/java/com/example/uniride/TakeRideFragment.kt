package com.example.uniride

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class TakeRideFragment : Fragment() {
    private lateinit var pickupLocationEdit: TextInputEditText
    private lateinit var dropLocationEdit: TextInputEditText
    private lateinit var dateEdit: TextInputEditText
    private lateinit var passengersEdit: TextInputEditText

    // Added TextInputLayouts for better error handling
    private lateinit var pickupLocationLayout: TextInputLayout
    private lateinit var dropLocationLayout: TextInputLayout
    private lateinit var dateLayout: TextInputLayout
    private lateinit var passengersLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Make sure this layout file exists in res/layout/
        return inflater.inflate(R.layout.fragment_take_ride, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize TextInputLayouts
        pickupLocationLayout = view.findViewById(R.id.pickupLocationLayout)
        dropLocationLayout = view.findViewById(R.id.dropLocationLayout)
        dateLayout = view.findViewById(R.id.dateLayout)
        passengersLayout = view.findViewById(R.id.passengersLayout)

        // Initialize EditTexts
        pickupLocationEdit = view.findViewById(R.id.pickupLocationEdit)
        dropLocationEdit = view.findViewById(R.id.dropLocationEdit)
        dateEdit = view.findViewById(R.id.dateEdit)
        passengersEdit = view.findViewById(R.id.passengersEdit)

        val searchButton: Button = view.findViewById(R.id.searchButton)

        setupDatePicker()

        searchButton.setOnClickListener {
            clearErrors()
            if (validateInputs()) {
                searchForRides()
            }
        }
    }

    private fun clearErrors() {
        pickupLocationLayout.error = null
        dropLocationLayout.error = null
        dateLayout.error = null
        passengersLayout.error = null
    }

    private fun setupDatePicker() {
        dateEdit.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateEdit.setText(dateFormat.format(calendar.time))
            }, year, month, day).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
                show()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        when {
            pickupLocationEdit.text.isNullOrBlank() -> {
                pickupLocationLayout.error = "Please enter pickup location"
                isValid = false
            }
            dropLocationEdit.text.isNullOrBlank() -> {
                dropLocationLayout.error = "Please enter drop-off location"
                isValid = false
            }
            dateEdit.text.isNullOrBlank() -> {
                dateLayout.error = "Please select a date"
                isValid = false
            }
            passengersEdit.text.isNullOrBlank() -> {
                passengersLayout.error = "Please enter number of passengers"
                isValid = false
            }
            else -> {
                val passengers = passengersEdit.text.toString().toIntOrNull()
                if (passengers == null || passengers < 1) {
                    passengersLayout.error = "Please enter a valid number of passengers"
                    isValid = false
                }
            }
        }

        return isValid
    }

    private fun searchForRides() {
        if (validateInputs()) {
            val searchCriteria = RideSearchCriteria(
                pickupLocation = pickupLocationEdit.text.toString(),
                dropLocation = dropLocationEdit.text.toString(),
                date = dateEdit.text.toString(),
                passengers = passengersEdit.text.toString().toIntOrNull() ?: 1
            )

            val intent = Intent(requireContext(), AvailableRidesActivity::class.java).apply {
                putExtra("searchCriteria", searchCriteria)
            }
            startActivity(intent)
        }
    }
}
