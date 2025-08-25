package com.example.workspace_booking_app.data

import android.content.Context

class RoomPhotosRepo(context: Context) {
    private val dbHelper = MyDatabaseHelper(context)
    private val crud = RoomPhotosCRUD(dbHelper)

    fun addPhoto(roomId: Int, photoUrl: String): Long {
        return crud.addPhoto(roomId, photoUrl)
    }

    fun getAllPhotosByRoomId(roomId: Int): List<Map<String, String?>> {
        return crud.getAllPhotosByRoomId(roomId)
    }

    fun getPhotosByFilter(filterColumn: String, filterValue: String): List<Map<String, String?>> {
        return crud.getPhotosByFilter(filterColumn, filterValue)
    }

    fun deletePhoto(photoId: Int): Int {
        return crud.deletePhoto(photoId)
    }
}