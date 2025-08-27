package com.example.workspace_booking_app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import com.example.workspace_booking_app.utils.ImageUtils

object DatabaseSeeder {

    fun seedDefaults(context: Context, db: SQLiteDatabase) {
        insertDefaultUsers(db)
        insertDefaultWorkspaces(context, db)
        insertDefaultRooms(context, db)
    }

    private fun insertDefaultUsers(db: SQLiteDatabase) {
        db.execSQL("""
            INSERT OR IGNORE INTO users (id, name, email, password, role)
            VALUES (1 ,'Admin', 'admin9824@mailinator.com', 'Admin@2498', 'admin')
        """.trimIndent())
    }

    private fun insertDefaultWorkspaces(context: Context, db: SQLiteDatabase) {
        // 1️⃣ Convert drawable to bitmap
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.banner)

        // 2️⃣ Save to internal storage
        val path = ImageUtils.saveBitmapToInternalStorage(context, bitmap, "banners")

        // 3️⃣ Insert into DB using the path string
        db.execSQL("""
            INSERT OR IGNORE INTO workspace (id, name, banner_path)
            VALUES (1, 'Default Workspace', '$path')
        """.trimIndent())
    }

    private fun insertDefaultRooms(context: Context, db: SQLiteDatabase) {
        // Insert Room 1: Executive Meeting Room
        db.execSQL("""
            INSERT OR IGNORE INTO rooms (id, name, room_type, location, size, has_computer, has_projector, description)
            VALUES (1, 'Executive Meeting Room', 'meeting', 'Floor 2, East Wing', 120, 1, 1, 'Premium meeting room with modern amenities, perfect for executive meetings and client presentations. Features ergonomic seating and advanced presentation equipment.')
        """.trimIndent())

        // Insert Room 2: Creative Studio
        db.execSQL("""
            INSERT OR IGNORE INTO rooms (id, name, room_type, location, size, has_computer, has_projector, description)
            VALUES (2, 'Creative Studio', 'chill', 'Floor 1, Creative Hub', 85, 1, 0, 'Relaxed creative space designed for brainstorming sessions and collaborative work. Features comfortable seating and natural lighting.')
        """.trimIndent())

        // Insert Room 1 Photos (3 images)
        insertRoomPhotos(context, db, 1, "room1_p1")
        insertRoomPhotos(context, db, 1, "room1_p2") 
        insertRoomPhotos(context, db, 1, "room1_p3")

        // Insert Room 2 Photos (3 images)
        insertRoomPhotos(context, db, 2, "room2_p1")
        insertRoomPhotos(context, db, 2, "room2_p2")
        insertRoomPhotos(context, db, 2, "room2_p3")
    }

    private fun insertRoomPhotos(context: Context, db: SQLiteDatabase, roomId: Int, imageName: String) {
        // Get drawable resource ID
        val resourceId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        if (resourceId == 0) {
            // Image not found - break/stop seeding
            throw RuntimeException("Image $imageName not found in drawable resources. Seeding stopped.")
        }
        
        try {
            // Convert drawable to bitmap
            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
            
            // Save to internal storage with unique name
            val path = ImageUtils.saveImageWithUniqueName(context, bitmap, "rooms")
            
            // Insert into database
            db.execSQL("""
                INSERT OR IGNORE INTO room_photos (room_id, photo_url)
                VALUES ($roomId, '$path')
            """.trimIndent())
            
        } catch (e: Exception) {
            // If any error occurs, break seeding
            throw RuntimeException("Error processing image $imageName: ${e.message}")
        }
    }
}
