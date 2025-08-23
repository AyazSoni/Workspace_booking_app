package com.example.workspace_booking_app

import android.app.Dialog
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import com.example.workspace_booking_app.utils.ImageUtils
import java.io.File

class ImageUploadDialog(private val activity: AppCompatActivity) {
    
    private lateinit var dialog: Dialog
    private val selectedImages = mutableListOf<String>()

    private val pickMedia = activity.registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(activity.contentResolver, uri))
            } else {
                android.provider.MediaStore.Images.Media.getBitmap(activity.contentResolver, uri)
            }
            val path = ImageUtils.saveImageWithUniqueName(activity, bitmap, "product")
            Toast.makeText(activity, "Image Saved", Toast.LENGTH_SHORT).show()
            addImage(path)
        }
    }

    fun show() {
        dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.image_upload_dialog)

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
        val uploadButton = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.upload_button)
        val saveButton = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.save_room_button)
        val closeButton = dialog.findViewById<ImageButton>(R.id.btnClose)
        val roomTypeDropdown = dialog.findViewById<AutoCompleteTextView>(R.id.room_type_dropdown)
        val computerSwitch = dialog.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.computer_switch)
        val projectorSwitch = dialog.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.projector_switch)

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
        // TODO: Implement save room functionality
        // Get all input values and save to database
        Toast.makeText(activity, "Room saved successfully!", Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }

    private fun addImage(imagePath: String) {
        selectedImages.add(imagePath)
        addImageToContainer(imagePath)
        showImagesContainer()
    }

    private fun addImageToContainer(imagePath: String) {
        val imagesList = dialog.findViewById<LinearLayout>(R.id.images_list)
        val inflater = LayoutInflater.from(activity)
        val imageItemView = inflater.inflate(R.layout.image_item_simple, imagesList, false)
        
        val imageView = imageItemView.findViewById<ImageView>(R.id.image_preview)
        val removeButton = imageItemView.findViewById<ImageButton>(R.id.remove_button)
        
        // Load image
        val file = File(imagePath)
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            imageView.setImageBitmap(bitmap)
        }
        
        // Set remove button click
        removeButton.setOnClickListener {
            removeImage(imagePath, imageItemView)
        }
        
        imagesList.addView(imageItemView)
    }

    private fun removeImage(imagePath: String, imageItemView: View) {
        ImageUtils.deleteImageFromStorage(imagePath)
        
        val imagesList = dialog.findViewById<LinearLayout>(R.id.images_list)
        imagesList.removeView(imageItemView)
        selectedImages.remove(imagePath)
        
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
}
