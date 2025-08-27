package com.example.workspace_booking_app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.workspace_booking_app.data.BookingRepo
import com.example.workspace_booking_app.utils.SessionManager
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
    
    private var roomId: Int = 0
    private var userId: Int = 0
    private var selectedDate: String = ""
    private var selectedStartTime: String = ""
    private var selectedEndTime: String = ""
    
    private val timeSlots = mutableListOf<String>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.booking)
        
        // Get room ID from intent
        roomId = intent.getIntExtra("room_id", 0)
        if (roomId == 0) {
            Toast.makeText(this, "Error: Room ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Get user ID from session
        val sessionManager = SessionManager(this)
        userId = sessionManager.getUserIdAsInt()
        if (userId == 0) {
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
        
        // Generate time slots (24-hour format)
        generateTimeSlots()
        
        // Set up date picker
        setupDatePicker()
        
        // Set up time spinners
        setupTimeSpinners()
        
        // Set up save button click listener
        btnSave.setOnClickListener {
            saveBooking()
        }
    }
    
    private fun generateTimeSlots() {
        timeSlots.clear()
        for (hour in 0..23) {
            for (minute in 0..59 step 30) { // 30-minute intervals
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
            
            // Set minimum date to today
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            
            datePickerDialog.show()
        }
    }
    
    private fun setupTimeSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        spinnerStartTime.adapter = adapter
        spinnerEndTime.adapter = adapter
        
        // Set default selections (9:00 AM and 10:00 AM)
        val defaultStartIndex = timeSlots.indexOf("09:00")
        val defaultEndIndex = timeSlots.indexOf("10:00")
        
        if (defaultStartIndex >= 0) spinnerStartTime.setSelection(defaultStartIndex)
        if (defaultEndIndex >= 0) spinnerEndTime.setSelection(defaultEndIndex)
        
        // Update duration when times change
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
        
        // Validate inputs
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
        
        // Validate that end time is after start time
        if (!isEndTimeAfterStartTime(selectedStartTime, selectedEndTime)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            // Create booking
            val bookingRepo = BookingRepo(this)
            val bookingId = bookingRepo.createBooking(
                userId = userId,
                roomId = roomId,
                date = selectedDate,
                startTime = "$selectedStartTime:00", // Add seconds
                endTime = "$selectedEndTime:00", // Add seconds
                purpose = if (purpose.isNotEmpty()) purpose else null
            )
            
            if (bookingId > 0) {
                Toast.makeText(this, "Booking created successfully!", Toast.LENGTH_LONG).show()
                // Redirect to profile page to show the new booking
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("show_profile", true)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to create booking", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
