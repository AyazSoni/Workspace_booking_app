package com.example.workspace_booking_app

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workspace_booking_app.data.RoomRepo
import com.example.workspace_booking_app.data.WorkspaceRepo
import com.example.workspace_booking_app.utils.ImageUtils
import android.util.Log

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.home_fragment, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.roomsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        loadRooms(recyclerView)

        return view
    }

    private fun loadRooms(recyclerView: RecyclerView) {

        val roomRepo = RoomRepo(requireContext())
        val roomsData = roomRepo.getAllRooms()

        val rooms = roomsData.map {

            Room(
                id = it["id"] ?: "",
                name = it["name"] ?: "",
                location = it["location"] ?: "",
                size = it["size"]?.toIntOrNull() ?: 0,
                roomType = it["room_type"] ?: "",
                hasComputer = it["has_computer"] == "1",
                hasProjector = it["has_projector"] == "1"
            )

        }

        recyclerView.adapter = HomeRoomCardAdapter(
            context = requireContext(),
            rooms = rooms,

            onRoomCardClickListener = { room ->

                val intent = Intent(
                    requireContext(),
                    BookingDetailsActivity::class.java
                )

                intent.putExtra("ROOM_ID", room.id)
                intent.putExtra("ROOM_NAME", room.name)
                intent.putExtra("ROOM_LOCATION", room.location)
                intent.putExtra("ROOM_SIZE", room.size)
                intent.putExtra("ROOM_TYPE", room.roomType)

                startActivity(intent)
            }
        )
    }
}