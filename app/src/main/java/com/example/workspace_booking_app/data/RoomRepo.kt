package com.example.workspace_booking_app.data

import android.content.Context

class RoomRepo(context: Context) {
    private val dbHelper = MyDatabaseHelper(context)
    private val crud = RoomCRUD(dbHelper)

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
        return crud.addRoom(name, roomType, size, hasComputer, hasProjector, address, description, workspaceId)
    }

    fun getAllRooms(): List<Map<String, String?>> {
        return crud.getAllRooms()
    }

    fun getRoomById(roomId: Int): Map<String, String?>? {
        return crud.getRoomById(roomId)
    }

    fun getRoomsByFilter(filterColumn: String, filterValue: String): List<Map<String, String?>> {
        return crud.getRoomsByFilter(filterColumn, filterValue)
    }

    fun updateRoom(column: String, newValue: String, roomId: Int): Int {
        return crud.updateRoom(column, newValue, roomId)
    }

    fun deleteRoom(roomId: Int): Int {
        return crud.deleteRoom(roomId)
    }

    fun getUniqueLocations(): List<String> {
        return crud.getUniqueLocations()
    }
}
