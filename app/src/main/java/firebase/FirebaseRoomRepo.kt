package com.example.workspace_booking_app.firebase

import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRoomRepo {

    private val db = FirebaseFirestore.getInstance()

    fun addRoom(
        name: String,
        capacity: Int,
        price: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val room = hashMapOf(
            "name" to name,
            "capacity" to capacity,
            "price" to price
        )

        db.collection("rooms")
            .add(room)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun getRooms(
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        db.collection("rooms")
            .get()
            .addOnSuccessListener { result ->

                val rooms = mutableListOf<Map<String, Any>>()

                for (document in result) {
                    rooms.add(document.data)
                }

                onSuccess(rooms)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}