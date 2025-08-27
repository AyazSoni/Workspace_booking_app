package com.example.workspace_booking_app

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import android.view.MotionEvent
import android.widget.FrameLayout
import com.example.workspace_booking_app.data.RoomRepo
import com.example.workspace_booking_app.data.RoomPhotosRepo
import com.example.workspace_booking_app.data.WorkspaceRepo
import com.example.workspace_booking_app.Room
import com.example.workspace_booking_app.utils.ImageUtils
import android.util.Log

class HomeFragment : Fragment() {
    
    // Filter variables
    private var selectedRoomType: String = ""
    private var selectedLocation: String = ""
    private var minSize: Int = 0
    private var maxSize: Int = 0
    private var hasComputer: Boolean = false
    private var hasProjector: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)
        
        // Set up filter button click listener
        val filterButton = view.findViewById<ImageButton>(R.id.filterButton)
        filterButton.setOnClickListener {
            showFilterDialog()
        }
        
        // Set up workspace header
        setupWorkspaceHeader(view)
        
        // Set up room list
        setupRoomList(view)
        
        return view
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh room list when fragment becomes visible
        val recyclerView = view?.findViewById<RecyclerView>(R.id.roomsRecyclerView)
        recyclerView?.let { rv ->
            loadAndDisplayRooms(rv)
        }
        
        // Refresh workspace header when fragment becomes visible
        view?.let { setupWorkspaceHeader(it) }
    }
    
    private fun setupWorkspaceHeader(view: View) {
        try {
            val workspaceRepo = WorkspaceRepo(requireContext())
            val workspace = workspaceRepo.getWorkspace()
            
            Log.d("HomeFragment", "Setting up workspace header: $workspace")
            
            // Set workspace name
            val headerText = view.findViewById<TextView>(R.id.headerText)
            headerText.text = workspace?.get("name") ?: "Default Workspace"
            
            // Set workspace banner image
            val bannerPath = workspace?.get("banner_path")
            val headerImage = view.findViewById<ImageView>(R.id.headerImage)
            
            bannerPath?.let { path ->
                if (path.isNotEmpty()) {
                    try {
                        ImageUtils.setImageFromPath(headerImage, path)
                        Log.d("HomeFragment", "Successfully loaded workspace banner from: $path")
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Error loading workspace banner from: $path", e)
                        // Keep default image if loading fails
                    }
                } else {
                    Log.d("HomeFragment", "No banner path, using default image")
                    // Keep default image if no banner path
                }
            } ?: run {
                Log.d("HomeFragment", "No workspace banner data, using default image")
                // Keep default image if no workspace data
            }
            
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error setting up workspace header", e)
            // Keep default values if there's an error
        }
    }
    
    private fun setupRoomList(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.roomsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        loadAndDisplayRooms(recyclerView)
    }
    
    private fun loadAndDisplayRooms(recyclerView: RecyclerView) {
        try {
            // Get real data from database
            val roomRepo = RoomRepo(requireContext())
            val roomsData = roomRepo.getAllRooms()
            
            Log.d("HomeFragment", "Loaded ${roomsData.size} rooms from database")
            
            if (roomsData.isEmpty()) {
                Log.d("HomeFragment", "No rooms found, showing no rooms message")
                // Show message when no rooms are available
                showNoRoomsMessage(recyclerView)
                return
            }
            
            // Convert database data to Room objects
            val realRooms = roomsData.map { roomMap ->
                Room(
                    id = roomMap["id"] ?: "",
                    name = roomMap["name"] ?: "",
                    location = roomMap["location"] ?: "",
                    size = roomMap["size"]?.toIntOrNull() ?: 0,
                    roomType = roomMap["room_type"] ?: "",
                    hasComputer = roomMap["has_computer"] == "1",
                    hasProjector = roomMap["has_projector"] == "1"
                )
            }
            
            Log.d("HomeFragment", "Converted ${realRooms.size} rooms to Room objects")
            
            recyclerView.adapter = HomeRoomCardAdapter(
                context = requireContext(),
                rooms = realRooms,
                onRoomCardClickListener = { room ->
                    // Handle room card click - you can implement navigation or show details
                    // For now, just show a toast
                    Toast.makeText(requireContext(), "Clicked on ${room.name}", Toast.LENGTH_SHORT).show()
                }
            )
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error loading rooms", e)
            // Handle any database errors
            showErrorMessage(recyclerView, "Error loading rooms: ${e.message}")
        }
    }
    
    private fun showNoRoomsMessage(recyclerView: RecyclerView) {
        // Create a simple message view when no rooms are available
        val messageView = TextView(requireContext()).apply {
            text = "No rooms available at the moment.\nPlease check back later or contact administrator."
            textSize = 16f
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
        }
        
        // Create a FrameLayout to hold the message
        val frameLayout = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(messageView)
        }
        
        // Set the message view as the adapter
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return object : RecyclerView.ViewHolder(frameLayout) {}
            }
            
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
            
            override fun getItemCount() = 1
        }
    }
    
    private fun showErrorMessage(recyclerView: RecyclerView, message: String) {
        val messageView = TextView(requireContext()).apply {
            text = message
            textSize = 16f
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
        }
        
        val frameLayout = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(messageView)
        }
        
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return object : RecyclerView.ViewHolder(frameLayout) {}
            }
            
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
            
            override fun getItemCount() = 1
        }
    }
    
    private fun showFilterDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.filter_dialog)
        
        // Set dialog width to 90% of screen and make background transparent
        dialog.setOnShowListener {
            val width = (resources.displayMetrics.widthPixels * 0.97).toInt()
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        
        // Get dialog views
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val roomTypeSpinner = dialog.findViewById<Spinner>(R.id.roomTypeSpinner)
        val locationSpinner = dialog.findViewById<Spinner>(R.id.locationSpinner)
        val rangeSliderContainer = dialog.findViewById<FrameLayout>(R.id.rangeSliderContainer)
        val minPoint = dialog.findViewById<View>(R.id.minPoint)
        val maxPoint = dialog.findViewById<View>(R.id.maxPoint)
        val activeRangeLine = dialog.findViewById<View>(R.id.activeRangeLine)
        val minValueText = dialog.findViewById<TextView>(R.id.minValueText)
        val maxValueText = dialog.findViewById<TextView>(R.id.maxValueText)
        val computerToggle = dialog.findViewById<ToggleButton>(R.id.computerToggle)
        val projectorToggle = dialog.findViewById<ToggleButton>(R.id.projectorToggle)
        val btnReset = dialog.findViewById<MaterialButton>(R.id.btnReset)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)
        
        // Set up room type spinner
        val roomTypes = arrayOf("All", "Meeting Room", "Conference Room", "Office", "Studio", "Classroom")
        val roomTypeAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item_room_type, roomTypes)
        roomTypeSpinner.adapter = roomTypeAdapter
        
        // Set up location spinner
        val locations = arrayOf("All", "Floor 1", "Floor 2", "Floor 3", "Building A", "Building B", "Building C")
        val locationAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item_room_type, locations)
        locationSpinner.adapter = locationAdapter
        
        // Set up custom range slider
        var isDraggingMin = false
        var isDraggingMax = false
        var lastTouchX = 0f
        
        // Initialize range values
        var minValue = if (minSize > 0) minSize else 1
        var maxValue = if (maxSize > 0) maxSize else 100
        
        fun updateRangeSlider() {
            val containerWidth = rangeSliderContainer.width.toFloat()
            if (containerWidth > 0) {
                val minPosition = ((minValue - 1) / 99f) * (containerWidth - 16f)
                val maxPosition = ((maxValue - 1) / 99f) * (containerWidth - 16f)
                
                minPoint.translationX = minPosition
                maxPoint.translationX = maxPosition
                
                // Update active range line
                val activeLineParams = activeRangeLine.layoutParams
                activeLineParams.width = (maxPosition - minPosition).toInt()
                activeRangeLine.layoutParams = activeLineParams
                activeRangeLine.translationX = minPosition
                
                // Update text values (just numbers, no "people")
                minValueText.text = minValue.toString()
                maxValueText.text = maxValue.toString()
            }
        }
        
        // Touch listener for range slider
        rangeSliderContainer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.x
                    val minPointX = minPoint.x + minPoint.translationX + 8f // Center of min point
                    val maxPointX = maxPoint.x + maxPoint.translationX + 8f // Center of max point
                    
                    // Increase touch area to 60dp for easier dragging
                    if (Math.abs(event.x - minPointX) < 60) {
                        isDraggingMin = true
                        true
                    } else if (Math.abs(event.x - maxPointX) < 60) {
                        isDraggingMax = true
                        true
                    } else {
                        false
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDraggingMin || isDraggingMax) {
                        val containerWidth = rangeSliderContainer.width.toFloat()
                        val newPosition = event.x.coerceIn(0f, containerWidth - 16f)
                        val newValue = ((newPosition / (containerWidth - 16f)) * 99f + 1f).toInt()
                        
                        if (isDraggingMin) {
                            minValue = newValue.coerceAtMost(maxValue - 1).coerceAtLeast(1)
                        } else if (isDraggingMax) {
                            maxValue = newValue.coerceAtLeast(minValue + 1).coerceAtMost(100)
                        }
                        
                        updateRangeSlider()
                        true
                    } else {
                        false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    isDraggingMin = false
                    isDraggingMax = false
                    true
                }
                else -> false
            }
        }
        
        // Alternative: Add touch listeners directly to the points for better responsiveness
        minPoint.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDraggingMin = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDraggingMin) {
                        val containerWidth = rangeSliderContainer.width.toFloat()
                        val newPosition = (event.rawX - rangeSliderContainer.x).coerceIn(0f, containerWidth - 16f)
                        val newValue = ((newPosition / (containerWidth - 16f)) * 99f + 1f).toInt()
                        minValue = newValue.coerceAtMost(maxValue - 1).coerceAtLeast(1)
                        updateRangeSlider()
                        true
                    } else {
                        false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    isDraggingMin = false
                    true
                }
                else -> false
            }
        }
        
        maxPoint.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDraggingMax = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDraggingMax) {
                        val containerWidth = rangeSliderContainer.width.toFloat()
                        val newPosition = (event.rawX - rangeSliderContainer.x).coerceIn(0f, containerWidth - 16f)
                        val newValue = ((newPosition / (containerWidth - 16f)) * 99f + 1f).toInt()
                        maxValue = newValue.coerceAtLeast(minValue + 1).coerceAtMost(100)
                        updateRangeSlider()
                        true
                    } else {
                        false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    isDraggingMax = false
                    true
                }
                else -> false
            }
        }
        
        // Set current values
        if (selectedRoomType.isNotEmpty()) {
            val roomTypeIndex = roomTypes.indexOf(selectedRoomType)
            if (roomTypeIndex >= 0) roomTypeSpinner.setSelection(roomTypeIndex)
        }
        
        if (selectedLocation.isNotEmpty()) {
            val locationIndex = locations.indexOf(selectedLocation)
            if (locationIndex >= 0) locationSpinner.setSelection(locationIndex)
        }
        
        // Initialize range slider after layout with proper initial positions
        rangeSliderContainer.post {
            // Set initial positions: min at start (1), max at end (100)
            minValue = 1
            maxValue = 100
            updateRangeSlider()
        }
        
        computerToggle.isChecked = hasComputer
        projectorToggle.isChecked = hasProjector
        
        // Set up click listeners
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        btnReset.setOnClickListener {
            // Reset all values
            roomTypeSpinner.setSelection(0)
            locationSpinner.setSelection(0)
            minValue = 1
            maxValue = 100
            updateRangeSlider()
            computerToggle.isChecked = false
            projectorToggle.isChecked = false
            
            // Reset variables
            selectedRoomType = ""
            selectedLocation = ""
            minSize = 0
            maxSize = 0
            hasComputer = false
            hasProjector = false
            
            // Refresh room list to show all rooms
            val recyclerView = view?.findViewById<RecyclerView>(R.id.roomsRecyclerView)
            recyclerView?.let { rv ->
                loadAndDisplayRooms(rv)
            }
        }
        
        btnSave.setOnClickListener {
            // Save filter values
            selectedRoomType = if (roomTypeSpinner.selectedItemPosition > 0) {
                roomTypeSpinner.selectedItem.toString()
            } else ""
            
            selectedLocation = if (locationSpinner.selectedItemPosition > 0) {
                locationSpinner.selectedItem.toString()
            } else ""
            
            minSize = minValue
            maxSize = maxValue
            hasComputer = computerToggle.isChecked
            hasProjector = projectorToggle.isChecked
            
            // Apply filters (for now just show a toast, later will implement actual filtering)
            applyFilters()
            
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun applyFilters() {
        // Create filter summary
        val filterSummary = buildString {
            append("Applied Filters:\n")
            if (selectedRoomType.isNotEmpty()) append("Room Type: $selectedRoomType\n")
            if (selectedLocation.isNotEmpty()) append("Location: $selectedLocation\n")
            if (minSize > 0 || maxSize > 0) {
                append("Size: ")
                if (minSize > 0) append("$minSize")
                append(" - ")
                if (maxSize > 0) append("$maxSize")
                append("\n")
            }
            if (hasComputer) append("Computer: Yes\n")
            if (hasProjector) append("Projector: Yes\n")
        }
        
        // Show toast with filter summary
        Toast.makeText(requireContext(), filterSummary, Toast.LENGTH_LONG).show()
        
        // Apply filters to the room list
        applyFiltersToRoomList()
    }
    
    private fun applyFiltersToRoomList() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.roomsRecyclerView)
        recyclerView?.let { rv ->
            val roomRepo = RoomRepo(requireContext())
            var roomsData = roomRepo.getAllRooms()
            
            Log.d("HomeFragment", "Applying filters to ${roomsData.size} rooms")
            
            // Apply filters
            if (selectedRoomType.isNotEmpty()) {
                roomsData = roomsData.filter { it["room_type"] == selectedRoomType }
                Log.d("HomeFragment", "After room type filter: ${roomsData.size} rooms")
            }
            
            if (selectedLocation.isNotEmpty()) {
                roomsData = roomsData.filter { it["location"] == selectedLocation }
                Log.d("HomeFragment", "After location filter: ${roomsData.size} rooms")
            }
            
            if (minSize > 0 || maxSize > 0) {
                roomsData = roomsData.filter { room ->
                    val size = room["size"]?.toIntOrNull() ?: 0
                    (minSize <= 0 || size >= minSize) && (maxSize <= 0 || size <= maxSize)
                }
                Log.d("HomeFragment", "After size filter: ${roomsData.size} rooms")
            }
            
            if (hasComputer) {
                roomsData = roomsData.filter { it["has_computer"] == "1" }
                Log.d("HomeFragment", "After computer filter: ${roomsData.size} rooms")
            }
            
            if (hasProjector) {
                roomsData = roomsData.filter { it["has_projector"] == "1" }
                Log.d("HomeFragment", "After projector filter: ${roomsData.size} rooms")
            }
            
            // Convert filtered data to Room objects
            val filteredRooms = roomsData.map { roomMap ->
                Room(
                    id = roomMap["id"] ?: "",
                    name = roomMap["name"] ?: "",
                    location = roomMap["location"] ?: "",
                    size = roomMap["size"]?.toIntOrNull() ?: 0,
                    roomType = roomMap["room_type"] ?: "",
                    hasComputer = roomMap["has_computer"] == "1",
                    hasProjector = roomMap["has_projector"] == "1"
                )
            }
            
            Log.d("HomeFragment", "Final filtered rooms: ${filteredRooms.size}")
            
            // Update adapter with filtered data
            rv.adapter = HomeRoomCardAdapter(
                context = requireContext(),
                rooms = filteredRooms,
                onRoomCardClickListener = { room ->
                    Toast.makeText(requireContext(), "Clicked on ${room.name}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
