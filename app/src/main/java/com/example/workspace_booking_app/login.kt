package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.workspace_booking_app.databinding.ActivityLoginBinding
import com.example.workspace_booking_app.utils.DialogUtils
import com.example.workspace_booking_app.firebase.FirebaseUserRepo
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val userRepo = FirebaseUserRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (isLoginInputValid(email, password)) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                            // Check user role from Firestore and route accordingly
                            userRepo.getUserRole(uid,
                                onSuccess = { role ->
                                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                    if (role == "admin") {
                                        startActivity(Intent(this, AdminActivity::class.java))
                                    } else {
                                        startActivity(Intent(this, MainActivity::class.java))
                                    }
                                    finish()
                                },
                                onFailure = {
                                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                            )
                        } else {
                            DialogUtils.showMessage(
                                context = this@LoginActivity,
                                title = "Login Failed",
                                message = task.exception?.message ?: "Authentication failed"
                            )
                        }
                    }
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLoginInputValid(email: String, password: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        if (email.isEmpty() || password.isEmpty()) {
            DialogUtils.showMessage(this@LoginActivity, "Missing Information", "Please enter both your email and password.")
            return false
        }
        if (!email.matches(emailPattern.toRegex())) {
            DialogUtils.showMessage(this@LoginActivity, "Invalid Email", "Please enter a valid email address.")
            return false
        }
        return true
    }
}
