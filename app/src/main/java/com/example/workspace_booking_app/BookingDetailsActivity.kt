package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.workspace_booking_app.firebase.FirebaseRoomRepo
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class BookingDetailsActivity : AppCompatActivity() {

    private val roomRepo = FirebaseRoomRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.booking_details)

        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val roomId = intent.getStringExtra("ROOM_ID") ?: run {
            Toast.makeText(this, "Room not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load room data from Firestore
        roomRepo.getRoomById(roomId,
            onSuccess = { room ->
                if (room == null) {
                    runOnUiThread {
                        Toast.makeText(this, "Room not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    return@getRoomById
                }

                runOnUiThread {
                    // Room name and type
                    findViewById<TextView>(R.id.roomName).text = room.name
                    findViewById<TextView>(R.id.roomType).text = room.roomType.replaceFirstChar { it.uppercase() }

                    // Capacity and location
                    findViewById<TextView>(R.id.roomSize).text = "${room.size}"
                    findViewById<TextView>(R.id.roomLocation).text = room.location.ifEmpty { "Not specified" }

                    // Description
                    findViewById<TextView>(R.id.roomDescription).text = room.description.ifEmpty {
                        "This is a well-equipped room perfect for meetings, presentations, and team collaborations."
                    }

                    // Features
                    val computerFeature = findViewById<MaterialCardView>(R.id.computerFeature)
                    val projectorFeature = findViewById<MaterialCardView>(R.id.projectorFeature)
                    computerFeature.visibility = if (room.hasComputer) View.VISIBLE else View.GONE
                    projectorFeature.visibility = if (room.hasProjector) View.VISIBLE else View.GONE

                    // Image slider
                    if (room.imageUrls.isNotEmpty()) {
                        val imageSlider = findViewById<ViewPager2>(R.id.imageSlider)
                        imageSlider.adapter = ImageSliderAdapter(room.imageUrls)
                    }

                    // Book Now button -> navigate to BookingActivity
                    findViewById<MaterialButton>(R.id.bookNowBtn).setOnClickListener {
                        val intent = Intent(this, BookingActivity::class.java)
                        intent.putExtra("room_id", roomId)
                        intent.putExtra("room_name", room.name)
                        startActivity(intent)
                    }
                }
            },
            onFailure = {
                runOnUiThread {
                    Toast.makeText(this, "Failed to load room", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        )
    }
}
