package com.example.workspace_booking_app.firebase

import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRoomLoader {

    private val db = FirebaseFirestore.getInstance()

    fun getRooms(
        onSuccess: (List<RoomModel>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        db.collection("rooms")
            .get()
            .addOnSuccessListener { result ->

                val rooms = mutableListOf<RoomModel>()

                for (doc in result) {

                    val room = doc.toObject(RoomModel::class.java)
                    rooms.add(room)
                }

                onSuccess(rooms)
            }
            .addOnFailureListener {

                onFailure(it)
            }
    }
}