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
import com.example.workspace_booking_app.sharedstore.Workspace
import com.example.workspace_booking_app.utils.ImageUtils

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Workspace.setupDefaultData(this)
        setContentView(R.layout.admin_page)
        val workspace_name = findViewById<TextView>(R.id.workspace_name)
        workspace_name.setText(Workspace.getWorkspaceName())
        val bannerPath = Workspace.getWorkspaceBannerPath()
        val bannerImageView = findViewById<ImageView>(R.id.imageViewBanner)
        bannerPath?.let {
            ImageUtils.setImageFromPath(bannerImageView, it)
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
}
