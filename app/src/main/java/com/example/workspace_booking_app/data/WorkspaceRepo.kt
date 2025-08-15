package com.example.workspace_booking_app.data

import android.content.Context

class WorkspaceRepo(context: Context) {
    private val dbHelper = MyDatabaseHelper(context)
    private val crud = WorkspaceCRUD(context, dbHelper)

    /**
     * Update workspace name and/or banner.
     * id defaults to 1 since there’s only one workspace.
     */
    fun updateWorkspace(newName: String? = null) {
        crud.updateWorkspace(id = 1, newName = newName)
    }

    /**
     * Get the first (and only) workspace as a Map<String, String?>
     */
    fun getWorkspace(): Map<String, String?>? {
        return crud.getWorkspace()
    }
}
