package com.example.uniride

import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

data class Rides(
    val id: String = "",
    val driverId: String = "",
    val vehicleType: String = "",
    val vehicleNumber: String = "",
    val startLocation: String = "",
    val destination: String = "",
    val date: String = "",
    val time: String = "",
    val availableSeats: Int = 0,
    val pricePerSeat: Double = 0.0,
    val status: RideStatus = RideStatus.Active,
    val createdAt: Date = Date(),
    val passengerIds: List<String> = emptyList()
) {
    // Convert Ride object to HashMap for Firestore
    fun toMap(): HashMap<String, Any> {
        return hashMapOf(
            "driverId" to driverId,
            "vehicleType" to vehicleType,
            "vehicleNumber" to vehicleNumber,
            "startLocation" to startLocation,
            "destination" to destination,
            "date" to date,
            "time" to time,
            "availableSeats" to availableSeats,
            "pricePerSeat" to pricePerSeat,
            "status" to status.toMap(),
            "createdAt" to createdAt,
            "passengerIds" to passengerIds
        )
    }

    companion object {
        // Convert Firestore document to Ride object
        fun fromDocument(document: DocumentSnapshot): Rides {
            return Rides(
                id = document.id,
                driverId = document.getString("driverId") ?: "",
                vehicleType = document.getString("vehicleType") ?: "",
                vehicleNumber = document.getString("vehicleNumber") ?: "",
                startLocation = document.getString("startLocation") ?: "",
                destination = document.getString("destination") ?: "",
                date = document.getString("date") ?: "",
                time = document.getString("time") ?: "",
                availableSeats = document.getLong("availableSeats")?.toInt() ?: 0,
                pricePerSeat = document.getDouble("pricePerSeat") ?: 0.0,
                status = RideStatus.fromString(
                    document.getString("status") ?: "active",
                    document.get("statusMetadata") as? Map<String, Any>
                ),
                createdAt = document.getDate("createdAt") ?: Date(),
                passengerIds = (document.get("passengerIds") as? List<String>) ?: emptyList()
            )
        }
    }
}