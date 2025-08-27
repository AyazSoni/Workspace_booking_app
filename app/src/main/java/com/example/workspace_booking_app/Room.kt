package com.example.workspace_booking_app

// Data class for Room
data class Room(
    val id: String,
    val name: String,
    val location: String,
    val size: Int,
    val roomType: String,
    val hasComputer: Boolean,
    val hasProjector: Boolean
)
