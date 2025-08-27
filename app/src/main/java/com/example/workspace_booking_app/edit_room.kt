package com.example.workspace_booking_app

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workspace_booking_app.data.RoomPhotosRepo
import com.example.workspace_booking_app.data.RoomRepo
import com.example.workspace_booking_app.utils.ImageUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class EditRoomDialog(
    private val activity: Activity,
    private val room: Room,
    private val onRoomUpdatedListener: (() -> Unit)? = null
) {
    private lateinit var dialog: Dialog
    private val roomRepo = RoomRepo(activity)
    private val roomPhotosRepo = RoomPhotosRepo(activity)
    private val selectedImages = mutableListOf<Bitmap>()
    private val existingImages = mutableListOf<String>()
    private val removedImages = mutableListOf<String>() // Track removed existing images
    private val PICK_IMAGE_REQUEST = 1
    
    // Track original values for change detection
    private var originalRoomName = ""
    private var originalRoomType = ""
    private var originalRoomSize = ""
    private var originalRoomAddress = ""
    private var originalRoomDescription = ""
    private var originalHasComputer = false
    private var originalHasProjector = false

    fun show() {
        // Create dialog with same layout as add_room.xml
        dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.add_room)

        // Set dialog size to 95% of screen and make background transparent
        dialog.setOnShowListener {
            val width = (activity.resources.displayMetrics.widthPixels * 0.95).toInt()
            val height = (activity.resources.displayMetrics.heightPixels * 0.9).toInt()
            dialog.window?.setLayout(width, height)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        setupViews()
        loadRoomData()
        setupListeners()

        dialog.show()
    }

    private fun setupViews() {
        // Update title to "Edit Room"
        val titleView = dialog.findViewById<TextView>(R.id.textViewTitle)
        titleView.text = "Edit Room"

        // Setup room type dropdown
        val roomTypeDropdown = dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown)
        val roomTypes = arrayOf("Meeting", "Normal", "Chill")
        val adapter = ArrayAdapter(activity, R.layout.dropdown_item_room_type, roomTypes)
        roomTypeDropdown.setAdapter(adapter)

        // Setup image container
        val imagesContainer = dialog.findViewById<LinearLayout>(R.id.images_container)
        val imagesList = dialog.findViewById<LinearLayout>(R.id.images_list)
        val uploadButton = dialog.findViewById<MaterialButton>(R.id.upload_button)

        // Clear any existing images first
        imagesList.removeAllViews()
        hideImagesContainer()

        // Load existing images
        loadExistingImages(imagesList)
        if (existingImages.isNotEmpty()) {
            imagesContainer.visibility = View.VISIBLE
        }
    }

    private fun loadRoomData() {
        // Get room data from database
        val roomData = roomRepo.getRoomById(room.id.toInt())
        if (roomData != null) {
            // Fill the form with existing data
            val roomNameInput = dialog.findViewById<TextInputEditText>(R.id.room_name_input)
            val roomTypeDropdown = dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown)
            val roomSizeInput = dialog.findViewById<TextInputEditText>(R.id.room_size_input)
            val roomAddressInput = dialog.findViewById<TextInputEditText>(R.id.room_address_input)
            val roomDescriptionInput = dialog.findViewById<TextInputEditText>(R.id.room_description_input)
            val computerSwitch = dialog.findViewById<SwitchMaterial>(R.id.computer_switch)
            val projectorSwitch = dialog.findViewById<SwitchMaterial>(R.id.projector_switch)

            // Set current values
            val roomName = roomData["name"] ?: ""
            val roomType = (roomData["room_type"] ?: "").capitalize()
            val roomSize = roomData["size"] ?: ""
            val roomAddress = roomData["location"] ?: ""
            val roomDescription = roomData["description"] ?: ""
            val hasComputer = roomData["has_computer"] == "1"
            val hasProjector = roomData["has_projector"] == "1"

            roomNameInput.setText(roomName)
            roomTypeDropdown.setText(roomType)
            roomSizeInput.setText(roomSize)
            roomAddressInput.setText(roomAddress)
            roomDescriptionInput.setText(roomDescription)
            computerSwitch.isChecked = hasComputer
            projectorSwitch.isChecked = hasProjector

            // Store original values for change detection
            originalRoomName = roomName
            originalRoomType = roomType
            originalRoomSize = roomSize
            originalRoomAddress = roomAddress
            originalRoomDescription = roomDescription
            originalHasComputer = hasComputer
            originalHasProjector = hasProjector
        }
    }

    private fun loadExistingImages(imagesList: LinearLayout) {
        val roomPhotos = roomPhotosRepo.getAllPhotosByRoomId(room.id.toInt())
        
        for (photo in roomPhotos) {
            val photoPath = photo["photo_url"]
            if (photoPath != null) {
                existingImages.add(photoPath)
                addExistingImageToContainer(imagesList, photoPath)
            }
        }
    }

    private fun addExistingImageToContainer(imagesList: LinearLayout, imagePath: String) {
        val inflater = LayoutInflater.from(activity)
        val imageItemView = inflater.inflate(R.layout.image_item_simple, imagesList, false)
        
        val imageView = imageItemView.findViewById<ImageView>(R.id.image_preview)
        val removeButton = imageItemView.findViewById<ImageButton>(R.id.remove_button)
        
        // Load image from file
        try {
            val file = File(imagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    // Scale bitmap to ensure consistent size
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, true)
                    imageView.setImageBitmap(scaledBitmap)
                    if (scaledBitmap != bitmap) {
                        bitmap.recycle() // Free memory if we created a new bitmap
                    }
                }
            }
        } catch (e: Exception) {
            // Keep default background if image loading fails
        }
        
        // Set remove button click
        removeButton.setOnClickListener {
            removeExistingImage(imagePath, imageItemView)
        }
        
        imagesList.addView(imageItemView)
    }

    private fun removeExistingImage(imagePath: String, imageItemView: View) {
        val imagesList = dialog.findViewById<LinearLayout>(R.id.images_list)
        imagesList.removeView(imageItemView)
        existingImages.remove(imagePath)
        removedImages.add(imagePath) // Track this image for deletion
        
        if (existingImages.isEmpty() && selectedImages.isEmpty()) {
            dialog.findViewById<LinearLayout>(R.id.images_container).visibility = View.GONE
        }
        
        checkForChanges() // Trigger change detection
    }

    private fun setupListeners() {
        // Close button
        val closeButton = dialog.findViewById<ImageButton>(R.id.btnClose)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // Upload button
        val uploadButton = dialog.findViewById<MaterialButton>(R.id.upload_button)
        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activity.startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Save button
        val saveButton = dialog.findViewById<MaterialButton>(R.id.save_room_button)
        saveButton.text = "Update Room"
        saveButton.isEnabled = false // Initially disabled
        saveButton.setBackgroundColor(activity.getColor(android.R.color.darker_gray))
        saveButton.setOnClickListener {
            updateRoom()
        }

        // Add text change listeners for all input fields
        val roomNameInput = dialog.findViewById<TextInputEditText>(R.id.room_name_input)
        val roomTypeDropdown = dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown)
        val roomSizeInput = dialog.findViewById<TextInputEditText>(R.id.room_size_input)
        val roomAddressInput = dialog.findViewById<TextInputEditText>(R.id.room_address_input)
        val roomDescriptionInput = dialog.findViewById<TextInputEditText>(R.id.room_description_input)
        val computerSwitch = dialog.findViewById<SwitchMaterial>(R.id.computer_switch)
        val projectorSwitch = dialog.findViewById<SwitchMaterial>(R.id.projector_switch)

        // Add text change listeners
        roomNameInput.addTextChangedListener(createTextWatcher { checkForChanges() })
        roomTypeDropdown.addTextChangedListener(createTextWatcher { checkForChanges() })
        roomSizeInput.addTextChangedListener(createTextWatcher { checkForChanges() })
        roomAddressInput.addTextChangedListener(createTextWatcher { checkForChanges() })
        roomDescriptionInput.addTextChangedListener(createTextWatcher { checkForChanges() })

        // Add switch change listeners
        computerSwitch.setOnCheckedChangeListener { _, _ -> checkForChanges() }
        projectorSwitch.setOnCheckedChangeListener { _, _ -> checkForChanges() }
    }

    private fun updateRoom() {
        // Get all input values
        val roomNameInput = dialog.findViewById<TextInputEditText>(R.id.room_name_input)
        val roomTypeDropdown = dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown)
        val roomSizeInput = dialog.findViewById<TextInputEditText>(R.id.room_size_input)
        val roomAddressInput = dialog.findViewById<TextInputEditText>(R.id.room_address_input)
        val roomDescriptionInput = dialog.findViewById<TextInputEditText>(R.id.room_description_input)
        val computerSwitch = dialog.findViewById<SwitchMaterial>(R.id.computer_switch)
        val projectorSwitch = dialog.findViewById<SwitchMaterial>(R.id.projector_switch)

        val roomName = roomNameInput.text.toString().trim()
        val roomType = roomTypeDropdown.text.toString().trim()
        val roomSizeStr = roomSizeInput.text.toString().trim()
        val roomAddress = roomAddressInput.text.toString().trim()
        val roomDescription = roomDescriptionInput.text.toString().trim()
        val hasComputer = computerSwitch.isChecked
        val hasProjector = projectorSwitch.isChecked

        // Validate required fields
        if (roomName.isEmpty()) {
            showErrorToast("Please enter room name")
            return
        }
        if (roomType.isEmpty()) {
            showErrorToast("Please select room type")
            return
        }
        if (roomSizeStr.isEmpty()) {
            showErrorToast("Please enter room size")
            return
        }

        val roomSize = roomSizeStr.toIntOrNull()
        if (roomSize == null || roomSize <= 0) {
            showErrorToast("Please enter a valid room size")
            return
        }
        if (roomAddress.isEmpty()) {
            showErrorToast("Please enter room address")
            return
        }

        try {
            // Update room in database
            roomRepo.updateRoom("name", roomName, room.id.toInt())
            roomRepo.updateRoom("room_type", roomType.lowercase(), room.id.toInt())
            roomRepo.updateRoom("size", roomSize.toString(), room.id.toInt())
            roomRepo.updateRoom("location", roomAddress, room.id.toInt())
            roomRepo.updateRoom("description", roomDescription, room.id.toInt())
            roomRepo.updateRoom("has_computer", if (hasComputer) "1" else "0", room.id.toInt())
            roomRepo.updateRoom("has_projector", if (hasProjector) "1" else "0", room.id.toInt())

            // Delete removed images from database and storage
            for (imagePath in removedImages) {
                try {
                    // Delete from database
                    val roomPhotos = roomPhotosRepo.getAllPhotosByRoomId(room.id.toInt())
                    val photoToDelete = roomPhotos.find { it["photo_url"] == imagePath }
                    photoToDelete?.let { photo ->
                        val photoId = photo["id"]?.toIntOrNull()
                        if (photoId != null) {
                            roomPhotosRepo.deletePhoto(photoId)
                        }
                    }
                    
                    // Delete from storage
                    val file = File(imagePath)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    // Log error but continue with other operations
                    println("Error deleting image: ${e.message}")
                }
            }

            // Save new images to storage and database
            for (bitmap in selectedImages) {
                val imagePath = ImageUtils.saveImageWithUniqueName(activity, bitmap, "rooms")
                roomPhotosRepo.addPhoto(room.id.toInt(), imagePath)
            }

            showSuccessToast("Room updated successfully!")
            onRoomUpdatedListener?.invoke() // Notify that room was updated
            dialog.dismiss()

        } catch (e: Exception) {
            showErrorToast("Error updating room: ${e.message}")
        }
    }

    private fun addImage(bitmap: Bitmap) {
        selectedImages.add(bitmap)
        addImageToContainer(bitmap)
        showImagesContainer()
        checkForChanges() // Trigger change detection
    }

    private fun addImageToContainer(bitmap: Bitmap) {
        val imagesList = dialog.findViewById<LinearLayout>(R.id.images_list)
        val inflater = LayoutInflater.from(activity)
        val imageItemView = inflater.inflate(R.layout.image_item_simple, imagesList, false)
        
        val imageView = imageItemView.findViewById<ImageView>(R.id.image_preview)
        val removeButton = imageItemView.findViewById<ImageButton>(R.id.remove_button)
        
        // Set image directly from bitmap with consistent scaling
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, true)
        imageView.setImageBitmap(scaledBitmap)
        
        // Set remove button click
        removeButton.setOnClickListener {
            removeImage(bitmap, imageItemView)
        }
        
        imagesList.addView(imageItemView)
    }

    private fun removeImage(bitmap: Bitmap, imageItemView: View) {
        val imagesList = dialog.findViewById<LinearLayout>(R.id.images_list)
        imagesList.removeView(imageItemView)
        selectedImages.remove(bitmap)
        
        if (existingImages.isEmpty() && selectedImages.isEmpty()) {
            dialog.findViewById<LinearLayout>(R.id.images_container).visibility = View.GONE
        }
        
        checkForChanges() // Trigger change detection
    }

    private fun showImagesContainer() {
        val imagesContainer = dialog.findViewById<LinearLayout>(R.id.images_container)
        imagesContainer.visibility = View.VISIBLE
    }

    private fun hideImagesContainer() {
        val imagesContainer = dialog.findViewById<LinearLayout>(R.id.images_container)
        imagesContainer.visibility = View.GONE
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccessToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, imageUri)
                    addImage(bitmap)
                } catch (e: Exception) {
                    showErrorToast("Error loading image: ${e.message}")
                }
            }
        }
    }

    private fun createTextWatcher(onTextChanged: () -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onTextChanged()
            }
        }
    }

    private fun checkForChanges() {
        val roomNameInput = dialog.findViewById<TextInputEditText>(R.id.room_name_input)
        val roomTypeDropdown = dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown)
        val roomSizeInput = dialog.findViewById<TextInputEditText>(R.id.room_size_input)
        val roomAddressInput = dialog.findViewById<TextInputEditText>(R.id.room_address_input)
        val roomDescriptionInput = dialog.findViewById<TextInputEditText>(R.id.room_description_input)
        val computerSwitch = dialog.findViewById<SwitchMaterial>(R.id.computer_switch)
        val projectorSwitch = dialog.findViewById<SwitchMaterial>(R.id.projector_switch)
        val saveButton = dialog.findViewById<MaterialButton>(R.id.save_room_button)

        // Get current values
        val currentRoomName = roomNameInput.text.toString().trim()
        val currentRoomType = roomTypeDropdown.text.toString().trim()
        val currentRoomSize = roomSizeInput.text.toString().trim()
        val currentRoomAddress = roomAddressInput.text.toString().trim()
        val currentRoomDescription = roomDescriptionInput.text.toString().trim()
        val currentHasComputer = computerSwitch.isChecked
        val currentHasProjector = projectorSwitch.isChecked

        // Check if any values have changed
        val hasChanges = currentRoomName != originalRoomName ||
                currentRoomType != originalRoomType ||
                currentRoomSize != originalRoomSize ||
                currentRoomAddress != originalRoomAddress ||
                currentRoomDescription != originalRoomDescription ||
                currentHasComputer != originalHasComputer ||
                currentHasProjector != originalHasProjector ||
                selectedImages.isNotEmpty() ||
                removedImages.isNotEmpty()

        // Update button state
        if (hasChanges) {
            saveButton.isEnabled = true
            saveButton.setBackgroundColor(activity.getColor(android.R.color.black))
        } else {
            saveButton.isEnabled = false
            saveButton.setBackgroundColor(activity.getColor(android.R.color.darker_gray))
        }
    }
}