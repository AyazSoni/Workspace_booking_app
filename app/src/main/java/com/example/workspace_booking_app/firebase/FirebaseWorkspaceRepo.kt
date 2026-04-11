package com.example.workspace_booking_app.firebase

import com.google.firebase.firestore.FirebaseFirestore

class FirebaseWorkspaceRepo {

    private val db = FirebaseFirestore.getInstance()
    private val workspacesCollection = db.collection("workspaces")

    fun getOrCreateDefaultWorkspace(
        onSuccess: (Map<String, Any?>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        workspacesCollection.document("default").get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val workspace = mapOf(
                        "id" to doc.id,
                        "name" to (doc.getString("name") ?: "Default Workspace"),
                        "bannerUrl" to (doc.getString("bannerUrl") ?: "")
                    )
                    onSuccess(workspace)
                } else {
                    // Create default workspace
                    val defaultWorkspace = hashMapOf(
                        "name" to "Default Workspace",
                        "bannerUrl" to "",
                        "createdAt" to System.currentTimeMillis()
                    )
                    workspacesCollection.document("default").set(defaultWorkspace)
                        .addOnSuccessListener {
                            onSuccess(mapOf(
                                "id" to "default",
                                "name" to "Default Workspace",
                                "bannerUrl" to ""
                            ))
                        }
                        .addOnFailureListener { e -> onFailure(e) }
                }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun updateWorkspace(
        name: String,
        bannerUrl: String? = null,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val updates = mutableMapOf<String, Any>("name" to name)
        if (bannerUrl != null) {
            updates["bannerUrl"] = bannerUrl
        }

        workspacesCollection.document("default").update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
