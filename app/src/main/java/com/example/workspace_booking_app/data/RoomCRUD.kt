package com.example.workspace_booking_app.data

import android.content.ContentValues

class RoomCRUD(private val dbHelper: MyDatabaseHelper) {
    
    fun addRoom(
        name: String,
        roomType: String,
        size: Int,
        hasComputer: Boolean,
        hasProjector: Boolean,
        address: String? = null,
        description: String? = null,
        workspaceId: Int = 1
    ): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("room_type", roomType)
            put("size", size)
            put("has_computer", if (hasComputer) 1 else 0)
            put("has_projector", if (hasProjector) 1 else 0)
            put("location", address)
            put("description", description)
            put("workspace_id", workspaceId)
        }

        return db.insertOrThrow("rooms", null, values)
    }

    fun getAllRooms(): List<Map<String, String?>> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM rooms", null)
        val rooms = mutableListOf<Map<String, String?>>()

        while (cursor.moveToNext()) {
            val room = mutableMapOf<String, String?>()
            for (i in 0 until cursor.columnCount) {
                val columnName = cursor.getColumnName(i)
                val value = cursor.getString(i)
                room[columnName] = value
            }
            rooms.add(room)
        }

        cursor.close()
        return rooms
    }

    fun getRoomById(roomId: Int): Map<String, String?>? {
        return getRoomsByFilter("id", roomId.toString()).firstOrNull()
    }

    fun getRoomsByFilter(filterColumn: String, filterValue: String): List<Map<String, String?>> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM rooms WHERE $filterColumn = ?", arrayOf(filterValue))
        val rooms = mutableListOf<Map<String, String?>>()

        while (cursor.moveToNext()) {
            val room = mutableMapOf<String, String?>()
            for (i in 0 until cursor.columnCount) {
                val columnName = cursor.getColumnName(i)
                val value = cursor.getString(i)
                room[columnName] = value
            }
            rooms.add(room)
        }

        cursor.close()
        return rooms
    }

    fun updateRoom(column: String, newValue: String, roomId: Int): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(column, newValue)
        }
        
        return db.update("rooms", values, "id = ?", arrayOf(roomId.toString()))
    }

    fun deleteRoom(roomId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete("rooms", "id = ?", arrayOf(roomId.toString()))
    }

    fun getUniqueLocations(): List<String> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT location FROM rooms WHERE location IS NOT NULL AND location != '' ORDER BY location", null)
        val locations = mutableListOf<String>()

        while (cursor.moveToNext()) {
            val location = cursor.getString(0)
            if (location != null && location.isNotEmpty()) {
                locations.add(location)
            }
        }

        cursor.close()
        return locations
    }
}
