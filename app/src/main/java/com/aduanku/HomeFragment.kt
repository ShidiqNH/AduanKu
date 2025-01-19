package com.aduanku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aduanku.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val rootView = binding.root

        // Fetch the banner image link from Firestore
        fetchBannerImage()

        // Check if the user is signed in
        checkUserSignInStatus()

        // Set click listeners for buttons
        setupButtonListeners()

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupButtonListeners() {
        // Set click listener on Laporan Warga button
        binding.laporanWarga.setOnClickListener {
            if (auth.currentUser != null) {
                val intent = Intent(requireContext(), LaporanActivity::class.java)
                startActivity(intent)
            } else {
                navigateToLogin()
            }
        }

        // Set click listener on Laporan Saya button
        binding.laporanSaya.setOnClickListener {
            if (auth.currentUser != null) {
                val intent = Intent(requireContext(), LaporanSayaActivity::class.java)
                startActivity(intent)
            } else {
                navigateToLogin()
            }
        }

        // Set click listener on Cuaca button
        binding.Cuaca.setOnClickListener {
            if (auth.currentUser != null) {
                val intent = Intent(requireContext(), CuacaActivity::class.java)
                startActivity(intent)
            } else {
                navigateToLogin()
            }
        }
    }

    private fun checkUserSignInStatus() {
        if (auth.currentUser == null) {
            // If the user is not signed in, handle it here if needed
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    // Fetch the banner image link from Firestore
    private fun fetchBannerImage() {
        val imageBannerRef = firestore.collection("imageBanner").document("image")
        Log.d("HomeFragment", "Fetching document at path: ${imageBannerRef.path}")

        imageBannerRef.get()
            .addOnSuccessListener { documentSnapshot ->
                Log.d("HomeFragment", "Document retrieved: ${documentSnapshot.id}")
                if (documentSnapshot.exists()) {
                    val imgLink = documentSnapshot.getString("imgLink")
                    imgLink?.let {
                        Log.d("HomeFragment", "Image URL: $it")
                        Glide.with(this)
                            .load(it)
                            .timeout(5000)  // Set timeout to 5 seconds or more
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .into(binding.bannerImage)
                    }
                } else {
                    Log.e("HomeFragment", "No such document!")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error fetching document: ${exception.message}")
            }
    }
}
