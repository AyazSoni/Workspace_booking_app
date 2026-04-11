package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workspace_booking_app.firebase.FirebaseRoomRepo
import com.example.workspace_booking_app.firebase.FirebaseWorkspaceRepo
import com.google.firebase.auth.FirebaseAuth
import coil.load

class AdminActivity : AppCompatActivity() {

    private lateinit var addRoomDialog: Add_room
    private val firebaseRoomRepo = FirebaseRoomRepo()
    private val workspaceRepo = FirebaseWorkspaceRepo()
    private var currentEditDialog: EditRoomDialog? = null

    // Registered early (before STARTED) so EditRoomDialog can use it
    private val editPickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                android.graphics.ImageDecoder.decodeBitmap(
                    android.graphics.ImageDecoder.createSource(contentResolver, uri)
                )
            } else {
                android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            currentEditDialog?.addImage(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.admin_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load workspace header from Firestore
        loadWorkspaceHeader()

        // Setup room list from Firestore
        setupRoomList()

        // Initialize Add Room Dialog
        addRoomDialog = Add_room(this)
        addRoomDialog.setOnRoomAddedListener {
            setupRoomList()
        }

        // Logout button
        findViewById<ImageButton>(R.id.btn_logout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Settings button
        val settingButton = findViewById<ImageButton>(R.id.setting_btn)
        settingButton.setOnClickListener {
            val dialog = AddRoomDialogFragment()
            dialog.onDialogCloseListener = object : AddRoomDialogFragment.OnDialogCloseListener {
                override fun onDialogClosed() {
                    loadWorkspaceHeader()
                }
            }
            dialog.show(supportFragmentManager, "AddRoomDialogFragment")
        }

        // Add Room Button Click Handler
        val addRoomButton = findViewById<Button>(R.id.add_room_fab)
        addRoomButton.setOnClickListener {
            addRoomDialog.show()
        }
    }

    private fun loadWorkspaceHeader() {
        val workspaceName = findViewById<TextView>(R.id.workspace_name)
        val bannerImageView = findViewById<ImageView>(R.id.imageViewBanner)

        workspaceRepo.getOrCreateDefaultWorkspace(
            onSuccess = { workspace ->
                runOnUiThread {
                    workspaceName.text = workspace["name"] as? String ?: "Default Workspace"
                    val bannerUrl = workspace["bannerUrl"] as? String
                    if (!bannerUrl.isNullOrEmpty()) {
                        bannerImageView.load(bannerUrl)
                    }
                }
            },
            onFailure = {
                runOnUiThread {
                    workspaceName.text = "Default Workspace"
                }
            }
        )
    }

    private fun setupRoomList() {
        val recyclerView = findViewById<RecyclerView>(R.id.rooms_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        firebaseRoomRepo.getRooms(
            onSuccess = { roomModels ->
                runOnUiThread {
                    val rooms = roomModels.map { rm ->
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

                    recyclerView.adapter = RoomAdapter(
                        rooms = rooms,
                        activity = this,
                        editPickMedia = editPickMedia,
                        onEditDialogCreated = { dialog -> currentEditDialog = dialog },
                        onRoomUpdatedListener = { setupRoomList() }
                    )
                }
            },
            onFailure = {
                runOnUiThread {
                    Toast.makeText(this, "Failed to load rooms", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
