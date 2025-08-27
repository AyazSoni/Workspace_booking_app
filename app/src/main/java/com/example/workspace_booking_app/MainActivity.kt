package com.example.workspace_booking_app
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.workspace_booking_app.utils.SessionManager


class MainActivity : AppCompatActivity() {
    private lateinit var btnHome: LinearLayout
    private lateinit var btnProfile: LinearLayout
    private lateinit var txtHome: TextView
    private lateinit var txtProfile: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Check if user is logged in
        val sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            // Redirect to login if not logged in
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize navigation buttons and text views
        btnHome = findViewById(R.id.btnHome)
        btnProfile = findViewById(R.id.btnProfile)
        
        // Find TextViews directly by their IDs
        txtHome = findViewById(R.id.txtHome)
        txtProfile = findViewById(R.id.txtProfile)

        // Check if we should show profile page (after booking completion)
        val showProfile = intent.getBooleanExtra("show_profile", false)
        
        if (showProfile) {
            // Load Profile fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ProfileFragment())
                .commit()
            // Set initial state - Profile selected
            updateNavigationState(false)
        } else {
            // load Home by default
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
            // Set initial state - Home selected
            updateNavigationState(true)
        }

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
    

