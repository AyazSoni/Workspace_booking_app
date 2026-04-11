package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.workspace_booking_app.firebase.FirebaseUserRepo
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val userRepo = FirebaseUserRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // User is already logged in — check role and redirect
                userRepo.getUserRole(currentUser.uid,
                    onSuccess = { role ->
                        if (role == "admin") {
                            startActivity(Intent(this, AdminActivity::class.java))
                        } else {
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        finish()
                    },
                    onFailure = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, 2000)
    }
}
