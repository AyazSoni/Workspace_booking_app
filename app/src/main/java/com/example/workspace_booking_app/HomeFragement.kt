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
import android.content.Intent
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.AutoCompleteTextView
import android.text.Editable

class HomeFragment : Fragment() {
    
    // Filter variables
    private var selectedRoomType: String = ""
    private var selectedLocation: String = ""
    private var minSize: Int = 0
    private var maxSize: Int = 0
    private var hasComputer: Boolean = false
    private var hasProjector: Boolean = false
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)
        
        // Set up filter button click listener
        val filterButton = view.findViewById<ImageButton>(R.id.filterButton)
        filterButton.setOnClickListener {
            Log.d("HomeFragment", "Filter button clicked!")
            Toast.makeText(requireContext(), "Filter button clicked!", Toast.LENGTH_SHORT).show()
            showFilterDialog()
        }
        
        // Set up search functionality
        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.trim() ?: ""
                Log.d("HomeFragment", "Search query: '$searchQuery'")
                
                // Apply search filter immediately
                val recyclerView = view.findViewById<RecyclerView>(R.id.roomsRecyclerView)
                applySearchAndFilters(recyclerView)
            }
        })
        
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
            applySearchAndFilters(rv)
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
            var roomsData = roomRepo.getAllRooms()
            
            Log.d("HomeFragment", "Loaded ${roomsData.size} rooms from database")
            
            if (roomsData.isEmpty()) {
                Log.d("HomeFragment", "No rooms found, showing no rooms message")
                // Show message when no rooms are available
                showNoRoomsMessage(recyclerView)
                return
            }
            
            // Apply search and filters
            applySearchAndFilters(recyclerView)
            
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
    
    private fun navigateToBookingDetails(room: Room) {
        try {
            val intent = Intent(requireContext(), BookingDetailsActivity::class.java).apply {
                putExtra("room_id", room.id)
                putExtra("room_name", room.name)
                putExtra("room_type", room.roomType)
                putExtra("room_size", room.size.toString())
                putExtra("room_location", room.location)
                putExtra("room_description", "") // We don't have description in our Room model yet
                putExtra("has_computer", room.hasComputer)
                putExtra("has_projector", room.hasProjector)
            }
            
            Log.d("HomeFragment", "Navigating to BookingDetailsActivity with room: ${room.name}")
            startActivity(intent)
            
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error navigating to booking details", e)
            Toast.makeText(requireContext(), "Error opening room details", Toast.LENGTH_SHORT).show()
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
        val roomTypeSpinner = dialog.findViewById<AutoCompleteTextView>(R.id.roomTypeSpinner)
        val locationSpinner = dialog.findViewById<AutoCompleteTextView>(R.id.locationSpinner)
        val minCapacityInput = dialog.findViewById<TextInputEditText>(R.id.minCapacityInput)
        val maxCapacityInput = dialog.findViewById<TextInputEditText>(R.id.maxCapacityInput)
        val computerToggle = dialog.findViewById<SwitchMaterial>(R.id.computerToggle)
        val projectorToggle = dialog.findViewById<SwitchMaterial>(R.id.projectorToggle)
        val btnReset = dialog.findViewById<MaterialButton>(R.id.btnReset)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)
        
        // Set up room type spinner with correct values from database
        val roomTypes = arrayOf("All", "meeting", "normal", "chill")
        val roomTypeAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item_room_type, roomTypes)
        roomTypeSpinner.setAdapter(roomTypeAdapter)
        
        // Set up location spinner with dynamic data from database
        val roomRepo = RoomRepo(requireContext())
        val uniqueLocations = roomRepo.getUniqueLocations()
        val locations = mutableListOf("All")
        locations.addAll(uniqueLocations)
        val locationAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item_room_type, locations)
        locationSpinner.setAdapter(locationAdapter)
        
        // Set current values
        if (selectedRoomType.isNotEmpty()) {
            val roomTypeIndex = roomTypes.indexOf(selectedRoomType)
            if (roomTypeIndex >= 0) roomTypeSpinner.setText(roomTypes[roomTypeIndex], false)
        } else {
            roomTypeSpinner.setText("All", false)
        }
        
        if (selectedLocation.isNotEmpty()) {
            val locationIndex = locations.indexOf(selectedLocation)
            if (locationIndex >= 0) locationSpinner.setText(locations[locationIndex], false)
        } else {
            locationSpinner.setText("All", false)
        }
        
        // Set capacity values
        minCapacityInput.setText(if (minSize > 0) minSize.toString() else "1")
        maxCapacityInput.setText(if (maxSize > 0) maxSize.toString() else "100")
        
        computerToggle.isChecked = hasComputer
        projectorToggle.isChecked = hasProjector
        
        // Set up click listeners
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        btnReset.setOnClickListener {
            // Reset all values
            roomTypeSpinner.setText("All", false)
            locationSpinner.setText("All", false)
            minCapacityInput.setText("1")
            maxCapacityInput.setText("100")
            computerToggle.isChecked = false
            projectorToggle.isChecked = false
            
            // Reset variables
            selectedRoomType = ""
            selectedLocation = ""
            minSize = 0
            maxSize = 0
            hasComputer = false
            hasProjector = false
            searchQuery = ""
            
            // Clear search text
            val searchEditText = view?.findViewById<EditText>(R.id.searchEditText)
            searchEditText?.setText("")
            
            // Refresh room list to show all rooms
            val recyclerView = view?.findViewById<RecyclerView>(R.id.roomsRecyclerView)
            recyclerView?.let { rv ->
                applySearchAndFilters(rv)
            }
        }
        
        btnSave.setOnClickListener {
            // Validate capacity inputs
            val minCapacity = minCapacityInput.text.toString().toIntOrNull() ?: 1
            val maxCapacity = maxCapacityInput.text.toString().toIntOrNull() ?: 100
            
            if (minCapacity < 1 || minCapacity > 100) {
                Toast.makeText(requireContext(), "Min capacity must be between 1 and 100", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (maxCapacity < 1 || maxCapacity > 100) {
                Toast.makeText(requireContext(), "Max capacity must be between 1 and 100", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (minCapacity > maxCapacity) {
                Toast.makeText(requireContext(), "Min capacity cannot be greater than max capacity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Save filter values
            selectedRoomType = if (roomTypeSpinner.text.toString() != "All") {
                roomTypeSpinner.text.toString()
            } else ""
            
            selectedLocation = if (locationSpinner.text.toString() != "All") {
                locationSpinner.text.toString()
            } else ""
            
            minSize = minCapacity
            maxSize = maxCapacity
            hasComputer = computerToggle.isChecked
            hasProjector = projectorToggle.isChecked
            
            // Apply filters
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
                append("Capacity: ")
                if (minSize > 0) append("$minSize")
                append(" - ")
                if (maxSize > 0) append("$maxSize")
                append(" people\n")
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
            applySearchAndFilters(rv)
        }
    }

    private fun applySearchAndFilters(recyclerView: RecyclerView) {
        try {
            val roomRepo = RoomRepo(requireContext())
            var roomsData = roomRepo.getAllRooms()
            
            Log.d("HomeFragment", "Applying search and filters to ${roomsData.size} rooms")
            Log.d("HomeFragment", "Search query: '$searchQuery'")
            
            // Apply search filter first
            if (searchQuery.isNotEmpty()) {
                roomsData = roomsData.filter { room ->
                    val name = room["name"]?.lowercase() ?: ""
                    val location = room["location"]?.lowercase() ?: ""
                    val query = searchQuery.lowercase()
                    
                    name.contains(query) || location.contains(query)
                }
                Log.d("HomeFragment", "After search filter: ${roomsData.size} rooms")
            }
            
            // Apply other filters
            if (selectedRoomType.isNotEmpty()) {
                roomsData = roomsData.filter { it["room_type"] == selectedRoomType }
                Log.d("HomeFragment", "After room type filter ($selectedRoomType): ${roomsData.size} rooms")
            }
            
            if (selectedLocation.isNotEmpty()) {
                roomsData = roomsData.filter { it["location"] == selectedLocation }
                Log.d("HomeFragment", "After location filter ($selectedLocation): ${roomsData.size} rooms")
            }
            
            if (minSize > 0 || maxSize > 0) {
                roomsData = roomsData.filter { room ->
                    val size = room["size"]?.toIntOrNull() ?: 0
                    val minCheck = minSize <= 0 || size >= minSize
                    val maxCheck = maxSize <= 0 || size <= maxSize
                    minCheck && maxCheck
                }
                Log.d("HomeFragment", "After capacity filter ($minSize-$maxSize): ${roomsData.size} rooms")
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
            recyclerView.adapter = HomeRoomCardAdapter(
                context = requireContext(),
                rooms = filteredRooms,
                onRoomCardClickListener = { room ->
                    navigateToBookingDetails(room)
                }
            )
            
            // Show message if no rooms match filters
            if (filteredRooms.isEmpty()) {
                showNoRoomsMessage(recyclerView)
            }
            
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error applying search and filters", e)
            showErrorMessage(recyclerView, "Error applying filters: ${e.message}")
        }
    }
}
