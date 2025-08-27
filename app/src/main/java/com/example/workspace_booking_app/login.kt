package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.workspace_booking_app.databinding.ActivityLoginBinding
import com.example.workspace_booking_app.data.UserRepo
import com.example.workspace_booking_app.utils.DialogUtils
import com.example.workspace_booking_app.utils.SessionManager

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        val userRepo = UserRepo(this)

        val btnSignup = binding.btnSignup

        btnSignup.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (isLoginInputValid(email, password)) {
                val user = userRepo.getValueByFilter("email" , email)
                if (user == null) {
                    DialogUtils.showMessage(
                        context = this@LoginActivity, // Ensures activity context
                        title = "Account Not Found",
                        message = "No account found with that email. Please sign up to continue.",
                        positiveText = "Sign Up",
                        positiveAction = {
                            startActivity(Intent(this@LoginActivity, Register::class.java))
                        },
                        negativeText = "Cancel"
                    )
                }
                else if (user["password"] != password) {
                    DialogUtils.showMessage(
                        context = this@LoginActivity,
                        title = "Wrong Password",
                        message = "The password you entered is incorrect."
                    )

                }
                else {
                    // Create user session
                    val sessionManager = SessionManager(this)
                    sessionManager.createLoginSession(
                        userId = user["id"] ?: "",
                        email = user["email"] ?: "",
                        name = user["name"] ?: "",
                        role = user["role"] ?: ""
                    )
                    
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    if(user["role"] == "admin"){
                        val intent = Intent(this, AdminActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
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
