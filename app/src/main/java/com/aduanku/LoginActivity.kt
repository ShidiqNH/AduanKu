package com.aduanku

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aduanku.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Google Sign-In request code
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Check if the user is already logged in
        val user = auth.currentUser
        if (user != null && user.isEmailVerified) {
            // If the user is already logged in and email is verified, navigate to MainActivity
            navigateToMainActivity()
        }

        // Set up the registerText click listener
        binding.registerText.setOnClickListener {
            // Start the RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Set up the Google sign-in button click listener
        binding.googleButton.setOnClickListener {
            signInWithGoogle()
        }

        // Set up email login button click listener
        binding.loginButton.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInWithEmail(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Sign in with Email and Password
    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        // Retrieve FCM token when the user successfully logs in
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val token = tokenTask.result
                                // Update the FCM token in Firestore for the user
                                updateFcmTokenInFirestore(token)
                                // Navigate to MainActivity after successful login and email verification
                                navigateToMainActivity()
                            } else {
                                Toast.makeText(this, "Failed to get FCM token", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Please verify your email first", Toast.LENGTH_LONG).show()
                        auth.signOut() // Sign out the user if email is not verified
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Google Sign-In setup
    private fun signInWithGoogle() {
        val googleSignInClient = GoogleSignIn.getClient(this, getGoogleSignInOptions())
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Configure Google Sign-In options
    private fun getGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Web client ID from Firebase Console
            .requestEmail()
            .build()
    }

    // Handle the result of Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    authenticateWithFirebase(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Authenticate with Firebase using Google Sign-In
    private fun authenticateWithFirebase(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // Retrieve FCM token when the user successfully logs in via Google
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val token = tokenTask.result
                                // Update the FCM token in Firestore for the user
                                updateFcmTokenInFirestore(token)
                                // Navigate to MainActivity
                                navigateToMainActivity()
                            } else {
                                Toast.makeText(this, "Failed to get FCM token", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Update the FCM token in Firestore
    private fun updateFcmTokenInFirestore(token: String?) {
        val user = auth.currentUser
        user?.let {
            val userRef = firestore.collection("users").document(it.uid)
            val updates = hashMapOf<String, Any>("fcmToken" to token!!)
            userRef.update(updates)
                .addOnSuccessListener {
                    // Successfully updated FCM token
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating token: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Navigate to MainActivity after successful login
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Close LoginActivity
    }
}
