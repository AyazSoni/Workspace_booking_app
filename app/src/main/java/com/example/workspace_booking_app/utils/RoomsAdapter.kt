package com.example.workspace_booking_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.workspace_booking_app.databinding.RoomItemBinding

class RoomsAdapter(
    private val rooms: List<String>
) : RecyclerView.Adapter<RoomsAdapter.RoomViewHolder>() {

    class RoomViewHolder(val binding: RoomItemBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {

        val binding = RoomItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {

        holder.binding.roomName.text = rooms[position]
    }

    override fun getItemCount(): Int {
        return rooms.size
    }
}