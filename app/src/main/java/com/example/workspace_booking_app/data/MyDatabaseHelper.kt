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

        val createRooms = """
            CREATE TABLE rooms(
                id Integer PRIMARY KEY AUTOINCREMENT,
                created_at DATETIME DEFAULT current_timestamp,
                name TEXT NOT NULL,
                workspace_id INTEGER DEFAULT 1,
                room_type TEXT CHECK(room_type IN ('meeting','normal','chill')),
                location TEXT,
                size INTEGER,
                has_computer INTEGER DEFAULT 0 CHECK(has_computer IN (0,1)),
                has_projector INTEGER DEFAULT 0 CHECK(has_projector IN (0,1)),
                description TEXT,
                FOREIGN KEY(workspace_id) REFERENCES workspace(id) ON DELETE CASCADE
            )
        """.trimIndent()

        val roomPhotos = """
            CREATE TABLE room_photos (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            created_at DATETIME DEFAULT current_timestamp,
            room_id TEXT NOT NULL,
            photo_url TEXT NOT NULL,
            FOREIGN KEY(room_id) REFERENCES rooms(id) ON DELETE CASCADE
        )
        """.trimIndent()

        val createBookings = """
            CREATE TABLE bookings(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                room_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT NOT NULL,
                purpose TEXT,
                status TEXT DEFAULT 'upcoming' CHECK(status IN ('upcoming', 'completed', 'cancelled')),
                created_at DATETIME DEFAULT current_timestamp,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY(room_id) REFERENCES rooms(id) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createUser)
        db.execSQL(createWorkspace)
        db.execSQL(createRooms)
        db.execSQL(roomPhotos)
        db.execSQL(createBookings)




        // Pass the helper's context to seeder
        DatabaseSeeder.seedDefaults(context, db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS workspace")
        onCreate(db)
    }
}
