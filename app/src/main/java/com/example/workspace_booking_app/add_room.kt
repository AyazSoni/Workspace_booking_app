package com.example.workspace_booking_app

import android.app.Dialog
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import com.example.workspace_booking_app.firebase.FirebaseRoomRepo
import com.google.android.material.textfield.TextInputEditText

class Add_room(private val activity: AppCompatActivity) {

    private lateinit var dialog: Dialog
    private val selectedImages = mutableListOf<Bitmap>()

    // 🔥 Firebase Repo
    private val firebaseRoomRepo = FirebaseRoomRepo()

    private var onRoomAddedListener: (() -> Unit)? = null

    private val pickMedia =
        activity.registerForActivityResult(PickVisualMedia()) { uri ->

            if (uri != null) {

                val bitmap =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {

                        android.graphics.ImageDecoder.decodeBitmap(
                            android.graphics.ImageDecoder.createSource(
                                activity.contentResolver,
                                uri
                            )
                        )

                    } else {

                        android.provider.MediaStore.Images.Media.getBitmap(
                            activity.contentResolver,
                            uri
                        )
                    }

                addImage(bitmap)
            }
        }

    fun show() {

        selectedImages.clear()

        dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.add_room)

        dialog.setOnShowListener {

            val width =
                (activity.resources.displayMetrics.widthPixels * 0.95).toInt()

            val height =
                (activity.resources.displayMetrics.heightPixels * 0.9).toInt()

            dialog.window?.setLayout(width, height)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        setupViews()
        dialog.show()
    }

    private fun setupViews() {

        val imagesList = dialog.findViewById<LinearLayout>(R.id.images_list)
        imagesList.removeAllViews()
        hideImagesContainer()

        val roomNameInput =
            dialog.findViewById<TextInputEditText>(R.id.room_name_input)

        val roomTypeDropdown =
            dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown)

        val roomSizeInput =
            dialog.findViewById<TextInputEditText>(R.id.room_size_input)

        val roomAddressInput =
            dialog.findViewById<TextInputEditText>(R.id.room_address_input)

        val roomDescriptionInput =
            dialog.findViewById<TextInputEditText>(R.id.room_description_input)

        val computerSwitch =
            dialog.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.computer_switch)

        val projectorSwitch =
            dialog.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.projector_switch)

        val uploadButton =
            dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.upload_button)

        val saveButton =
            dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.save_room_button)

        val closeButton =
            dialog.findViewById<ImageButton>(R.id.btnClose)

        val roomTypes = arrayOf("Meeting", "Normal", "Chill")

        val adapter =
            ArrayAdapter(activity, R.layout.dropdown_item_room_type, roomTypes)

        roomTypeDropdown.setAdapter(adapter)

        uploadButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        saveButton.setOnClickListener {
            saveRoom()
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun saveRoom() {

        val roomName =
            dialog.findViewById<TextInputEditText>(R.id.room_name_input)
                .text.toString().trim()

        val roomType =
            dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown)
                .text.toString().trim()

        val roomSizeStr =
            dialog.findViewById<TextInputEditText>(R.id.room_size_input)
                .text.toString().trim()

        val roomAddress =
            dialog.findViewById<TextInputEditText>(R.id.room_address_input)
                .text.toString().trim()

        val roomDescription =
            dialog.findViewById<TextInputEditText>(R.id.room_description_input)
                .text.toString().trim()

        val hasComputer =
            dialog.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.computer_switch)
                .isChecked

        val hasProjector =
            dialog.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.projector_switch)
                .isChecked

        if (roomName.isEmpty()) {
            showErrorToast("Please enter room name")
            return
        }

        if (roomType.isEmpty()) {
            showErrorToast("Please select room type")
            return
        }

        val roomSize = roomSizeStr.toIntOrNull()

        if (roomSize == null || roomSize <= 0) {
            showErrorToast("Invalid room size")
            return
        }

        if (roomAddress.isEmpty()) {
            showErrorToast("Please enter room address")
            return
        }

        if (selectedImages.isEmpty()) {
            showErrorToast("Upload at least one image")
            return
        }

        // 🔥 Save to Firestore
        firebaseRoomRepo.addRoom(
            name = roomName,
            capacity = roomSize,
            price = 0,
            onSuccess = {

                showSuccessToast("Room added successfully!")

                onRoomAddedListener?.invoke()

                dialog.dismiss()
            },
            onFailure = {

                showErrorToast("Failed to save room")
            }
        )
    }

    private fun addImage(bitmap: Bitmap) {

        selectedImages.add(bitmap)
        addImageToContainer(bitmap)
        showImagesContainer()
    }

    private fun addImageToContainer(bitmap: Bitmap) {

        val imagesList =
            dialog.findViewById<LinearLayout>(R.id.images_list)

        val inflater = LayoutInflater.from(activity)

        val imageItemView =
            inflater.inflate(R.layout.image_item_simple, imagesList, false)

        val imageView =
            imageItemView.findViewById<ImageView>(R.id.image_preview)

        val removeButton =
            imageItemView.findViewById<ImageButton>(R.id.remove_button)

        imageView.setImageBitmap(bitmap)

        removeButton.setOnClickListener {
            removeImage(bitmap, imageItemView)
        }

        imagesList.addView(imageItemView)
    }

    private fun removeImage(bitmap: Bitmap, imageItemView: View) {

        val imagesList =
            dialog.findViewById<LinearLayout>(R.id.images_list)

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