package com.example.uniride

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random

class login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var captchaInput: EditText
    private lateinit var captchaText: TextView
    private lateinit var loginButton: Button
    private lateinit var passwordToggle: ImageView
    private lateinit var refreshCaptcha: ImageView

    private var currentCaptcha: String = ""
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()



        // Initialize views
        emailEditText = findViewById(R.id.user_id)
        passwordEditText = findViewById(R.id.password)
        captchaInput = findViewById(R.id.captcha_input)
        captchaText = findViewById(R.id.captcha_text)
        loginButton = findViewById(R.id.login_button)
        passwordToggle = findViewById(R.id.toggle_password)
        refreshCaptcha = findViewById(R.id.refresh_captcha)

        // Generate initial captcha
        generateCaptcha()

        // Set click listeners
        passwordToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        refreshCaptcha.setOnClickListener {
            generateCaptcha()
        }

        loginButton.setOnClickListener {
            if (validateCaptcha()) {
                performLogin()
            } else {
                Toast.makeText(this, "Invalid Captcha!", Toast.LENGTH_SHORT).show()
                generateCaptcha() // Generate new captcha after failed attempt
            }
        }
    }

    private fun generateCaptcha() {
        // Generate a random 6-character captcha
        val allowedChars = ('A'..'Z') + ('0'..'9')
        currentCaptcha = (1..6)
            .map { allowedChars[Random.nextInt(0, allowedChars.size)] }
            .joinToString("")
        captchaText.text = currentCaptcha
    }

    private fun validateCaptcha(): Boolean {
        return captchaInput.text.toString() == currentCaptcha
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            // Show password
            passwordEditText.transformationMethod = null
            passwordToggle.setImageResource(R.drawable.ic_visibility_on)
        } else {
            // Hide password
            passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            passwordToggle.setImageResource(R.drawable.ic_visibility_off)
        }
        // Keep cursor position
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun performLogin() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Input validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state
        loginButton.isEnabled = false
        loginButton.text = "Logging in..."

        // Perform Firebase authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login successful
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    // Navigate to main screen
                    startHomeActivity()
                } else {
                    // Login failed
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    // Reset button state and generate new captcha
                    loginButton.isEnabled = true
                    loginButton.text = "LOGIN"
                    generateCaptcha()
                }
            }
    }
    private fun startHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        // Clear the back stack so user can't go back to login screen
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}