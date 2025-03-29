package com.example.uniride

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RideHistoryAdapter(private val rides: List<Ride>) :
    RecyclerView.Adapter<RideHistoryAdapter.RideViewHolder>() {

    class RideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val startLocation: TextView = view.findViewById(R.id.startLocation)
        val destination: TextView = view.findViewById(R.id.destination)
        val dateTime: TextView = view.findViewById(R.id.dateTime)
        val status: TextView = view.findViewById(R.id.status)
        val price: TextView = view.findViewById(R.id.price)

        fun bind(ride: Ride) {
            startLocation.text = ride.startLocation
            destination.text = ride.destination
            dateTime.text = "${ride.date} ${ride.time}"
            status.text = ride.status
            price.text = "₹${ride.price}"

            // Set status background color based on status
            val statusColor = when (ride.status.toLowerCase()) {
                "completed" -> R.color.status_completed
                "cancelled" -> R.color.status_cancelled
                "ongoing" -> R.color.status_ongoing
                else -> R.color.status_pending
            }
            status.setBackgroundResource(statusColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_ride_history_adapter, parent, false)
        return RideViewHolder(view)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        holder.bind(rides[position])
    }

    override fun getItemCount() = rides.size
}