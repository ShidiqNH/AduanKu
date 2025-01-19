package com.aduanku

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.aduanku.databinding.ActivityLaporanBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class LaporanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaporanBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the reference to the container where you want to add the cards
        val cardContainer: LinearLayout = binding.cardContainer

        // Fetch all reports from Firestore
        fetchReports(cardContainer)
    }

    // Fetch reports from Firestore
    private fun fetchReports(cardContainer: LinearLayout) {
        firestore.collection("reports")
            .get() // Get all reports
            .addOnSuccessListener { documents ->
                // Check if there are reports
                if (documents.isEmpty) {
                    // Show message if no reports are found
                    showToast("No reports found")
                } else {
                    // Process and display the reports
                    displayReports(documents, cardContainer)
                }
            }
            .addOnFailureListener { e ->
                showToast("Failed to fetch reports: ${e.message}")
            }
    }

    // Display reports dynamically in the UI
    private fun displayReports(documents: QuerySnapshot, cardContainer: LinearLayout) {
        val layoutInflater = LayoutInflater.from(this)

        // Iterate through each document and create a CardView for each report
        for (document in documents) {
            val report = document.data

            // Inflate the laporan_card layout
            val laporanCardView: CardView = layoutInflater.inflate(R.layout.laporan_card, cardContainer, false) as CardView

            // Get the references to the views inside the laporan_card
            val imageView: ImageView = laporanCardView.findViewById(R.id.image)
            val titleTextView: TextView = laporanCardView.findViewById(R.id.title)
            val descriptionTextView: TextView = laporanCardView.findViewById(R.id.description)
            val locationTextView: TextView = laporanCardView.findViewById(R.id.location)

            // Set the data dynamically from the report
            imageView.setImageResource(R.drawable.placeholder_image) // Set a placeholder image (you can load dynamic images here)
            titleTextView.text = report["jenisLaporan"].toString()
            descriptionTextView.text = report["description"].toString()
            locationTextView.text = report["status"].toString()  // Using "status" for location as per the previous logic

            // Add the inflated card to the container
            cardContainer.addView(laporanCardView)
        }
    }

    // Helper function to show toast messages
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
