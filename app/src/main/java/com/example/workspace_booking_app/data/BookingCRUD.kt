package com.example.workspace_booking_app.data

import android.content.ContentValues
import java.text.SimpleDateFormat
import java.util.*

class BookingCRUD(private val dbHelper: MyDatabaseHelper) {
    
    fun createBooking(
        userId: Int,
        roomId: Int,
        date: String,
        startTime: String,
        endTime: String,
        purpose: String? = null
    ): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("room_id", roomId)
            put("date", date)
            put("start_time", startTime)
            put("end_time", endTime)
            put("purpose", purpose)
            put("status", "upcoming")
        }

        return db.insertOrThrow("bookings", null, values)
    }

    fun getAllBookings(): List<Map<String, String?>> {
        // Update completed bookings automatically first
        updateCompletedBookings()
        
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM bookings ORDER BY date DESC, start_time DESC", null)
        val bookings = mutableListOf<Map<String, String?>>()

        while (cursor.moveToNext()) {
            val booking = mutableMapOf<String, String?>()
            for (i in 0 until cursor.columnCount) {
                val columnName = cursor.getColumnName(i)
                val value = cursor.getString(i)
                booking[columnName] = value
            }
            bookings.add(booking)
        }

        cursor.close()
        
        return bookings
    }

    fun updateBookingStatus(bookingId: Int, status: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("status", status)
        }
        
        return db.update("bookings", values, "id = ?", arrayOf(bookingId.toString()))
    }

    private fun updateCompletedBookings() {
        val db = dbHelper.writableDatabase
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        
        // Update bookings where date is in the past or date is today but end_time has passed
        val updateQuery = """
            UPDATE bookings 
            SET status = 'completed' 
            WHERE status = 'future' 
            AND (
                date < ? 
                OR (date = ? AND end_time < ?)
            )
        """.trimIndent()
        
        db.execSQL(updateQuery, arrayOf(currentDate, currentDate, currentTime))
    }

    fun getBookingsByUserId(userId: Int): List<Map<String, String?>> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM bookings WHERE user_id = ? ORDER BY date DESC, start_time DESC", arrayOf(userId.toString()))
        val bookings = mutableListOf<Map<String, String?>>()

        while (cursor.moveToNext()) {
            val booking = mutableMapOf<String, String?>()
            for (i in 0 until cursor.columnCount) {
                val columnName = cursor.getColumnName(i)
                val value = cursor.getString(i)
                booking[columnName] = value
            }
            bookings.add(booking)
        }

        cursor.close()
        return bookings
    }

    fun getBookingsByRoomId(roomId: Int): List<Map<String, String?>> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM bookings WHERE room_id = ? ORDER BY date DESC, start_time DESC", arrayOf(roomId.toString()))
        val bookings = mutableListOf<Map<String, String?>>()

        while (cursor.moveToNext()) {
            val booking = mutableMapOf<String, String?>()
            for (i in 0 until cursor.columnCount) {
                val columnName = cursor.getColumnName(i)
                val value = cursor.getString(i)
                booking[columnName] = value
            }
            bookings.add(booking)
        }

        cursor.close()
        return bookings
    }

    fun getBookingById(bookingId: Int): Map<String, String?>? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM bookings WHERE id = ?", arrayOf(bookingId.toString()))
        
        var booking: Map<String, String?>? = null
        if (cursor.moveToFirst()) {
            booking = mutableMapOf()
            for (i in 0 until cursor.columnCount) {
                val columnName = cursor.getColumnName(i)
                val value = cursor.getString(i)
                booking[columnName] = value
            }
        }
        
        cursor.close()
        return booking
    }

    fun deleteBooking(bookingId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete("bookings", "id = ?", arrayOf(bookingId.toString()))
    }
}
