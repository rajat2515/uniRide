package com.example.uniride
data class AvailableRide(
    val id: String = "",
    val driverId: String = "",
    val startLocation: String = "",
    val destination: String = "",
    val date: String = "",
    val startTime: String = "",
    val duration: String = "",
    val price: Double = 0.0,
    val pricePerSeat: Double = 0.0,
    val availableSeats: Int = 0,
    val driverName: String = "",
    val driverRating: Double = 0.0,
    val driverPhoto: String = ""
)