package com.example.workspace_booking_app.data

import android.content.ContentValues

class RoomPhotosCRUD(private val dbHelper: MyDatabaseHelper) {
    
    fun addPhoto(roomId: Int, photoUrl: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("room_id", roomId)
            put("photo_url", photoUrl)
        }

        return db.insertOrThrow("room_photos", null, values)
    }

    fun getAllPhotosByRoomId(roomId: Int): List<Map<String, String?>> {
        return getPhotosByFilter("room_id", roomId.toString())
    }

    fun getPhotosByFilter(filterColumn: String, filterValue: String): List<Map<String, String?>> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM room_photos WHERE $filterColumn = ?", arrayOf(filterValue))
        val photos = mutableListOf<Map<String, String?>>()

        while (cursor.moveToNext()) {
            val photo = mutableMapOf<String, String?>()
            for (i in 0 until cursor.columnCount) {
                val columnName = cursor.getColumnName(i)
                val value = cursor.getString(i)
                photo[columnName] = value
            }
            photos.add(photo)
        }

        cursor.close()
        return photos
    }

    fun deletePhoto(photoId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete("room_photos", "id = ?", arrayOf(photoId.toString()))
    }
}
