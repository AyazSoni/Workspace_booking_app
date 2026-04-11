package com.example.workspace_booking_app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.workspace_booking_app.firebase.FirebaseBookingRepo
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class BookingActivity : AppCompatActivity() {

    private lateinit var tvSelectedDate: TextView
    private lateinit var btnSelectDate: MaterialButton
    private lateinit var spinnerStartTime: Spinner
    private lateinit var spinnerEndTime: Spinner
    private lateinit var tvDuration: TextView
    private lateinit var etPurpose: TextInputEditText
    private lateinit var btnSave: MaterialButton

    private var roomId: String = ""
    private var roomName: String = ""
    private var selectedDate: String = ""
    private var selectedStartTime: String = ""
    private var selectedEndTime: String = ""

    private val timeSlots = mutableListOf<String>()
    private val bookingRepo = FirebaseBookingRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.booking)

        // Get room info from intent
        roomId = intent.getStringExtra("room_id") ?: ""
        roomName = intent.getStringExtra("room_name") ?: ""
        if (roomId.isEmpty()) {
            Toast.makeText(this, "Error: Room ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Check Firebase auth
        if (FirebaseAuth.getInstance().currentUser == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        spinnerStartTime = findViewById(R.id.spinnerStartTime)
        spinnerEndTime = findViewById(R.id.spinnerEndTime)
        tvDuration = findViewById(R.id.tvDuration)
        etPurpose = findViewById(R.id.etPurpose)
        btnSave = findViewById(R.id.btnSave)

        generateTimeSlots()
        setupDatePicker()
        setupTimeSpinners()

        btnSave.setOnClickListener {
            saveBooking()
        }
    }

    private fun generateTimeSlots() {
        timeSlots.clear()
        for (hour in 0..23) {
            for (minute in 0..59 step 30) {
                val timeString = String.format("%02d:%02d", hour, minute)
                timeSlots.add(timeString)
            }
        }
    }

    private fun setupDatePicker() {
        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    selectedDate = dateFormat.format(selectedCalendar.time)

                    val displayFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
                    val displayDate = displayFormat.format(selectedCalendar.time)

                    tvSelectedDate.text = displayDate
                    tvSelectedDate.setTextColor(resources.getColor(android.R.color.black, null))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }
    }

    private fun setupTimeSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerStartTime.adapter = adapter
        spinnerEndTime.adapter = adapter

        val defaultStartIndex = timeSlots.indexOf("09:00")
        val defaultEndIndex = timeSlots.indexOf("10:00")

        if (defaultStartIndex >= 0) spinnerStartTime.setSelection(defaultStartIndex)
        if (defaultEndIndex >= 0) spinnerEndTime.setSelection(defaultEndIndex)

        spinnerStartTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedStartTime = timeSlots[position]
                updateDuration()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerEndTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedEndTime = timeSlots[position]
                updateDuration()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateDuration() {
        if (selectedStartTime.isNotEmpty() && selectedEndTime.isNotEmpty()) {
            try {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val startTime = timeFormat.parse(selectedStartTime)
                val endTime = timeFormat.parse(selectedEndTime)

                if (startTime != null && endTime != null) {
                    val durationInMillis = endTime.time - startTime.time
                    val durationInHours = durationInMillis / (1000 * 60 * 60.0)

                    if (durationInHours > 0) {
                        val hours = durationInHours.toInt()
                        val minutes = ((durationInHours - hours) * 60).toInt()

                        val durationText = when {
                            hours > 0 && minutes > 0 -> "$hours hours $minutes minutes"
                            hours > 0 -> "$hours hours"
                            minutes > 0 -> "$minutes minutes"
                            else -> "0 hours"
                        }

                        tvDuration.text = durationText
                    } else {
                        tvDuration.text = "Invalid duration"
                    }
                }
            } catch (e: Exception) {
                tvDuration.text = "Error calculating duration"
            }
        }
    }

    private fun saveBooking() {
        val purpose = etPurpose.text.toString().trim()

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedStartTime.isEmpty()) {
            Toast.makeText(this, "Please select start time", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedEndTime.isEmpty()) {
            Toast.makeText(this, "Please select end time", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isEndTimeAfterStartTime(selectedStartTime, selectedEndTime)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            return
        }

        btnSave.isEnabled = false
        btnSave.text = "Booking..."

        bookingRepo.bookRoom(
            roomId = roomId,
            roomName = roomName,
            date = selectedDate,
            time = "$selectedStartTime - $selectedEndTime",
            onSuccess = {
                runOnUiThread {
                    Toast.makeText(this, "Booking created successfully!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("show_profile", true)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            },
            onFailure = { e ->
                runOnUiThread {
                    btnSave.isEnabled = true
                    btnSave.text = "Confirm Booking"
                    Toast.makeText(this, "Failed to create booking: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun isEndTimeAfterStartTime(startTime: String, endTime: String): Boolean {
        return try {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = timeFormat.parse(startTime)
            val end = timeFormat.parse(endTime)
            end?.after(start) ?: false
        } catch (e: Exception) {
            false
        }
    }
}
