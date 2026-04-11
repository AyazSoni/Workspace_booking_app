package com.example.workspace_booking_app.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseBookingRepo {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val bookingsCollection = db.collection("bookings")

    fun bookRoom(
        roomId: String,
        roomName: String,
        date: String,
        time: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onFailure(Exception("User not logged in"))
            return
        }

        val booking = hashMapOf(
            "roomId" to roomId,
            "roomName" to roomName,
            "userId" to user.uid,
            "userEmail" to (user.email ?: ""),
            "date" to date,
            "time" to time,
            "status" to "confirmed",
            "createdAt" to System.currentTimeMillis()
        )

        bookingsCollection.add(booking)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getBookingsForRoom(
        roomId: String,
        onSuccess: (List<Map<String, Any?>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        bookingsCollection.whereEqualTo("roomId", roomId).get()
            .addOnSuccessListener { result ->
                val bookings = result.documents.map { doc ->
                    mapOf(
                        "id" to doc.id,
                        "roomId" to doc.getString("roomId"),
                        "roomName" to doc.getString("roomName"),
                        "userId" to doc.getString("userId"),
                        "userEmail" to doc.getString("userEmail"),
                        "date" to doc.getString("date"),
                        "time" to doc.getString("time"),
                        "status" to doc.getString("status")
                    )
                }
                onSuccess(bookings)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getUserBookings(
        onSuccess: (List<Map<String, Any?>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onFailure(Exception("User not logged in"))
            return
        }

        bookingsCollection.whereEqualTo("userId", user.uid).get()
            .addOnSuccessListener { result ->
                val bookings = result.documents.map { doc ->
                    mapOf(
                        "id" to doc.id,
                        "roomId" to doc.getString("roomId"),
                        "roomName" to doc.getString("roomName"),
                        "date" to doc.getString("date"),
                        "time" to doc.getString("time"),
                        "status" to doc.getString("status")
                    )
                }
                onSuccess(bookings)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun updateBookingStatus(
        bookingId: String,
        status: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        bookingsCollection.document(bookingId).update("status", status)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
