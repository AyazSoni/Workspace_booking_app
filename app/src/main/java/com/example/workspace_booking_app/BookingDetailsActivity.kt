package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.workspace_booking_app.data.RoomPhotosRepo
import com.example.workspace_booking_app.utils.ImageUtils
import android.util.Log
import android.widget.ImageView
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import android.widget.ImageView as AndroidImageView

class BookingDetailsActivity : AppCompatActivity() {
    
    private lateinit var imageSlider: ViewPager2
    private lateinit var roomName: TextView
    private lateinit var roomType: TextView
    private lateinit var roomSize: TextView
    private lateinit var roomLocation: TextView
    private lateinit var roomDescription: TextView
    private var roomId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.booking_details)
        
        // Initialize views
        imageSlider = findViewById(R.id.imageSlider)
        roomName = findViewById(R.id.roomName)
        roomType = findViewById(R.id.roomType)
        roomSize = findViewById(R.id.roomSize)
        roomLocation = findViewById(R.id.roomLocation)
        roomDescription = findViewById(R.id.roomDescription)
        
        // Set up back button
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
        
        // Set up book now button
        val bookNowBtn = findViewById<MaterialButton>(R.id.bookNowBtn)
        bookNowBtn.setOnClickListener {
            val intent = Intent(this, BookingActivity::class.java)
            intent.putExtra("room_id", roomId.toInt())
            startActivity(intent)
        }
        
        // Get data passed from intent
        roomId = intent.getStringExtra("room_id") ?: ""
        val roomNameText = intent.getStringExtra("room_name") ?: ""
        val roomTypeText = intent.getStringExtra("room_type") ?: ""
        val roomSizeText = intent.getStringExtra("room_size") ?: ""
        val roomLocationText = intent.getStringExtra("room_location") ?: ""
        val roomDescriptionText = intent.getStringExtra("room_description") ?: ""
        val hasComputer = intent.getBooleanExtra("has_computer", false)
        val hasProjector = intent.getBooleanExtra("has_projector", false)
        
        Log.d("BookingDetailsActivity", "Received room data: id=$roomId, name=$roomNameText")
        
        // Set room information
        roomName.text = roomNameText
        roomType.text = roomTypeText
        roomSize.text = roomSizeText
        roomLocation.text = roomLocationText
        roomDescription.text = roomDescriptionText.ifEmpty { "This is a well-equipped room perfect for meetings, presentations, and team collaborations. The room features modern amenities and comfortable seating arrangements." }
        
        // Load room photos
        if (roomId.isNotEmpty()) {
            loadRoomPhotos(roomId.toInt())
        }
        
        // Update features section based on room capabilities
        updateFeaturesSection(hasComputer, hasProjector)
    }
    
    private fun loadRoomPhotos(roomId: Int) {
        try {
            val roomPhotosRepo = RoomPhotosRepo(this)
            val photos = roomPhotosRepo.getAllPhotosByRoomId(roomId)
            
            Log.d("BookingDetailsActivity", "Loading photos for room $roomId, found ${photos.size} photos")
            
            if (photos.isNotEmpty()) {
                // Create image adapter for ViewPager2
                val imageAdapter = RoomImageAdapter(photos.map { it["photo_url"] ?: "" })
                imageSlider.adapter = imageAdapter
            } else {
                // Show default image if no photos
                showDefaultImage()
            }
        } catch (e: Exception) {
            Log.e("BookingDetailsActivity", "Error loading room photos", e)
            showDefaultImage()
        }
    }
    
    private fun showDefaultImage() {
        // Create a simple adapter with default image
        val defaultImageAdapter = RoomImageAdapter(listOf(""))
        imageSlider.adapter = defaultImageAdapter
    }
    
    private fun updateFeaturesSection(hasComputer: Boolean, hasProjector: Boolean) {
        val computerFeature = findViewById<MaterialCardView>(R.id.computerFeature)
        val projectorFeature = findViewById<MaterialCardView>(R.id.projectorFeature)
        
        // Show computer feature if room has computer
        computerFeature.visibility = if (hasComputer) View.VISIBLE else View.GONE
        
        // Show projector feature if room has projector
        projectorFeature.visibility = if (hasProjector) View.VISIBLE else View.GONE
        
        Log.d("BookingDetailsActivity", "Features updated: Computer=$hasComputer, Projector=$hasProjector")
    }
    
    // Image adapter for ViewPager2
    private inner class RoomImageAdapter(private val imageUrls: List<String>) : 
        RecyclerView.Adapter<RoomImageAdapter.ImageViewHolder>() {
        
        inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: AndroidImageView = itemView.findViewById(R.id.sliderImage)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.slider_image_item, parent, false)
            return ImageViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val imageUrl = imageUrls[position]
            
            if (imageUrl.isNotEmpty()) {
                try {
                    ImageUtils.setImageFromPath(holder.imageView, imageUrl)
                } catch (e: Exception) {
                    Log.e("RoomImageAdapter", "Error loading image: $imageUrl", e)
                    holder.imageView.setImageResource(R.drawable.room1_p2)
                }
            } else {
                holder.imageView.setImageResource(R.drawable.room1_p2)
            }
        }
        
        override fun getItemCount() = imageUrls.size
    }
}
