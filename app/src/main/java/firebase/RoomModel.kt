package com.example.workspace_booking_app.firebase

data class RoomModel(

    val id: String = "",
    val name: String = "",
    val location: String = "",
    val size: Int = 0,
    val roomType: String = "",
    val hasComputer: Boolean = false,
    val hasProjector: Boolean = false

)