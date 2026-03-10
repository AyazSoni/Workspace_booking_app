package com.example.workspace_booking_app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workspace_booking_app.databinding.BookingDetailsBinding

class BookingDetailsActivity : AppCompatActivity() {

    private lateinit var binding: BookingDetailsBinding
    private lateinit var adapter: ImageSliderAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

    // Auto scroll runnable
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            currentPage++
            binding.imageSlider.setCurrentItem(currentPage, true)
            handler.postDelayed(this, 3000) // change image every 3 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = BookingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val images = listOf(
            R.drawable.workspace_demo,
            R.drawable.workspace_demo2,
            R.drawable.workspace_demo3
        )

        adapter = ImageSliderAdapter(images)
        binding.imageSlider.adapter = adapter

        // Start from middle for infinite scrolling effect
        currentPage = Int.MAX_VALUE / 2
        binding.imageSlider.setCurrentItem(currentPage, false)

        // Start auto scroll
        handler.postDelayed(autoScrollRunnable, 3000)

        binding.bookNowBtn.setOnClickListener {
            Toast.makeText(this, "Booking Confirmed!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(autoScrollRunnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(autoScrollRunnable, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoScrollRunnable)
    }
}