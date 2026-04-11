package com.example.workspace_booking_app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import coil.load

class HomeRoomCardAdapter(
    private val context: Context,
    private val rooms: List<Room>,
    private val onRoomCardClickListener: ((Room) -> Unit)? = null
) : RecyclerView.Adapter<HomeRoomCardAdapter.RoomCardViewHolder>() {

    class RoomCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardImage: ImageView = itemView.findViewById(R.id.cardImage)
        val title: TextView = itemView.findViewById(R.id.title)
        val roomTypeBadge: TextView = itemView.findViewById(R.id.roomTypeBadge)
        val location: TextView = itemView.findViewById(R.id.location)
        val capacity: TextView = itemView.findViewById(R.id.capacity)
        val computerBadge: ImageView = itemView.findViewById(R.id.computerBadge)
        val projectorBadge: ImageView = itemView.findViewById(R.id.ProjectorBadge)
        val btnSeeDetails: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.btnChangeBanner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.room_card_item, parent, false)
        return RoomCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomCardViewHolder, position: Int) {
        val room = rooms[position]

        holder.title.text = room.name
        holder.roomTypeBadge.text = room.roomType
        holder.location.text = room.location.ifEmpty { "Location not specified" }
        holder.capacity.text = "${room.size} people"

        holder.computerBadge.visibility = if (room.hasComputer) View.VISIBLE else View.GONE
        holder.projectorBadge.visibility = if (room.hasProjector) View.VISIBLE else View.GONE

        // Load first image from Supabase URLs using Coil
        if (room.imageUrls.isNotEmpty()) {
            holder.cardImage.load(room.imageUrls.first()) {
                placeholder(R.drawable.room1_p2)
                error(R.drawable.room1_p2)
            }
        } else {
            holder.cardImage.setImageResource(R.drawable.room1_p2)
        }

        holder.btnSeeDetails.setOnClickListener {
            onRoomCardClickListener?.invoke(room)
        }

        holder.itemView.setOnClickListener {
            onRoomCardClickListener?.invoke(room)
        }
    }

    override fun getItemCount() = rooms.size
}
