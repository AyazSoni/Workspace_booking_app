package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.workspace_booking_app.data.WorkspaceRepo
import com.example.workspace_booking_app.data.RoomRepo
import com.example.workspace_booking_app.utils.ImageUtils
import com.example.workspace_booking_app.Room

class AdminActivity : AppCompatActivity() {

    private lateinit var addRoomDialog: Add_room
    private var currentEditDialog: EditRoomDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.admin_page)

        val workspaceRepo = WorkspaceRepo(this)
        
        // Setup workspace banner
        val workspace_name = findViewById<TextView>(R.id.workspace_name)
        val workspace = workspaceRepo.getWorkspace()

        workspace_name.setText(workspace?.get("name") ?: "Default Workspace" )
        val bannerPath = workspace?.get("banner_path")
        val bannerImageView = findViewById<ImageView>(R.id.imageViewBanner)
        bannerPath?.let {
            ImageUtils.setImageFromPath(bannerImageView, it)
        }

        // Setup room list
        setupRoomList()

        // Initialize Add Room Dialog (registerForActivityResult must be called before RESUMED)
        addRoomDialog = Add_room(this)
        addRoomDialog.setOnRoomAddedListener {
            setupRoomList() // Refresh the room list
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val settingButton = findViewById<ImageButton>(R.id.setting_btn)
        settingButton.setOnClickListener {
            val dialog = AddRoomDialogFragment()
            dialog.onDialogCloseListener = object : AddRoomDialogFragment.OnDialogCloseListener {
                override fun onDialogClosed() {
                   val workspace_name = findViewById<TextView>(R.id.workspace_name)
                    val workspace = workspaceRepo.getWorkspace()
                    workspace_name.setText(workspace?.get("name") ?: "harami Workspace")
                    val bannerPath = workspace?.get("banner_path")
                    val bannerImageView = findViewById<ImageView>(R.id.imageViewBanner)
                    bannerPath?.let {
                        ImageUtils.setImageFromPath(bannerImageView, it)
                    }
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

    private fun setupRoomList() {
        val recyclerView = findViewById<RecyclerView>(R.id.rooms_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Get real data from database
        val roomRepo = RoomRepo(this)
        val roomsData = roomRepo.getAllRooms()
        
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
        
        recyclerView.adapter = RoomAdapter(
            rooms = realRooms,
            activity = this,
            onRoomUpdatedListener = {
                setupRoomList() // Refresh the room list when a room is updated
            },
            onEditDialogCreated = { editDialog ->
                currentEditDialog = editDialog
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        currentEditDialog?.handleActivityResult(requestCode, resultCode, data)
    }
}


