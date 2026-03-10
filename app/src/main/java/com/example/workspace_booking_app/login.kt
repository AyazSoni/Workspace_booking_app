package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.workspace_booking_app.databinding.ActivityLoginBinding
import com.example.workspace_booking_app.utils.DialogUtils
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // If user already logged in → skip login
        if (auth.currentUser != null) {
            startActivity(Intent(this, BookingDetailsActivity::class.java))
            finish()
        }

        // Go to Register Screen
        binding.btnSignup.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        // Login Button
        binding.btnLogin.setOnClickListener {

            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (isLoginInputValid(email, password)) {

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            Toast.makeText(
                                this,
                                "Login Successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(this, BookingDetailsActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {

                            DialogUtils.showMessage(
                                context = this@LoginActivity,
                                title = "Login Failed",
                                message = task.exception?.message ?: "Authentication failed"
                            )
                        }
                    }
            } else {
                Toast.makeText(this, "Please fix the errors", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLoginInputValid(email: String, password: String): Boolean {

        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        if (email.isEmpty() || password.isEmpty()) {
            DialogUtils.showMessage(
                context = this@LoginActivity,
                title = "Missing Information",
                message = "Please enter both your email and password."
            )
            return false
        }

        if (!email.matches(emailPattern.toRegex())) {
            DialogUtils.showMessage(
                context = this@LoginActivity,
                title = "Invalid Email",
                message = "Please enter a valid email address."
            )
            return false
        }

        return true
    }
}