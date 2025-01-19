package com.aduanku

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aduanku.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Register button click listener
        binding.loginButton.setOnClickListener {
            val username = binding.username.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val passwordRepeat = binding.passwordRepeat.text.toString().trim()

            // Check if passwords match
            if (password == passwordRepeat) {
                checkIfUsernameExists(username, email, password)
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to LoginActivity if registerText is clicked
        binding.loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    // Check if the username already exists
    private fun checkIfUsernameExists(username: String, email: String, password: String) {
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result!!
                    if (querySnapshot.isEmpty) {
                        // Username is available, proceed with registration
                        registerUser(username, email, password)
                    } else {
                        // Username already exists
                        Toast.makeText(this, "Username already taken, please choose another", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Error checking username
                    Toast.makeText(this, "Error checking username: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Register the user with email and password
    private fun registerUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Get the current user
                    val user = auth.currentUser
                    user?.let {
                        // Send email verification
                        it.sendEmailVerification()
                            .addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    Toast.makeText(this, "Verification email sent. Please verify your email before logging in.", Toast.LENGTH_LONG).show()

                                    // Get FCM token and save it
                                    FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                                        if (tokenTask.isSuccessful) {
                                            val token = tokenTask.result

                                            // Save user details along with the FCM token in Firestore
                                            val userData = hashMapOf(
                                                "username" to username,
                                                "email" to email,
                                                "role" to "user", // Default role is "user"
                                                "fcmToken" to token
                                            )

                                            firestore.collection("users")
                                                .document(user.uid)
                                                .set(userData)
                                                .addOnSuccessListener {
                                                    // Navigate to LoginActivity after successful registration and email verification
                                                    val intent = Intent(this, LoginActivity::class.java)
                                                    startActivity(intent)
                                                    finish() // Close RegisterActivity
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(this, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            Toast.makeText(this, "Failed to get FCM token", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(this, "Failed to send verification email: ${verificationTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    // If registration fails
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
