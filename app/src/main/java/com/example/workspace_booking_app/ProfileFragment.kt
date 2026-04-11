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
import com.example.workspace_booking_app.firebase.FirebaseUserRepo
import com.example.workspace_booking_app.firebase.FirebaseBookingRepo
import com.example.workspace_booking_app.firebase.FirebaseRoomRepo
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnLogout: MaterialButton
    private lateinit var bookingsContainer: LinearLayout
    private lateinit var tvNoBookings: TextView

    private val auth = FirebaseAuth.getInstance()
    private val userRepo = FirebaseUserRepo()
    private val bookingRepo = FirebaseBookingRepo()
    private val roomRepo = FirebaseRoomRepo()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile_fragment, container, false)

        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        btnLogout = view.findViewById(R.id.btnLogout)
        bookingsContainer = view.findViewById(R.id.bookingsContainer)
        tvNoBookings = view.findViewById(R.id.tvNoBookings)

        loadUserInfo()
        loadUserBookings()

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    private fun loadUserInfo() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            tvUserEmail.text = "Email: ${currentUser.email ?: "No email"}"

            userRepo.getUserProfile(currentUser.uid,
                onSuccess = { profile ->
                    activity?.runOnUiThread {
                        tvUserName.text = "Name: ${profile["name"] as? String ?: "User"}"
                    }
                },
                onFailure = {
                    activity?.runOnUiThread {
                        tvUserName.text = "Name: User"
                    }
                }
            )
        }
    }

    private fun loadUserBookings() {
        bookingRepo.getUserBookings(
            onSuccess = { bookings ->
                activity?.runOnUiThread {
                    if (bookings.isEmpty()) {
                        showNoBookings()
                    } else {
                        displayBookings(bookings)
                    }
                }
            },
            onFailure = {
                activity?.runOnUiThread {
                    showNoBookings()
                }
            }
        )
    }

    private fun showNoBookings() {
        bookingsContainer.removeAllViews()
        tvNoBookings.visibility = View.VISIBLE
    }

    private fun displayBookings(bookings: List<Map<String, Any?>>) {
        bookingsContainer.removeAllViews()
        tvNoBookings.visibility = View.GONE

        for (booking in bookings) {
            val bookingCard = createBookingCard(booking)
            bookingsContainer.addView(bookingCard)
        }
    }

    private fun createBookingCard(booking: Map<String, Any?>): View {
        val inflater = LayoutInflater.from(requireContext())
        val cardView = inflater.inflate(R.layout.booking_card_item, bookingsContainer, false)

        val roomName = booking["roomName"] as? String ?: "Unknown Room"
        val date = booking["date"] as? String ?: ""
        val time = booking["time"] as? String ?: ""
        val status = booking["status"] as? String ?: "confirmed"

        val tvRoomName = cardView.findViewById<TextView>(R.id.tvRoomName)
        tvRoomName.text = roomName

        val tvStatusBadge = cardView.findViewById<TextView>(R.id.tvStatusBadge)
        when (status.lowercase()) {
            "confirmed" -> {
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

        val tvBookingDate = cardView.findViewById<TextView>(R.id.tvBookingDate)
        tvBookingDate.text = formatDate(date)

        val tvBookingTime = cardView.findViewById<TextView>(R.id.tvBookingTime)
        tvBookingTime.text = time

        // Purpose container
        val purposeContainer = cardView.findViewById<LinearLayout>(R.id.purposeContainer)
        purposeContainer.visibility = View.GONE

        // Cancel button for upcoming bookings
        val btnCancelBooking = cardView.findViewById<MaterialButton>(R.id.btnCancelBooking)
        if (status.lowercase() == "confirmed") {
            btnCancelBooking.visibility = View.VISIBLE
            btnCancelBooking.setOnClickListener {
                val bookingId = booking["id"] as? String ?: return@setOnClickListener
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

    private fun showCancelConfirmationDialog(bookingId: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                cancelBooking(bookingId)
            }
            .setNegativeButton("No, Keep") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun cancelBooking(bookingId: String) {
        bookingRepo.updateBookingStatus(bookingId, "cancelled",
            onSuccess = {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Booking cancelled", Toast.LENGTH_SHORT).show()
                    loadUserBookings()
                }
            },
            onFailure = {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to cancel booking", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
