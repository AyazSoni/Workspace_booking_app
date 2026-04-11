package com.example.workspace_booking_app.firebase

import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRoomRepo {

    private val db = FirebaseFirestore.getInstance()
    private val roomsCollection = db.collection("rooms")

    fun addRoom(
        name: String,
        roomType: String,
        size: Int,
        location: String,
        hasComputer: Boolean,
        hasProjector: Boolean,
        description: String,
        imageUrls: List<String>,
        workspaceId: String = "",
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val room = hashMapOf(
            "name" to name,
            "roomType" to roomType.lowercase(),
            "size" to size,
            "location" to location,
            "hasComputer" to hasComputer,
            "hasProjector" to hasProjector,
            "description" to description,
            "imageUrls" to imageUrls,
            "workspaceId" to workspaceId,
            "createdAt" to System.currentTimeMillis()
        )

        roomsCollection.add(room)
            .addOnSuccessListener { docRef ->
                onSuccess(docRef.id)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getRooms(
        onSuccess: (List<RoomModel>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        roomsCollection.get()
            .addOnSuccessListener { result ->
                val rooms = result.documents.map { doc ->
                    RoomModel(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        location = doc.getString("location") ?: "",
                        size = (doc.getLong("size") ?: 0).toInt(),
                        roomType = doc.getString("roomType") ?: "",
                        hasComputer = doc.getBoolean("hasComputer") ?: false,
                        hasProjector = doc.getBoolean("hasProjector") ?: false,
                        description = doc.getString("description") ?: "",
                        imageUrls = (doc.get("imageUrls") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        workspaceId = doc.getString("workspaceId") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }
                onSuccess(rooms)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getRoomById(
        roomId: String,
        onSuccess: (RoomModel?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        roomsCollection.document(roomId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val room = RoomModel(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        location = doc.getString("location") ?: "",
                        size = (doc.getLong("size") ?: 0).toInt(),
                        roomType = doc.getString("roomType") ?: "",
                        hasComputer = doc.getBoolean("hasComputer") ?: false,
                        hasProjector = doc.getBoolean("hasProjector") ?: false,
                        description = doc.getString("description") ?: "",
                        imageUrls = (doc.get("imageUrls") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        workspaceId = doc.getString("workspaceId") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                    onSuccess(room)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun updateRoom(
        roomId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        roomsCollection.document(roomId).update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun deleteRoom(
        roomId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        roomsCollection.document(roomId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
