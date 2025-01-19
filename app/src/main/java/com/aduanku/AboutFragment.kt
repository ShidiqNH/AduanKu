package com.aduanku

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aduanku.databinding.FragmentAboutBinding  // ViewBinding class

class AboutFragment : Fragment() {

    // Declare the ViewBinding variable
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using ViewBinding
        _binding = FragmentAboutBinding.inflate(inflater, container, false)

        // Set click listeners for the buttons
        binding.githubButton.setOnClickListener {
            openWebsite("https://github.com/ShidiqNH/AduanKu") // Replace with actual GitHub URL
        }

        binding.aduankuDemoButton.setOnClickListener {
            openWebsite("https://www.shidiq.com/aduanku-demo") // Replace with actual Aduanku demo URL
        }

        return binding.root // Return the root view from the ViewBinding object
    }

    private fun openWebsite(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    // Clean up the binding when the fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
