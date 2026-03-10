package com.example.workspace_booking_app

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnHome: LinearLayout
    private lateinit var btnProfile: LinearLayout
    private lateinit var txtHome: TextView
    private lateinit var txtProfile: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnHome = findViewById(R.id.btnHome)
        btnProfile = findViewById(R.id.btnProfile)

        txtHome = findViewById(R.id.txtHome)
        txtProfile = findViewById(R.id.txtProfile)

        // Load HomeFragment by default
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeFragment())
            .commit()

        updateNavigationState(true)

        btnHome.setOnClickListener {

            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()

            updateNavigationState(true)
        }

        btnProfile.setOnClickListener {

            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ProfileFragment())
                .commit()

            updateNavigationState(false)
        }
    }

    private fun updateNavigationState(isHomeSelected: Boolean) {

        if (isHomeSelected) {

            btnHome.setBackgroundResource(R.drawable.nav_item_selected)
            btnProfile.setBackgroundResource(android.R.color.transparent)

            txtHome.setTextColor(getColor(android.R.color.black))
            txtProfile.setTextColor(getColor(android.R.color.white))

        } else {

            btnHome.setBackgroundResource(android.R.color.transparent)
            btnProfile.setBackgroundResource(R.drawable.nav_item_selected)

            txtHome.setTextColor(getColor(android.R.color.white))
            txtProfile.setTextColor(getColor(android.R.color.black))
        }
    }
}