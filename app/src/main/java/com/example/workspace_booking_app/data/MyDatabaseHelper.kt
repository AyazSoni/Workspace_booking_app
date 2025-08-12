package com.example.workspace_booking_app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "${context.packageName}.db", null, 1) { // fresh DB version = 1

    override fun onCreate(db: SQLiteDatabase?) {
        val createUser = """
            CREATE TABLE users(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                created_at DATETIME DEFAULT current_timestamp,
                name TEXT,
                email TEXT UNIQUE,
                password TEXT,
                role TEXT DEFAULT 'user'
            )
        """.trimIndent()
        db?.execSQL(createUser)

        // Insert admin account
        val insertAdmin = """
            INSERT INTO users (name, email, password, role)
            VALUES ('Admin', 'admin9824@mailinator.com', 'Admin@2498', 'admin')
        """.trimIndent()
        db?.execSQL(insertAdmin)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }
}
