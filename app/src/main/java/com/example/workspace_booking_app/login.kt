package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.workspace_booking_app.databinding.ActivityLoginBinding
import com.example.workspace_booking_app.utils.DialogUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // If already logged in → check role
        if (auth.currentUser != null) {
            checkUserRole(auth.currentUser!!.uid)
        }

        // Go to Register Screen
        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        // Login Button
        binding.btnLogin.setOnClickListener {

            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (isLoginInputValid(email, password)) {

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            val user = auth.currentUser

                            Toast.makeText(
                                this,
                                "Login Successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            if (user != null) {
                                checkUserRole(user.uid)
                            }

                        } else {

                            DialogUtils.showMessage(
                                context = this@LoginActivity,
                                title = "Login Failed",
                                message = task.exception?.message ?: "Authentication failed"
                            )
                        }
                    }
            }
        }
    }

    // 🔥 Check role from Firestore
    private fun checkUserRole(uid: String) {

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val role = document.getString("role")

                    if (role == "admin") {

                        startActivity(
                            Intent(this, AdminActivity::class.java)
                        )

                    } else {

                        startActivity(
                            Intent(this, BookingDetailsActivity::class.java)
                        )
                    }

                    finish()

                } else {

                    Toast.makeText(
                        this,
                        "User data not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Error checking role",
                    Toast.LENGTH_SHORT
                ).show()
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