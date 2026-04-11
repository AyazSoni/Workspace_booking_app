package com.example.workspace_booking_app.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseUserRepo {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val auth = FirebaseAuth.getInstance()

    fun saveUserProfile(
        uid: String,
        name: String,
        email: String,
        role: String = "user",
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "role" to role,
            "createdAt" to System.currentTimeMillis()
        )

        usersCollection.document(uid).set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getUserRole(
        uid: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usersCollection.document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "user"
                onSuccess(role)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getUserProfile(
        uid: String,
        onSuccess: (Map<String, Any?>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usersCollection.document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val profile = mapOf(
                        "name" to doc.getString("name"),
                        "email" to doc.getString("email"),
                        "role" to doc.getString("role"),
                        "createdAt" to doc.getLong("createdAt")
                    )
                    onSuccess(profile)
                } else {
                    onSuccess(emptyMap())
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getCurrentUser() = auth.currentUser
}
