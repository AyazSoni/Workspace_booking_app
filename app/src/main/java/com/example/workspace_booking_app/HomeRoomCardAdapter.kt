package com.example.workspace_booking_app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.workspace_booking_app.data.RoomPhotosRepo
import com.example.workspace_booking_app.data.RoomRepo
import com.example.workspace_booking_app.utils.ImageUtils
import android.util.Log

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
        
        // Set room information
        holder.title.text = room.name
        holder.roomTypeBadge.text = room.roomType
        holder.location.text = room.location.ifEmpty { "Location not specified" }
        holder.capacity.text = "${room.size} people"
        
        // Set feature badges visibility
        holder.computerBadge.visibility = if (room.hasComputer) View.VISIBLE else View.GONE
        holder.projectorBadge.visibility = if (room.hasProjector) View.VISIBLE else View.GONE
        
        // Load room photo (first photo)
        loadRoomPhoto(holder.cardImage, room.id.toInt())
        
        // Set click listener for the card
        holder.btnSeeDetails.setOnClickListener {
            onRoomCardClickListener?.invoke(room)
        }
    }

    override fun getItemCount() = rooms.size

    private fun loadRoomPhoto(imageView: ImageView, roomId: Int) {
        try {
            val roomPhotosRepo = RoomPhotosRepo(context)
            val photos = roomPhotosRepo.getAllPhotosByRoomId(roomId)
            
            Log.d("HomeRoomCardAdapter", "Loading photo for room $roomId, found ${photos.size} photos")
            
            if (photos.isNotEmpty()) {
                // Use the first photo
                val firstPhoto = photos.first()
                val photoPath = firstPhoto["photo_url"]
                
                Log.d("HomeRoomCardAdapter", "Photo path: $photoPath")
                
                if (!photoPath.isNullOrEmpty()) {
                    try {
                        ImageUtils.setImageFromPath(imageView, photoPath)
                        Log.d("HomeRoomCardAdapter", "Successfully loaded photo from path: $photoPath")
                    } catch (e: Exception) {
                        Log.e("HomeRoomCardAdapter", "Error loading photo from path: $photoPath", e)
                        // If loading from path fails, use default image
                        imageView.setImageResource(R.drawable.room1_p2)
                    }
                } else {
                    Log.d("HomeRoomCardAdapter", "No photo path, using default image")
                    // Set default image if no photo path
                    imageView.setImageResource(R.drawable.room1_p2)
                }
            } else {
                Log.d("HomeRoomCardAdapter", "No photos found, using default image")
                // Set default image if no photos
                imageView.setImageResource(R.drawable.room1_p2)
            }
        } catch (e: Exception) {
            Log.e("HomeRoomCardAdapter", "Error in loadRoomPhoto for room $roomId", e)
            // Set default image on error
            imageView.setImageResource(R.drawable.room1_p2)
        }
    }
}
