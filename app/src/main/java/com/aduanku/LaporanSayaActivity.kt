package com.aduanku

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aduanku.databinding.ActivityLaporanSayaBinding
import com.aduanku.databinding.LaporanCardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class LaporanSayaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaporanSayaBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize binding
        binding = ActivityLaporanSayaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fetch reports for the logged-in user
        fetchReports()
    }

    // Fetch reports for the logged-in user
    private fun fetchReports() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("reports")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(this, "No reports found", Toast.LENGTH_SHORT).show()
                    } else {
                        displayReports(documents)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to fetch reports: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Display reports in the UI
    private fun displayReports(documents: QuerySnapshot) {
        val layoutInflater = LayoutInflater.from(this)

        // Iterate over each document and create a CardView
        for (document in documents) {
            val report = document.data

            // Inflate the laporan card layout
            val laporanCardBinding = LaporanCardBinding.inflate(layoutInflater)
            val cardView = laporanCardBinding.root

            // Bind the data to the CardView
            laporanCardBinding.title.text = report["jenisLaporan"].toString()
            laporanCardBinding.description.text = report["description"].toString()
            laporanCardBinding.location.text = report["status"].toString()  // Change location to status

            // Optionally, set an image if needed
            // val imageUrl = report["imageUrl"].toString()
            // Load image using an image loading library (e.g., Glide)

            // Add the card view to the container
            binding.cardContainer.addView(cardView)
        }
    }
}
