package com.example.workspace_booking_app.data

import android.content.Context

class BookingRepo(context: Context) {
    private val dbHelper = MyDatabaseHelper(context)
    private val bookingCRUD = BookingCRUD(dbHelper)

    fun createBooking(
        userId: Int,
        roomId: Int,
        date: String,
        startTime: String,
        endTime: String,
        purpose: String? = null
    ): Long {
        return bookingCRUD.createBooking(userId, roomId, date, startTime, endTime, purpose)
    }

    fun getAllBookings(): List<Map<String, String?>> {
        return bookingCRUD.getAllBookings()
    }

    fun updateBookingStatus(bookingId: Int, status: String): Int {
        return bookingCRUD.updateBookingStatus(bookingId, status)
    }

    fun getBookingsByUserId(userId: Int): List<Map<String, String?>> {
        return bookingCRUD.getBookingsByUserId(userId)
    }

    fun getBookingsByRoomId(roomId: Int): List<Map<String, String?>> {
        return bookingCRUD.getBookingsByRoomId(roomId)
    }

    fun getBookingById(bookingId: Int): Map<String, String?>? {
        return bookingCRUD.getBookingById(bookingId)
    }

    fun deleteBooking(bookingId: Int): Int {
        return bookingCRUD.deleteBooking(bookingId)
    }
}
