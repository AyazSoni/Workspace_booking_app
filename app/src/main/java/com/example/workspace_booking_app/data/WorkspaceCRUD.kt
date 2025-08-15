package com.example.workspace_booking_app.data

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import com.example.workspace_booking_app.R
import com.example.workspace_booking_app.utils.ImageUtils

class WorkspaceCRUD(private val context: Context, private val dbHelper: MyDatabaseHelper) {

    /**
     * Update workspace (name and/or banner)
     */
    fun updateWorkspace(id: Int = 1, newName: String? = null) {
        val values = ContentValues()

        if (!newName.isNullOrEmpty()) {
            values.put("name", newName)
        }
            val db = dbHelper.writableDatabase
            db.update("workspace", values, "id = ?", arrayOf(id.toString()))
            db.close()

    }

    /**
     * Get the first (and only) workspace as a map
     */
    fun getWorkspace(): Map<String, String?>? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM workspace LIMIT 1", null)
        var row: MutableMap<String, String?>? = null

        if (cursor.moveToFirst()) {
            row = mutableMapOf()
            for (i in 0 until cursor.columnCount) {
                val columnName = cursor.getColumnName(i)
                val value = cursor.getString(i)
                row[columnName] = value
            }
        }

        cursor.close()
        db.close()
        return row
    }
}
