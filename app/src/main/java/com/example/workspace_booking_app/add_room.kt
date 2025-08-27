package com.example.workspace_booking_app

import android.app.Dialog
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import com.example.workspace_booking_app.data.RoomRepo
import com.example.workspace_booking_app.data.RoomPhotosRepo
import com.example.workspace_booking_app.utils.ImageUtils
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class Add_room(private val activity: AppCompatActivity) {
    
    private lateinit var dialog: Dialog
    private val selectedImages = mutableListOf<android.graphics.Bitmap>()
    private val roomRepo = RoomRepo(activity)
    private val roomPhotosRepo = RoomPhotosRepo(activity)
    private var onRoomAddedListener: (() -> Unit)? = null

    private val pickMedia = activity.registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(activity.contentResolver, uri))
            } else {
                android.provider.MediaStore.Images.Media.getBitmap(activity.contentResolver, uri)
            }
            addImage(bitmap)
        }
    }

    fun show() {
        // Clear previous images when dialog opens
        selectedImages.clear()
        
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
        dialog.show()
    }

    private fun setupViews() {
        // Clear images container
        val imagesList = dialog.findViewById<LinearLayout>(R.id.images_list)
        imagesList.removeAllViews()
        hideImagesContainer()
        
        // Clear all input fields
        val roomNameInput = dialog.findViewById<TextInputEditText>(R.id.room_name_input)
        val roomTypeDropdown = dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown)
        val roomSizeInput = dialog.findViewById<TextInputEditText>(R.id.room_size_input)
        val roomAddressInput = dialog.findViewById<TextInputEditText>(R.id.room_address_input)
        val roomDescriptionInput = dialog.findViewById<TextInputEditText>(R.id.room_description_input)
        val computerSwitch = dialog.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.computer_switch)
        val projectorSwitch = dialog.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.projector_switch)
        
        // Reset all inputs to default state
        roomNameInput.setText("")
        roomTypeDropdown.setText("")
        roomSizeInput.setText("")
        roomAddressInput.setText("")
        roomDescriptionInput.setText("")
        computerSwitch.isChecked = false
        projectorSwitch.isChecked = false
        
        val uploadButton = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.upload_button)
        val saveButton = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.save_room_button)
        val closeButton = dialog.findViewById<ImageButton>(R.id.btnClose)

        // Setup beautiful room type dropdown
        val roomTypes = arrayOf("Meeting", "Normal", "Chill")
        val adapter = ArrayAdapter(activity, R.layout.dropdown_item_room_type, roomTypes)
        roomTypeDropdown.setAdapter(adapter)

        // Setup toggle switches with beautiful animations
        computerSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle computer toggle
        }

        projectorSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle projector toggle
        }

        uploadButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        saveButton.setOnClickListener {
            // Handle save room functionality
            saveRoom()
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun saveRoom() {
        // Get all input values
        val roomNameInput = dialog.findViewById<TextInputEditText>(R.id.room_name_input)
        val roomTypeDropdown = dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown)
        val roomSizeInput = dialog.findViewById<TextInputEditText>(R.id.room_size_input)
        val roomAddressInput = dialog.findViewById<TextInputEditText>(R.id.room_address_input)
        val roomDescriptionInput = dialog.findViewById<TextInputEditText>(R.id.room_description_input)
        val computerSwitch = dialog.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.computer_switch)
        val projectorSwitch = dialog.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.projector_switch)

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
        if (selectedImages.isEmpty()) {
            showErrorToast("Please upload at least one photo")
            return
        }

        try {
            // Save room to database
            val roomId = roomRepo.addRoom(
                name = roomName,
                roomType = roomType.lowercase(),
                size = roomSize,
                hasComputer = hasComputer,
                hasProjector = hasProjector,
                address = roomAddress,
                description = if (roomDescription.isNotEmpty()) roomDescription else null
                // workspaceId defaults to 1
            )

            // Save images to storage and database
            for (bitmap in selectedImages) {
                val imagePath = ImageUtils.saveImageWithUniqueName(activity, bitmap, "rooms")
                roomPhotosRepo.addPhoto(roomId.toInt(), imagePath)
            }

            showSuccessToast("Room saved successfully!")
            onRoomAddedListener?.invoke() // Notify that room was added
            dialog.dismiss()

        } catch (e: Exception) {
            showErrorToast("Error saving room: ${e.message}")
        }
    }

    private fun addImage(bitmap: android.graphics.Bitmap) {
        selectedImages.add(bitmap)
        addImageToContainer(bitmap)
        showImagesContainer()
    }

    private fun addImageToContainer(bitmap: android.graphics.Bitmap) {
        val imagesList = dialog.findViewById<LinearLayout>(R.id.images_list)
        val inflater = LayoutInflater.from(activity)
        val imageItemView = inflater.inflate(R.layout.image_item_simple, imagesList, false)
        
        val imageView = imageItemView.findViewById<ImageView>(R.id.image_preview)
        val removeButton = imageItemView.findViewById<ImageButton>(R.id.remove_button)
        
        // Set image directly from bitmap
        imageView.setImageBitmap(bitmap)
        
        // Set remove button click
        removeButton.setOnClickListener {
            removeImage(bitmap, imageItemView)
        }
        
        imagesList.addView(imageItemView)
    }

    private fun removeImage(bitmap: android.graphics.Bitmap, imageItemView: View) {
        val imagesList = dialog.findViewById<LinearLayout>(R.id.images_list)
        imagesList.removeView(imageItemView)
        selectedImages.remove(bitmap)
        
        if (selectedImages.isEmpty()) {
            hideImagesContainer()
        }
    }

    private fun showImagesContainer() {
        dialog.findViewById<LinearLayout>(R.id.images_container).visibility = View.VISIBLE
    }

    private fun hideImagesContainer() {
        dialog.findViewById<LinearLayout>(R.id.images_container).visibility = View.GONE
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccessToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }
    
    fun setOnRoomAddedListener(listener: () -> Unit) {
        onRoomAddedListener = listener
    }
}
