package com.aduanku

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.aduanku.databinding.FragmentProfileBinding  // Import the generated ViewBinding class

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout and initialize ViewBinding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Initialize FirebaseAuth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Load user profile
        loadUserProfile()

        // Set up the logout button click listener
        binding.logoutButton.setOnClickListener {
            logoutUser()
        }

        // Set up the reset password button click listener
        binding.resetPasswordButton.setOnClickListener {
            resetPassword()
        }

        return binding.root // Return the root view from the binding
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            // Display user's email from FirebaseAuth
            binding.emailTextView.text = "Email: ${user.email}"

            // Get the username from Firestore
            firestore.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username")
                        if (username != null) {
                            binding.usernameTextView.text = "Username: $username"
                        } else {
                            binding.usernameTextView.text = "Username: Not available"
                        }
                    } else {
                        Toast.makeText(requireContext(), "No user document found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error getting user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logoutUser() {
        // Sign out the user from FirebaseAuth
        auth.signOut()

        // Show a message that the user has been logged out
        Toast.makeText(requireContext(), "You have logged out", Toast.LENGTH_SHORT).show()

        // Navigate to the LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()  // Optional: Close the current activity
    }

    // Reset Password Function
    private fun resetPassword() {
        val email = binding.emailTextView.text.toString().replace("Email: ", "").trim()

        if (email.isNotEmpty()) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Password reset email sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to send password reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "Email is not available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Set the binding reference to null to avoid memory leaks
        _binding = null
    }
}
