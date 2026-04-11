package com.example.workspace_booking_app

import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import com.example.workspace_booking_app.firebase.FirebaseRoomRepo
import com.example.workspace_booking_app.supabase.SupabaseStorageHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import coil.load

class EditRoomDialog(
    private val activity: Activity,
    private val room: Room,
    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest>,
    private val onRoomUpdatedListener: (() -> Unit)? = null
) {
    private lateinit var dialog: Dialog
    private val firebaseRoomRepo = FirebaseRoomRepo()
    private val storageHelper = SupabaseStorageHelper()
    private val newImages = mutableListOf<Bitmap>()
    private val existingImageUrls = mutableListOf<String>()
    private val removedImageUrls = mutableListOf<String>()

    private var originalRoomName = ""
    private var originalRoomType = ""
    private var originalRoomSize = ""
    private var originalRoomAddress = ""
    private var originalRoomDescription = ""
    private var originalHasComputer = false
    private var originalHasProjector = false

    fun show() {
        newImages.clear()
        existingImageUrls.clear()
        existingImageUrls.addAll(room.imageUrls)
        removedImageUrls.clear()

        dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.add_room)

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
        dialog.findViewById<TextView>(R.id.textViewTitle).text = "Edit Room"

        val roomTypeDropdown = dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown)
        val roomTypes = arrayOf("Meeting", "Normal", "Chill")
        val adapter = ArrayAdapter(activity, R.layout.dropdown_item_room_type, roomTypes)
        roomTypeDropdown.setAdapter(adapter)

        val imagesList = dialog.findViewById<LinearLayout>(R.id.images_list)
        imagesList.removeAllViews()

        // Load existing images from URLs
        for (imageUrl in existingImageUrls) {
            addExistingImageToContainer(imagesList, imageUrl)
        }

        if (existingImageUrls.isNotEmpty()) {
            dialog.findViewById<LinearLayout>(R.id.images_container).visibility = View.VISIBLE
        } else {
            dialog.findViewById<LinearLayout>(R.id.images_container).visibility = View.GONE
        }
    }

    private fun loadRoomData() {
        val roomNameInput = dialog.findViewById<TextInputEditText>(R.id.room_name_input)
        val roomTypeDropdown = dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown)
        val roomSizeInput = dialog.findViewById<TextInputEditText>(R.id.room_size_input)
        val roomAddressInput = dialog.findViewById<TextInputEditText>(R.id.room_address_input)
        val roomDescriptionInput = dialog.findViewById<TextInputEditText>(R.id.room_description_input)
        val computerSwitch = dialog.findViewById<SwitchMaterial>(R.id.computer_switch)
        val projectorSwitch = dialog.findViewById<SwitchMaterial>(R.id.projector_switch)

        originalRoomName = room.name
        originalRoomType = room.roomType.replaceFirstChar { it.uppercase() }
        originalRoomSize = room.size.toString()
        originalRoomAddress = room.location
        originalRoomDescription = room.description
        originalHasComputer = room.hasComputer
        originalHasProjector = room.hasProjector

        roomNameInput.setText(originalRoomName)
        roomTypeDropdown.setText(originalRoomType, false)
        roomSizeInput.setText(originalRoomSize)
        roomAddressInput.setText(originalRoomAddress)
        roomDescriptionInput.setText(originalRoomDescription)
        computerSwitch.isChecked = originalHasComputer
        projectorSwitch.isChecked = originalHasProjector
    }

    private fun addExistingImageToContainer(imagesList: LinearLayout, imageUrl: String) {
        val inflater = LayoutInflater.from(activity)
        val imageItemView = inflater.inflate(R.layout.image_item_simple, imagesList, false)
        val imageView = imageItemView.findViewById<ImageView>(R.id.image_preview)
        val removeButton = imageItemView.findViewById<ImageButton>(R.id.remove_button)

        imageView.load(imageUrl)

        removeButton.setOnClickListener {
            imagesList.removeView(imageItemView)
            existingImageUrls.remove(imageUrl)
            removedImageUrls.add(imageUrl)
            if (existingImageUrls.isEmpty() && newImages.isEmpty()) {
                dialog.findViewById<LinearLayout>(R.id.images_container).visibility = View.GONE
            }
            checkForChanges()
        }

        imagesList.addView(imageItemView)
    }

    private fun setupListeners() {
        dialog.findViewById<ImageButton>(R.id.btnClose).setOnClickListener { dialog.dismiss() }

        dialog.findViewById<MaterialButton>(R.id.upload_button).setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        val saveButton = dialog.findViewById<MaterialButton>(R.id.save_room_button)
        saveButton.text = "Update Room"
        saveButton.isEnabled = false
        saveButton.setBackgroundColor(activity.getColor(android.R.color.darker_gray))
        saveButton.setOnClickListener { updateRoom() }

        // Text change listeners
        val watcher = createTextWatcher { checkForChanges() }
        dialog.findViewById<TextInputEditText>(R.id.room_name_input).addTextChangedListener(watcher)
        dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown).addTextChangedListener(createTextWatcher { checkForChanges() })
        dialog.findViewById<TextInputEditText>(R.id.room_size_input).addTextChangedListener(createTextWatcher { checkForChanges() })
        dialog.findViewById<TextInputEditText>(R.id.room_address_input).addTextChangedListener(createTextWatcher { checkForChanges() })
        dialog.findViewById<TextInputEditText>(R.id.room_description_input).addTextChangedListener(createTextWatcher { checkForChanges() })
        dialog.findViewById<SwitchMaterial>(R.id.computer_switch).setOnCheckedChangeListener { _, _ -> checkForChanges() }
        dialog.findViewById<SwitchMaterial>(R.id.projector_switch).setOnCheckedChangeListener { _, _ -> checkForChanges() }
    }

    private fun updateRoom() {
        val roomName = dialog.findViewById<TextInputEditText>(R.id.room_name_input).text.toString().trim()
        val roomType = dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown).text.toString().trim()
        val roomSizeStr = dialog.findViewById<TextInputEditText>(R.id.room_size_input).text.toString().trim()
        val roomAddress = dialog.findViewById<TextInputEditText>(R.id.room_address_input).text.toString().trim()
        val roomDescription = dialog.findViewById<TextInputEditText>(R.id.room_description_input).text.toString().trim()
        val hasComputer = dialog.findViewById<SwitchMaterial>(R.id.computer_switch).isChecked
        val hasProjector = dialog.findViewById<SwitchMaterial>(R.id.projector_switch).isChecked

        if (roomName.isEmpty()) { showToast("Please enter room name"); return }
        if (roomType.isEmpty()) { showToast("Please select room type"); return }
        val roomSize = roomSizeStr.toIntOrNull()
        if (roomSize == null || roomSize <= 0) { showToast("Invalid room size"); return }
        if (roomAddress.isEmpty()) { showToast("Please enter room address"); return }

        val saveButton = dialog.findViewById<MaterialButton>(R.id.save_room_button)
        saveButton.isEnabled = false
        saveButton.text = "Updating..."

        // Delete removed images from Supabase
        for (url in removedImageUrls) {
            storageHelper.deleteImage(url, onSuccess = {}, onFailure = {})
        }

        // Upload new images
        if (newImages.isNotEmpty()) {
            storageHelper.uploadMultipleImages(
                context = activity,
                bitmaps = newImages,
                folder = "rooms",
                onAllUploaded = { newUrls ->
                    val allImageUrls = existingImageUrls + newUrls
                    saveRoomToFirestore(roomName, roomType, roomSize, roomAddress, roomDescription, hasComputer, hasProjector, allImageUrls)
                },
                onFailure = {
                    activity.runOnUiThread {
                        saveButton.isEnabled = true
                        saveButton.text = "Update Room"
                        showToast("Failed to upload images")
                    }
                }
            )
        } else {
            saveRoomToFirestore(roomName, roomType, roomSize, roomAddress, roomDescription, hasComputer, hasProjector, existingImageUrls)
        }
    }

    private fun saveRoomToFirestore(
        name: String, roomType: String, size: Int, location: String,
        description: String, hasComputer: Boolean, hasProjector: Boolean, imageUrls: List<String>
    ) {
        val updates = mapOf(
            "name" to name,
            "roomType" to roomType.lowercase(),
            "size" to size,
            "location" to location,
            "description" to description,
            "hasComputer" to hasComputer,
            "hasProjector" to hasProjector,
            "imageUrls" to imageUrls
        )

        firebaseRoomRepo.updateRoom(room.id, updates,
            onSuccess = {
                activity.runOnUiThread {
                    showToast("Room updated successfully!")
                    onRoomUpdatedListener?.invoke()
                    dialog.dismiss()
                }
            },
            onFailure = {
                activity.runOnUiThread {
                    val saveButton = dialog.findViewById<MaterialButton>(R.id.save_room_button)
                    saveButton.isEnabled = true
                    saveButton.text = "Update Room"
                    showToast("Failed to update room")
                }
            }
        )
    }

    fun addImage(bitmap: Bitmap) {
        newImages.add(bitmap)
        val imagesList = dialog.findViewById<LinearLayout>(R.id.images_list)
        val inflater = LayoutInflater.from(activity)
        val imageItemView = inflater.inflate(R.layout.image_item_simple, imagesList, false)
        val imageView = imageItemView.findViewById<ImageView>(R.id.image_preview)
        val removeButton = imageItemView.findViewById<ImageButton>(R.id.remove_button)

        imageView.setImageBitmap(bitmap)
        removeButton.setOnClickListener {
            imagesList.removeView(imageItemView)
            newImages.remove(bitmap)
            if (existingImageUrls.isEmpty() && newImages.isEmpty()) {
                dialog.findViewById<LinearLayout>(R.id.images_container).visibility = View.GONE
            }
            checkForChanges()
        }

        imagesList.addView(imageItemView)
        dialog.findViewById<LinearLayout>(R.id.images_container).visibility = View.VISIBLE
        checkForChanges()
    }

    private fun checkForChanges() {
        val currentName = dialog.findViewById<TextInputEditText>(R.id.room_name_input).text.toString().trim()
        val currentType = dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown).text.toString().trim()
        val currentSize = dialog.findViewById<TextInputEditText>(R.id.room_size_input).text.toString().trim()
        val currentAddress = dialog.findViewById<TextInputEditText>(R.id.room_address_input).text.toString().trim()
        val currentDesc = dialog.findViewById<TextInputEditText>(R.id.room_description_input).text.toString().trim()
        val currentComputer = dialog.findViewById<SwitchMaterial>(R.id.computer_switch).isChecked
        val currentProjector = dialog.findViewById<SwitchMaterial>(R.id.projector_switch).isChecked
        val saveButton = dialog.findViewById<MaterialButton>(R.id.save_room_button)

        val hasChanges = currentName != originalRoomName ||
            currentType != originalRoomType ||
            currentSize != originalRoomSize ||
            currentAddress != originalRoomAddress ||
            currentDesc != originalRoomDescription ||
            currentComputer != originalHasComputer ||
            currentProjector != originalHasProjector ||
            newImages.isNotEmpty() ||
            removedImageUrls.isNotEmpty()

        saveButton.isEnabled = hasChanges
        saveButton.setBackgroundColor(
            activity.getColor(if (hasChanges) android.R.color.black else android.R.color.darker_gray)
        )
    }

    private fun createTextWatcher(onChanged: () -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { onChanged() }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}
