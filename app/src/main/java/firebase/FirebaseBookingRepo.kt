package com.example.workspace_booking_app.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseBookingRepo {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun bookRoom(
        roomId: String,
        date: String,
        time: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val user = auth.currentUser ?: return

        val booking = hashMapOf(

            "roomId" to roomId,
            "userId" to user.uid,
            "date" to date,
            "time" to time,
            "status" to "confirmed"
        )

        db.collection("bookings")
            .add(booking)
            .addOnSuccessListener {

                onSuccess()
            }
            .addOnFailureListener {

                onFailure(it)
            }
    }
}