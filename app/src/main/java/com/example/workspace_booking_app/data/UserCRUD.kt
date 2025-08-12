package com.example.workspace_booking_app.data

import android.content.ContentValues

class UserCRUD(private val dbHelper: MyDatabaseHelper) {
    fun insertUser(name: String, email: String, password: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("password", password)
        }

        return try {
            db.insertOrThrow("users", null, values)
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            if (e.message?.contains("UNIQUE constraint failed: users.email") == true) {
                // Email already exists
                -2  // special code for duplicate email
            } else {
                -1  // other error
            }
        }
    }

    fun getRowByFilter(filterColumn: String, filterValue: String): Map<String, String?>? {
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM users WHERE $filterColumn = ?"
        val cursor = db.rawQuery(query, arrayOf(filterValue))

        var row: MutableMap<String, String?>? = null
        if (cursor.moveToFirst()) {
            row = mutableMapOf()
            for (i in 0 until cursor.columnCount) {
                val columnName = cursor.getColumnName(i)
                val value = cursor.getString(i) // everything as String
                row[columnName] = value
            }
        }

        cursor.close()
        return row
    }

}