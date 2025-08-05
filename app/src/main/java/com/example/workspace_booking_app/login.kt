package com.example.workspace_booking_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.workspace_booking_app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (isLoginInputValid(email, password)) {
                if (UserManager.isValidUser(email, password)) {
                    Toast.makeText(this, "Login Successfull", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
        private fun isLoginInputValid(email: String, password: String): Boolean {
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
           if (email.isEmpty() || password.isEmpty()) {
               Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
               return false
           }
            if (!email.matches(emailPattern.toRegex())) {
                Toast.makeText(this, "Invalid Email Format", Toast.LENGTH_SHORT).show()
                return false
            }
            return true

        }
}