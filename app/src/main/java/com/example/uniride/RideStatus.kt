package com.example.uniride

import java.util.Date

sealed class RideStatus {
    object Active : RideStatus()
    data class Booked(val passengerId: String) : RideStatus()
    data class InProgress(val startTime: Date) : RideStatus()
    data class Completed(val endTime: Date) : RideStatus()
    data class Cancelled(val reason: String) : RideStatus()

    // Convert RideStatus to String for Firestore
    fun toMap(): String {
        return when (this) {
            is Active -> "active"
            is Booked -> "booked"
            is InProgress -> "in_progress"
            is Completed -> "completed"
            is Cancelled -> "cancelled"
        }
    }

    companion object {
        // Convert String from Firestore to RideStatus
        fun fromString(status: String, metadata: Map<String, Any>? = null): RideStatus {
            return when (status) {
                "active" -> Active
                "booked" -> Booked(metadata?.get("passengerId") as String? ?: "")
                "in_progress" -> InProgress(metadata?.get("startTime") as Date? ?: Date())
                "completed" -> Completed(metadata?.get("endTime") as Date? ?: Date())
                "cancelled" -> Cancelled(metadata?.get("reason") as String? ?: "")
                else -> Active
            }
        }
    }
}