package com.aduanku

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.aduanku.databinding.ActivityLaporBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat

class LaporActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaporBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaporBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Request location permission if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            getLocation()
        }

        // Submit report when the submit button is clicked
        binding.submitButton.setOnClickListener {
            val description = binding.descriptionEditText.text.toString()
            val jenisLaporan = binding.jenisLaporanSpinner.selectedItem.toString()

            if (latitude != 0.0 && longitude != 0.0) {
                handleReportSubmission(description, jenisLaporan)
            } else {
                Toast.makeText(this, "Please allow location access to submit report", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fetch current location
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.d("LaporActivity", "Location: $latitude, $longitude")
                    binding.locationTextView.text = "Latitude: $latitude, Longitude: $longitude"
                } else {
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("LaporActivity", "Location request failed", e)
            }
    }

    // Handle report submission
    private fun handleReportSubmission(description: String, jenisLaporan: String) {
        if (description.isNotBlank() && jenisLaporan.isNotBlank()) {
            Log.d("LaporActivity", "Report submitted: $jenisLaporan, $description")

            // Ensure that location is set
            if (latitude != 0.0 && longitude != 0.0) {
                saveDataToFirestore(description, jenisLaporan, latitude, longitude)
            } else {
                Toast.makeText(this, "Location is not available", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("LaporActivity", "Description or report type is missing")
            Toast.makeText(this, "Description or report type is missing", Toast.LENGTH_SHORT).show()
        }
    }

    // Save the report data to Firestore
    private fun saveDataToFirestore(description: String, jenisLaporan: String, latitude: Double, longitude: Double) {
        val user = auth.currentUser
        val reportData = hashMapOf(
            "jenisLaporan" to jenisLaporan,
            "description" to description,
            "status" to "menunggu", // Default status is 'menunggu' (waiting)
            "userId" to user?.uid, // Only store the user ID
            "latitude" to latitude,
            "longitude" to longitude
        )

        Log.d("Firestore", "Report Data: $reportData")  // Log report data before submission

        firestore.collection("reports")
            .add(reportData)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
                Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
                Toast.makeText(this, "Failed to submit report", Toast.LENGTH_SHORT).show()
            }
    }

    // Handle permissions result for location access
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation() // Permission granted, fetch the location
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
