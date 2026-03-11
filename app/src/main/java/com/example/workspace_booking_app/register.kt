package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workspace_booking_app.databinding.ActivityRegisterBinding
import com.example.workspace_booking_app.utils.DialogUtils
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Go to Login Screen
        binding.btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Register Button
        binding.signup.setOnClickListener {

            val name = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (isInputValid(name, email, password, confirmPassword)) {

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            Toast.makeText(
                                this,
                                "Registration Successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Go to Login Screen
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {

                            DialogUtils.showMessage(
                                context = this,
                                title = "Registration Failed",
                                message = task.exception?.message
                                    ?: "Something went wrong"
                            )
                        }
                    }
            }
        }
    }

    private fun isInputValid(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {

        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            DialogUtils.showMessage(
                context = this,
                title = "Missing Information",
                message = "Please fill in all the fields."
            )
            return false
        }

        if (!email.matches(emailPattern.toRegex())) {
            DialogUtils.showMessage(
                context = this,
                title = "Invalid Email",
                message = "Please enter a valid email address."
            )
            return false
        }

        if (password != confirmPassword) {
            DialogUtils.showMessage(
                context = this,
                title = "Password Mismatch",
                message = "The passwords you entered do not match."
            )
            return false
        }

        if (password.length < 6) {
            DialogUtils.showMessage(
                context = this,
                title = "Weak Password",
                message = "Password must be at least 6 characters long."
            )
            return false
        }

        return true
    }
}