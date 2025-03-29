package com.example.uniride

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RideSearchCriteria(
    val pickupLocation: String,
    val dropLocation: String,
    val date: String,
    val passengers: Int
) : Parcelable

