package com.example.workspace_booking_app

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.recyclerview.widget.RecyclerView
import com.example.workspace_booking_app.firebase.FirebaseRoomRepo
import com.example.workspace_booking_app.supabase.SupabaseStorageHelper

class RoomAdapter(
    private val rooms: List<Room>,
    private val activity: Activity,
    private val editPickMedia: ActivityResultLauncher<PickVisualMediaRequest>,
    private val onEditDialogCreated: ((EditRoomDialog) -> Unit)? = null,
    private val onRoomUpdatedListener: (() -> Unit)? = null
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    private val firebaseRoomRepo = FirebaseRoomRepo()
    private val storageHelper = SupabaseStorageHelper()

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

        val computerContainer =
            holder.itemView.findViewById<View>(R.id.computer_indicator).parent as View
        val projectorContainer =
            holder.itemView.findViewById<View>(R.id.projector_indicator).parent as View

        computerContainer.visibility = if (room.hasComputer) View.VISIBLE else View.GONE
        projectorContainer.visibility = if (room.hasProjector) View.VISIBLE else View.GONE

        holder.btnEdit.setOnClickListener {
            val editDialog = EditRoomDialog(activity, room, editPickMedia, onRoomUpdatedListener)
            onEditDialogCreated?.invoke(editDialog)
            editDialog.show()
        }

        holder.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog(room)
        }

        holder.btnBookings.setOnClickListener {
            RoomBookingsDialog(activity, room.id, room.name).show()
        }
    }

    override fun getItemCount() = rooms.size

    private fun showDeleteConfirmationDialog(room: Room) {
        AlertDialog.Builder(activity)
            .setTitle("Delete Room")
            .setMessage("Are you sure you want to delete '${room.name}'?")
            .setPositiveButton("Delete") { _, _ -> deleteRoom(room) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .create()
            .show()
    }

    private fun deleteRoom(room: Room) {
        // Delete images from Supabase
        for (imageUrl in room.imageUrls) {
            storageHelper.deleteImage(imageUrl, onSuccess = {}, onFailure = {})
        }

        // Delete room from Firestore
        firebaseRoomRepo.deleteRoom(room.id,
            onSuccess = {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Room '${room.name}' deleted", Toast.LENGTH_SHORT).show()
                    onRoomUpdatedListener?.invoke()
                }
            },
            onFailure = {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Failed to delete room", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

// Room Data Class
data class Room(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val size: Int = 0,
    val roomType: String = "",
    val hasComputer: Boolean = false,
    val hasProjector: Boolean = false,
    val description: String = "",
    val imageUrls: List<String> = emptyList()
)
