// AvailableRidesAdapter.kt
package com.example.uniride

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uniride.databinding.ItemAvailableRideBinding

class AvailableRidesAdapter(
    private val onBookClick: (AvailableRide) -> Unit
) : ListAdapter<AvailableRide, AvailableRidesAdapter.RideViewHolder>(RideDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val binding = ItemAvailableRideBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RideViewHolder(
        private val binding: ItemAvailableRideBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ride: AvailableRide) {
            binding.apply {
                startTimeText.text = ride.startTime
                durationText.text = ride.duration
                startLocationText.text = ride.startLocation
                destinationText.text = ride.destination
                priceText.text = "₹${ride.pricePerSeat}"
                driverNameText.text = ride.driverName
                ratingText.text = String.format("%.1f", ride.driverRating)

                // Load driver photo
                Glide.with(driverPhotoImage)
                    .load(ride.driverPhoto)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(driverPhotoImage)

                bookButton.setOnClickListener { onBookClick(ride) }
            }
        }
    }

    private class RideDiffCallback : DiffUtil.ItemCallback<AvailableRide>() {
        override fun areItemsTheSame(oldItem: AvailableRide, newItem: AvailableRide) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AvailableRide, newItem: AvailableRide) =
            oldItem == newItem
    }
}