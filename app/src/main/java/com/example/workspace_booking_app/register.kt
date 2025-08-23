package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workspace_booking_app.LoginActivity
import com.example.workspace_booking_app.data.UserRepo
import com.example.workspace_booking_app.databinding.ActivityRegisterBinding
import com.example.workspace_booking_app.utils.DialogUtils

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userRepo = UserRepo(this)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val btnLogin = binding.btnLogin

        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


        binding.signup.setOnClickListener {
            val name = binding.etFullName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (isInputValid(name, email, password, confirmPassword)) {
                val user = userRepo.insertUser(name, email, password)
                when (user) {
                    -2L -> DialogUtils.showMessage(
                        context = this,
                        title = "Account Already Exists",
                        message = "An account with this email already exists. Please log in instead.",
                        positiveText = "Login",
                        positiveAction = {
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        },
                        negativeText = "Cancel"
                    )

                    -1L -> DialogUtils.showMessage(
                        context = this,
                        title = "Server Error",
                        message = "Something went wrong on our end. Please try again later."
                    )

                    else -> {
                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
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
