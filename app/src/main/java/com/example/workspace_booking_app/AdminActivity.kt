package com.example.workspace_booking_app

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workspace_booking_app.sharedstore.Workspace
import com.example.workspace_booking_app.utils.ImageUtils

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Workspace.setupDefaultData(this)
        setContentView(R.layout.admin_page)
        
        // Setup workspace banner
        val workspace_name = findViewById<TextView>(R.id.workspace_name)
        workspace_name.setText(Workspace.getWorkspaceName())
        val bannerPath = Workspace.getWorkspaceBannerPath()
        val bannerImageView = findViewById<ImageView>(R.id.imageViewBanner)
        bannerPath?.let {
            ImageUtils.setImageFromPath(bannerImageView, it)
        }

        // Setup room list
        setupRoomList()

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
                    workspace_name.setText(Workspace.getWorkspaceName())
                    val bannerPath = Workspace.getWorkspaceBannerPath()
                    val bannerImageView = findViewById<ImageView>(R.id.imageViewBanner)
                    bannerPath?.let {
                        ImageUtils.setImageFromPath(bannerImageView, it)
                    }
                }
            }
            dialog.show(supportFragmentManager, "AddRoomDialogFragment")
        }
    }

    private fun setupRoomList() {
        val recyclerView = findViewById<RecyclerView>(R.id.rooms_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Dummy data
        val dummyRooms = listOf(
            Room("1", "Conference Room A", "Surat, Gujarat", 8, "meeting", true, true),
            Room("2", "Break Room B", "Surat, Gujarat", 4, "chill", true, false),
            Room("3", "Office 1", "Surat, Gujarat", 2, "normal", true, false),
            Room("4", "Meeting Hall", "Surat, Gujarat", 12, "meeting", false, true),
            Room("5", "Lounge Area", "Surat, Gujarat", 6, "chill", false, false)
        )
        
        recyclerView.adapter = RoomAdapter(dummyRooms)
    }
}
