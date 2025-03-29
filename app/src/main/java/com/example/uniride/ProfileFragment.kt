package com.example.uniride

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var profileImageView: ImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userRating: RatingBar
    private lateinit var universityId: TextView
    private lateinit var contactNumber: TextView
    private lateinit var address: TextView
    private lateinit var rideHistoryTabs: TabLayout
    private lateinit var rideHistoryRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeFirebase()
        initializeViews(view)
        setupEditButton()
        setupTabs()
        loadUserProfile()
        loadInitialRideHistory()
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    private fun initializeViews(view: View) {
        profileImageView = view.findViewById(R.id.profileImage)
        userName = view.findViewById(R.id.userName)
        userEmail = view.findViewById(R.id.userEmail)
        userRating = view.findViewById(R.id.userRating)
        universityId = view.findViewById(R.id.universityId)
        contactNumber = view.findViewById(R.id.contactNumber)
        address = view.findViewById(R.id.address)
        rideHistoryTabs = view.findViewById(R.id.rideHistoryTabs)
        rideHistoryRecyclerView = view.findViewById(R.id.rideHistoryRecyclerView)

        rideHistoryRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupEditButton() {
        view?.findViewById<ImageButton>(R.id.editProfileButton)?.setOnClickListener {
            startActivity(Intent(context, EditProfileActivity::class.java))
        }
    }

    private fun setupTabs() {
        rideHistoryTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadRidesTaken()
                    1 -> loadRidesOffered()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userName.text = document.getString("name") ?: "N/A"
                    userEmail.text = document.getString("email") ?: "N/A"
                    universityId.text = document.getString("universityId") ?: "N/A"
                    contactNumber.text = document.getString("contactNumber") ?: "N/A"
                    address.text = document.getString("address") ?: "N/A"
                    userRating.rating = document.getDouble("rating")?.toFloat() ?: 0f

                    // Try to load Base64 image first
                    document.getString("profileImageBase64")?.let { base64String ->
                        try {
                            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            profileImageView.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            // If Base64 fails, try loading from URL as fallback
                            loadProfileImageFromUrl(document.getString("profileImageUrl"))
                        }
                    } ?: run {
                        // If no Base64 image, try URL
                        loadProfileImageFromUrl(document.getString("profileImageUrl"))
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfileImageFromUrl(imageUrl: String?) {
        if (imageUrl != null && context != null) {
            profileImageView.setImageResource(R.drawable.default_profile)
        } else {
            profileImageView.setImageResource(R.drawable.default_profile)
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile() // Reload profile when returning to fragment
    }

    private fun loadInitialRideHistory() {
        loadRidesTaken()
    }

    private fun loadRidesTaken() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("rides")
            .whereEqualTo("passengerId", userId)
            .get()
            .addOnSuccessListener { documents ->
                updateRideHistoryList(documents.toObjects(Ride::class.java))
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading rides: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadRidesOffered() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("rides")
            .whereEqualTo("driverId", userId)
            .get()
            .addOnSuccessListener { documents ->
                updateRideHistoryList(documents.toObjects(Ride::class.java))
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading rides: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateRideHistoryList(rides: List<Ride>) {
        rideHistoryRecyclerView.adapter = RideHistoryAdapter(rides)
    }
}
// Data class for Ride
data class Ride(
    val id: String = "",
    val driverId: String = "",
    val passengerId: String? = null,
    val startLocation: String = "",
    val destination: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "",
    val price: Double = 0.0
)