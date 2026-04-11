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
import android.content.Intent
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.switchmaterial.SwitchMaterial
import android.text.Editable
import android.util.Log
import com.example.workspace_booking_app.firebase.FirebaseRoomRepo
import com.example.workspace_booking_app.firebase.FirebaseWorkspaceRepo
import coil.load

class HomeFragment : Fragment() {

    // Filter variables
    private var selectedRoomType: String = ""
    private var selectedLocation: String = ""
    private var minSize: Int = 0
    private var maxSize: Int = 0
    private var hasComputer: Boolean = false
    private var hasProjector: Boolean = false
    private var searchQuery: String = ""

    private val roomRepo = FirebaseRoomRepo()
    private val workspaceRepo = FirebaseWorkspaceRepo()

    // Cache all rooms loaded from Firestore
    private var allRooms: List<Room> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)

        val filterButton = view.findViewById<ImageButton>(R.id.filterButton)
        filterButton.setOnClickListener {
            showFilterDialog()
        }

        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.trim() ?: ""
                val recyclerView = view.findViewById<RecyclerView>(R.id.roomsRecyclerView)
                applySearchAndFilters(recyclerView)
            }
        })

        setupWorkspaceHeader(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.roomsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        loadRoomsFromFirestore(recyclerView)

        return view
    }

    override fun onResume() {
        super.onResume()
        view?.let { v ->
            setupWorkspaceHeader(v)
            val recyclerView = v.findViewById<RecyclerView>(R.id.roomsRecyclerView)
            loadRoomsFromFirestore(recyclerView)
        }
    }

    private fun setupWorkspaceHeader(view: View) {
        val headerText = view.findViewById<TextView>(R.id.headerText)
        val headerImage = view.findViewById<ImageView>(R.id.headerImage)

        workspaceRepo.getOrCreateDefaultWorkspace(
            onSuccess = { workspace ->
                activity?.runOnUiThread {
                    headerText.text = workspace["name"] as? String ?: "Default Workspace"
                    val bannerUrl = workspace["bannerUrl"] as? String
                    if (!bannerUrl.isNullOrEmpty()) {
                        headerImage.load(bannerUrl)
                    }
                }
            },
            onFailure = { }
        )
    }

    private fun loadRoomsFromFirestore(recyclerView: RecyclerView) {
        roomRepo.getRooms(
            onSuccess = { roomModels ->
                activity?.runOnUiThread {
                    allRooms = roomModels.map { rm ->
                        Room(
                            id = rm.id,
                            name = rm.name,
                            location = rm.location,
                            size = rm.size,
                            roomType = rm.roomType,
                            hasComputer = rm.hasComputer,
                            hasProjector = rm.hasProjector,
                            description = rm.description,
                            imageUrls = rm.imageUrls
                        )
                    }
                    applySearchAndFilters(recyclerView)
                }
            },
            onFailure = {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to load rooms", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun applySearchAndFilters(recyclerView: RecyclerView) {
        var filtered = allRooms

        if (searchQuery.isNotEmpty()) {
            val query = searchQuery.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(query) ||
                it.location.lowercase().contains(query)
            }
        }

        if (selectedRoomType.isNotEmpty()) {
            filtered = filtered.filter { it.roomType.equals(selectedRoomType, ignoreCase = true) }
        }

        if (selectedLocation.isNotEmpty()) {
            filtered = filtered.filter { it.location == selectedLocation }
        }

        if (minSize > 0 || maxSize > 0) {
            filtered = filtered.filter { room ->
                val minCheck = minSize <= 0 || room.size >= minSize
                val maxCheck = maxSize <= 0 || room.size <= maxSize
                minCheck && maxCheck
            }
        }

        if (hasComputer) {
            filtered = filtered.filter { it.hasComputer }
        }
        if (hasProjector) {
            filtered = filtered.filter { it.hasProjector }
        }

        if (filtered.isEmpty()) {
            showNoRoomsMessage(recyclerView)
        } else {
            recyclerView.adapter = HomeRoomCardAdapter(
                context = requireContext(),
                rooms = filtered,
                onRoomCardClickListener = { room ->
                    val intent = Intent(requireContext(), BookingDetailsActivity::class.java)
                    intent.putExtra("ROOM_ID", room.id)
                    startActivity(intent)
                }
            )
        }
    }

    private fun showNoRoomsMessage(recyclerView: RecyclerView) {
        val messageView = TextView(requireContext()).apply {
            text = "No rooms available at the moment.\nPlease check back later or contact administrator."
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

        dialog.setOnShowListener {
            val width = (resources.displayMetrics.widthPixels * 0.97).toInt()
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val roomTypeSpinner = dialog.findViewById<AutoCompleteTextView>(R.id.roomTypeSpinner)
        val locationSpinner = dialog.findViewById<AutoCompleteTextView>(R.id.locationSpinner)
        val minCapacityInput = dialog.findViewById<TextInputEditText>(R.id.minCapacityInput)
        val maxCapacityInput = dialog.findViewById<TextInputEditText>(R.id.maxCapacityInput)
        val computerToggle = dialog.findViewById<SwitchMaterial>(R.id.computerToggle)
        val projectorToggle = dialog.findViewById<SwitchMaterial>(R.id.projectorToggle)
        val btnReset = dialog.findViewById<MaterialButton>(R.id.btnReset)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)

        val roomTypes = arrayOf("All", "meeting", "normal", "chill")
        roomTypeSpinner.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_item_room_type, roomTypes))

        val uniqueLocations = allRooms.map { it.location }.filter { it.isNotEmpty() }.distinct()
        val locations = mutableListOf("All")
        locations.addAll(uniqueLocations)
        locationSpinner.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_item_room_type, locations))

        roomTypeSpinner.setText(if (selectedRoomType.isNotEmpty()) selectedRoomType else "All", false)
        locationSpinner.setText(if (selectedLocation.isNotEmpty()) selectedLocation else "All", false)
        minCapacityInput.setText(if (minSize > 0) minSize.toString() else "1")
        maxCapacityInput.setText(if (maxSize > 0) maxSize.toString() else "100")
        computerToggle.isChecked = hasComputer
        projectorToggle.isChecked = hasProjector

        btnClose.setOnClickListener { dialog.dismiss() }

        btnReset.setOnClickListener {
            roomTypeSpinner.setText("All", false)
            locationSpinner.setText("All", false)
            minCapacityInput.setText("1")
            maxCapacityInput.setText("100")
            computerToggle.isChecked = false
            projectorToggle.isChecked = false

            selectedRoomType = ""
            selectedLocation = ""
            minSize = 0
            maxSize = 0
            hasComputer = false
            hasProjector = false
            searchQuery = ""

            view?.findViewById<EditText>(R.id.searchEditText)?.setText("")
            view?.findViewById<RecyclerView>(R.id.roomsRecyclerView)?.let { applySearchAndFilters(it) }
        }

        btnSave.setOnClickListener {
            val minCapacity = minCapacityInput.text.toString().toIntOrNull() ?: 1
            val maxCapacity = maxCapacityInput.text.toString().toIntOrNull() ?: 100

            if (minCapacity > maxCapacity) {
                Toast.makeText(requireContext(), "Min capacity cannot be greater than max", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            selectedRoomType = if (roomTypeSpinner.text.toString() != "All") roomTypeSpinner.text.toString() else ""
            selectedLocation = if (locationSpinner.text.toString() != "All") locationSpinner.text.toString() else ""
            minSize = minCapacity
            maxSize = maxCapacity
            hasComputer = computerToggle.isChecked
            hasProjector = projectorToggle.isChecked

            view?.findViewById<RecyclerView>(R.id.roomsRecyclerView)?.let { applySearchAndFilters(it) }
            dialog.dismiss()
        }

        dialog.show()
    }
}
