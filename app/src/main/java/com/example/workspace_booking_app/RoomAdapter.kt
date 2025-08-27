package com.example.workspace_booking_app

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.workspace_booking_app.data.RoomPhotosRepo
import com.example.workspace_booking_app.data.RoomRepo
import java.io.File

class RoomAdapter(
    private val rooms: List<Room>,
    private val activity: Activity,
    private val onRoomUpdatedListener: (() -> Unit)? = null,
    private val onEditDialogCreated: ((EditRoomDialog) -> Unit)? = null
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

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

        // Set up edit button click listener
        holder.btnEdit.setOnClickListener {
            val editDialog = EditRoomDialog(activity, room, onRoomUpdatedListener)
            onEditDialogCreated?.invoke(editDialog)
            editDialog.show()
        }

        // Set up delete button click listener
        holder.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog(room)
        }

        // Set up bookings button click listener
        holder.btnBookings.setOnClickListener {
            // TODO: Implement bookings functionality
        }
    }

    override fun getItemCount() = rooms.size

    private fun showDeleteConfirmationDialog(room: Room) {
        val alertDialog = AlertDialog.Builder(activity)
            .setTitle("Delete Room")
            .setMessage("Are you sure you want to delete '${room.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteRoom(room)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .create()

        alertDialog.show()
    }

    private fun deleteRoom(room: Room) {
        try {
            val roomRepo = RoomRepo(activity)
            val roomPhotosRepo = RoomPhotosRepo(activity)

            // Delete room photos from database and storage
            val roomPhotos = roomPhotosRepo.getAllPhotosByRoomId(room.id.toInt())
            for (photo in roomPhotos) {
                val photoId = photo["id"]?.toIntOrNull()
                if (photoId != null) {
                    roomPhotosRepo.deletePhoto(photoId)
                }

                // Delete photo file from storage
                val photoPath = photo["photo_url"]
                if (photoPath != null) {
                    val file = File(photoPath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }

            // Delete the room from database
            roomRepo.deleteRoom(room.id.toInt())

            // Show success message
            Toast.makeText(activity, "Room '${room.name}' deleted successfully", Toast.LENGTH_SHORT).show()

            // Notify that room list should be refreshed
            onRoomUpdatedListener?.invoke()

        } catch (e: Exception) {
            Toast.makeText(activity, "Error deleting room: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
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