package com.example.workspace_booking_app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class BookingDetailsActivity : AppCompatActivity() {

    private lateinit var roomTitle: TextView
    private lateinit var roomLocation: TextView
    private lateinit var roomCapacity: TextView
    private lateinit var roomType: TextView
    private lateinit var bookButton: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_details)

        // Initialize Views
        roomTitle = findViewById(R.id.roomTitle)
        roomLocation = findViewById(R.id.roomLocation)
        roomCapacity = findViewById(R.id.roomCapacity)
        roomType = findViewById(R.id.roomType)
        bookButton = findViewById(R.id.bookButton)

        // Get data from Intent
        val roomId = intent.getStringExtra("ROOM_ID")
        val roomName = intent.getStringExtra("ROOM_NAME")
        val location = intent.getStringExtra("ROOM_LOCATION")
        val size = intent.getIntExtra("ROOM_SIZE", 0)
        val type = intent.getStringExtra("ROOM_TYPE")

        // Set room data to UI
        roomTitle.text = roomName
        roomLocation.text = location
        roomCapacity.text = "Capacity: $size"
        roomType.text = type

        // Booking button
        bookButton.setOnClickListener {

            val booking = hashMapOf(
                "roomId" to roomId,
                "roomName" to roomName,
                "location" to location,
                "capacity" to size,
                "type" to type,
                "date" to "2026-03-11",
                "time" to "10:00 AM",
                "status" to "confirmed"
            )

            db.collection("bookings")
                .add(booking)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Booking Confirmed!",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }
                .addOnFailureListener {

                    Toast.makeText(
                        this,
                        "Booking Failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}