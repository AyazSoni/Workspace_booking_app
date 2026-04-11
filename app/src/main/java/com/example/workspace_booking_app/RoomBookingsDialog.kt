package com.example.workspace_booking_app

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import com.example.workspace_booking_app.firebase.FirebaseBookingRepo
import java.text.SimpleDateFormat
import java.util.*

class RoomBookingsDialog(
    private val activity: Activity,
    private val roomId: String,
    private val roomName: String
) {
    private val bookingRepo = FirebaseBookingRepo()

    fun show() {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.room_bookings_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (activity.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )

        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
        val tvRoomName = dialog.findViewById<TextView>(R.id.tvRoomName)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)
        val tvNoBookings = dialog.findViewById<TextView>(R.id.tvNoBookings)
        val bookingsScrollView = dialog.findViewById<ScrollView>(R.id.bookingsScrollView)
        val bookingsContainer = dialog.findViewById<LinearLayout>(R.id.bookingsContainer)

        dialogTitle.text = "Bookings"
        tvRoomName.text = roomName

        btnClose.setOnClickListener { dialog.dismiss() }

        bookingRepo.getBookingsForRoom(roomId,
            onSuccess = { bookings ->
                activity.runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (bookings.isEmpty()) {
                        tvNoBookings.visibility = View.VISIBLE
                    } else {
                        bookingsScrollView.visibility = View.VISIBLE
                        displayBookings(bookingsContainer, bookings)
                    }
                }
            },
            onFailure = {
                activity.runOnUiThread {
                    progressBar.visibility = View.GONE
                    tvNoBookings.text = "Failed to load bookings"
                    tvNoBookings.visibility = View.VISIBLE
                }
            }
        )

        dialog.show()
    }

    private fun displayBookings(container: LinearLayout, bookings: List<Map<String, Any?>>) {
        val inflater = LayoutInflater.from(activity)

        for (booking in bookings) {
            val itemView = inflater.inflate(R.layout.booking_item_admin, container, false)

            val userEmail = booking["userEmail"] as? String ?: "Unknown user"
            val date = booking["date"] as? String ?: ""
            val time = booking["time"] as? String ?: ""
            val status = booking["status"] as? String ?: "confirmed"

            itemView.findViewById<TextView>(R.id.tvUserEmail).text = userEmail
            itemView.findViewById<TextView>(R.id.tvDate).text = formatDate(date)
            itemView.findViewById<TextView>(R.id.tvTime).text = time

            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
            when (status.lowercase()) {
                "confirmed" -> {
                    tvStatus.text = "CONFIRMED"
                    tvStatus.setBackgroundResource(R.drawable.badge_background_upcoming)
                }
                "completed" -> {
                    tvStatus.text = "COMPLETED"
                    tvStatus.setBackgroundResource(R.drawable.badge_background_completed)
                }
                "cancelled" -> {
                    tvStatus.text = "CANCELLED"
                    tvStatus.setBackgroundResource(R.drawable.badge_background_cancelled)
                }
                else -> {
                    tvStatus.text = status.uppercase()
                    tvStatus.setBackgroundResource(R.drawable.badge_background_upcoming)
                }
            }

            container.addView(itemView)
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
}
