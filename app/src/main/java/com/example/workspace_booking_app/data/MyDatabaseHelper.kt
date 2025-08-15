package com.example.workspace_booking_app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.workspace_booking_app.DatabaseSeeder

class MyDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, "${context.packageName}.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
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

        val createWorkspace = """
            CREATE TABLE workspace(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                created_at DATETIME DEFAULT current_timestamp,
                name TEXT,
                banner_path TEXT
            )
        """.trimIndent()

        db.execSQL(createUser)
        db.execSQL(createWorkspace)

        // Pass the helper's context to seeder
        DatabaseSeeder.seedDefaults(context, db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS workspace")
        onCreate(db)
    }
}
