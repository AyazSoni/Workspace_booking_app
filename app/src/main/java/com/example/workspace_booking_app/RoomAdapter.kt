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
import com.google.firebase.firestore.FirebaseFirestore

class RoomAdapter(
    private val rooms: List<Room>,
    private val activity: Activity,
    private val onRoomUpdatedListener: (() -> Unit)? = null,
    private val onEditDialogCreated: ((EditRoomDialog) -> Unit)? = null
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

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
        holder.roomType.text = "${room.roomType.replaceFirstChar { it.uppercase() }} Room"

        val computerContainer =
            holder.itemView.findViewById<View>(R.id.computer_indicator).parent as View

        val projectorContainer =
            holder.itemView.findViewById<View>(R.id.projector_indicator).parent as View

        computerContainer.visibility =
            if (room.hasComputer) View.VISIBLE else View.GONE

        projectorContainer.visibility =
            if (room.hasProjector) View.VISIBLE else View.GONE

        // Edit button
        holder.btnEdit.setOnClickListener {

            val editDialog = EditRoomDialog(activity, room, onRoomUpdatedListener)

            onEditDialogCreated?.invoke(editDialog)

            editDialog.show()
        }

        // Delete button
        holder.btnDelete.setOnClickListener {

            showDeleteConfirmationDialog(room)
        }

        // Bookings button
        holder.btnBookings.setOnClickListener {

            Toast.makeText(
                activity,
                "Bookings feature coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int {
        return rooms.size
    }

    private fun showDeleteConfirmationDialog(room: Room) {

        val alertDialog = AlertDialog.Builder(activity)
            .setTitle("Delete Room")
            .setMessage("Are you sure you want to delete '${room.name}'?")
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

        db.collection("room")
            .document(room.id)
            .delete()
            .addOnSuccessListener {

                Toast.makeText(
                    activity,
                    "Room '${room.name}' deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()

                onRoomUpdatedListener?.invoke()
            }
            .addOnFailureListener {

                Toast.makeText(
                    activity,
                    "Failed to delete room",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
    val hasProjector: Boolean = false
)