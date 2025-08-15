package com.example.workspace_booking_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RoomAdapter(private val rooms: List<Room>) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomName: TextView = itemView.findViewById(R.id.room_name)
        val roomLocation: TextView = itemView.findViewById(R.id.room_location)
        val roomCapacity: TextView = itemView.findViewById(R.id.room_capacity)
        val roomType: TextView = itemView.findViewById(R.id.room_type)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        val btnBookings: Button = itemView.findViewById(R.id.btn_bookings)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.room_list_item, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        holder.roomName.text = room.name
        holder.roomLocation.text = room.location
        holder.roomCapacity.text = "${room.size} people"
        holder.roomType.text = "${room.roomType.capitalize()} Room"
        
        // Set visibility for features
        val computerContainer = holder.itemView.findViewById<View>(R.id.computer_indicator).parent as View
        val projectorContainer = holder.itemView.findViewById<View>(R.id.projector_indicator).parent as View
        
        computerContainer.visibility = if (room.hasComputer) View.VISIBLE else View.GONE
        projectorContainer.visibility = if (room.hasProjector) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = rooms.size
}

// Data class for Room
data class Room(
    val id: String,
    val name: String,
    val location: String,
    val size: Int,
    val roomType: String,
    val hasComputer: Boolean,
    val hasProjector: Boolean
) 