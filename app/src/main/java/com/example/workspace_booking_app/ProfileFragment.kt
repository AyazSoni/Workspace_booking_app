package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.example.workspace_booking_app.utils.SessionManager
import com.example.workspace_booking_app.data.BookingRepo
import com.example.workspace_booking_app.data.RoomRepo
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {
    
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView

    private lateinit var btnLogout: MaterialButton
    private lateinit var bookingsContainer: LinearLayout
    private lateinit var tvNoBookings: TextView
    
    private lateinit var sessionManager: SessionManager
    private lateinit var bookingRepo: BookingRepo
    private lateinit var roomRepo: RoomRepo
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile_fragment, container, false)
        
        // Initialize managers and repos
        sessionManager = SessionManager(requireContext())
        bookingRepo = BookingRepo(requireContext())
        roomRepo = RoomRepo(requireContext())
        
        // Initialize views
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        btnLogout = view.findViewById(R.id.btnLogout)
        bookingsContainer = view.findViewById(R.id.bookingsContainer)
        tvNoBookings = view.findViewById(R.id.tvNoBookings)
        
        // Load user information
        loadUserInfo()
        
        // Load user bookings
        loadUserBookings()
        
        // Set up logout button
        btnLogout.setOnClickListener {
            logout()
        }
        
        return view
    }
    
    private fun loadUserInfo() {
        val userName = sessionManager.getUserName() ?: "Unknown"
        val userEmail = sessionManager.getUserEmail() ?: "No email"
        
        tvUserName.text = "Name: $userName"
        tvUserEmail.text = "Email: $userEmail"
    }
    
    private fun loadUserBookings() {
        val userId = sessionManager.getUserIdAsInt()
        if (userId == 0) {
            showNoBookings()
            return
        }
        
        try {
            val bookings = bookingRepo.getBookingsByUserId(userId)
            
            if (bookings.isEmpty()) {
                showNoBookings()
            } else {
                displayBookings(bookings)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error loading bookings: ${e.message}", Toast.LENGTH_SHORT).show()
            showNoBookings()
        }
    }
    
    private fun showNoBookings() {
        bookingsContainer.removeAllViews()
        tvNoBookings.visibility = View.VISIBLE
    }
    
    private fun displayBookings(bookings: List<Map<String, String?>>) {
        bookingsContainer.removeAllViews()
        tvNoBookings.visibility = View.GONE
        
        for (booking in bookings) {
            val bookingCard = createBookingCard(booking)
            bookingsContainer.addView(bookingCard)
        }
    }
    
    private fun createBookingCard(booking: Map<String, String?>): View {
        val inflater = LayoutInflater.from(requireContext())
        val cardView = inflater.inflate(R.layout.booking_card_item, bookingsContainer, false)
        
        // Get booking data
        val bookingId = booking["id"]?.toIntOrNull() ?: 0
        val roomId = booking["room_id"]?.toIntOrNull() ?: 0
        val date = booking["date"] ?: ""
        val startTime = booking["start_time"] ?: ""
        val endTime = booking["end_time"] ?: ""
        val purpose = booking["purpose"]
        val status = booking["status"] ?: "future"
        
        // Get room name
        val roomName = try {
            val room = roomRepo.getRoomById(roomId)
            room?.get("name") ?: "Unknown Room"
        } catch (e: Exception) {
            "Unknown Room"
        }
        
        // Set room name
        val tvRoomName = cardView.findViewById<TextView>(R.id.tvRoomName)
        tvRoomName.text = roomName
        
        // Set status badge
        val tvStatusBadge = cardView.findViewById<TextView>(R.id.tvStatusBadge)
        when (status.lowercase()) {
            "future" -> {
                tvStatusBadge.text = "UPCOMING"
                tvStatusBadge.setBackgroundResource(R.drawable.badge_background_upcoming)
            }
            "completed" -> {
                tvStatusBadge.text = "COMPLETED"
                tvStatusBadge.setBackgroundResource(R.drawable.badge_background_completed)
            }
            "cancelled" -> {
                tvStatusBadge.text = "CANCELLED"
                tvStatusBadge.setBackgroundResource(R.drawable.badge_background_cancelled)
            }
            else -> {
                tvStatusBadge.text = status.uppercase()
                tvStatusBadge.setBackgroundResource(R.drawable.badge_background_upcoming)
            }
        }
        
        // Format and set date
        val tvBookingDate = cardView.findViewById<TextView>(R.id.tvBookingDate)
        val formattedDate = formatDate(date)
        tvBookingDate.text = formattedDate
        
        // Format and set time
        val tvBookingTime = cardView.findViewById<TextView>(R.id.tvBookingTime)
        val formattedTime = formatTime(startTime, endTime)
        tvBookingTime.text = formattedTime
        
        // Set purpose if exists
        val purposeContainer = cardView.findViewById<LinearLayout>(R.id.purposeContainer)
        val tvBookingPurpose = cardView.findViewById<TextView>(R.id.tvBookingPurpose)
        if (!purpose.isNullOrEmpty()) {
            purposeContainer.visibility = View.VISIBLE
            tvBookingPurpose.text = purpose
        } else {
            purposeContainer.visibility = View.GONE
        }
        
        // Set up cancel button for upcoming bookings
        val btnCancelBooking = cardView.findViewById<MaterialButton>(R.id.btnCancelBooking)
        
        if (status.lowercase() == "upcoming") {
            btnCancelBooking.visibility = View.VISIBLE
            btnCancelBooking.setOnClickListener {
                showCancelConfirmationDialog(bookingId)
            }
        } else {
            btnCancelBooking.visibility = View.GONE
        }
        
        return cardView
    }
    
    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
    
    private fun formatTime(startTime: String, endTime: String): String {
        return try {
            val start = startTime.substring(0, 5) // Remove seconds
            val end = endTime.substring(0, 5) // Remove seconds
            "$start - $end"
        } catch (e: Exception) {
            "$startTime - $endTime"
        }
    }
    
    private fun showCancelConfirmationDialog(bookingId: Int) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                cancelBooking(bookingId)
            }
            .setNegativeButton("No, Keep") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        
        dialog.show()
    }
    
    private fun cancelBooking(bookingId: Int) {
        try {
            val success = bookingRepo.updateBookingStatus(bookingId, "cancelled")
            if (success > 0) {
                Toast.makeText(requireContext(), "Booking cancelled successfully", Toast.LENGTH_SHORT).show()
                // Reload bookings to reflect the change
                loadUserBookings()
            } else {
                Toast.makeText(requireContext(), "Failed to cancel booking", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error cancelling booking: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun logout() {
        sessionManager.logout()
        
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        
        // Redirect to login
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
