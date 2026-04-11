package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workspace_booking_app.LoginActivity
import com.example.workspace_booking_app.databinding.ActivityRegisterBinding
import com.example.workspace_booking_app.utils.DialogUtils
import com.example.workspace_booking_app.firebase.FirebaseUserRepo
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val userRepo = FirebaseUserRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.signup.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (isInputValid(name, email, password, confirmPassword)) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                            // Save user profile to Firestore
                            userRepo.saveUserProfile(
                                uid = uid,
                                name = name,
                                email = email,
                                role = "user",
                                onSuccess = {
                                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                    auth.signOut()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                },
                                onFailure = {
                                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                    auth.signOut()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                            )
                        } else {
                            DialogUtils.showMessage(
                                context = this,
                                title = "Registration Failed",
                                message = task.exception?.message ?: "Something went wrong"
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
            DialogUtils.showMessage(this, "Missing Information", "Please fill in all the fields.")
            return false
        }
        if (!email.matches(emailPattern.toRegex())) {
            DialogUtils.showMessage(this, "Invalid Email", "Please enter a valid email address.")
            return false
        }
        if (password != confirmPassword) {
            DialogUtils.showMessage(this, "Password Mismatch", "The passwords you entered do not match.")
            return false
        }
        if (password.length < 6) {
            DialogUtils.showMessage(this, "Weak Password", "Password must be at least 6 characters long.")
            return false
        }
        return true
    }
}
