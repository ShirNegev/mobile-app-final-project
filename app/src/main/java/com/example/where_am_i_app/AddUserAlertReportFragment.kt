package com.example.where_am_i_app

import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import ch.hsr.geohash.GeoHash
import com.example.where_am_i_app.databinding.FragmentAddUserAlertReportBinding
import com.example.where_am_i_app.model.AuthManager
import com.example.where_am_i_app.model.Model
import com.example.where_am_i_app.model.UserAlertReport
import com.example.where_am_i_app.utils.getLocationFromGeoHash
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import java.time.Instant
import java.util.Locale

class AddUserAlertReportFragment : Fragment() {
    private var binding: FragmentAddUserAlertReportBinding? = null
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var didSetImage = false
    private var title: String? = null
    private var userAlertReportId: String? = null
    private var userAlertReport: UserAlertReport? = null
    private var geoHashLocation: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.let { AddUserAlertReportFragmentArgs.fromBundle(it).alertTitle }
        userAlertReportId =
            arguments?.let { AddUserAlertReportFragmentArgs.fromBundle(it).userAlertReportId }

        setHasOptionsMenu(true)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddUserAlertReportBinding.inflate(layoutInflater, container, false)
        if (userAlertReportId == null) {
            binding?.textViewAlertTitle?.text = title
        } else {
            Model.shared.getUserAlertReportById(userAlertReportId!!) {
                userAlertReport = it
                geoHashLocation = it?.geohashLocation
                binding?.textViewAlertTitle?.text = userAlertReport?.alertTitle
                binding?.editTextMessage?.setText(userAlertReport?.text)
                showReportImage()
                showLocation()
            }
        }

        binding?.buttonCancelReport?.setOnClickListener(::onCancelClicked)
        binding?.buttonSubmitReport?.setOnClickListener(::onSaveClicked)

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
                if (bitmap != null) {
                    binding?.imageView?.setImageBitmap(bitmap)
                    didSetImage = true
                }
            }

        binding?.takePhotoButton?.setOnClickListener {
            cameraLauncher?.launch(null)
        }

        binding?.buttonAddLocation?.setOnClickListener {
            getCurrentLocation()
        }

        return binding?.root
    }

    private fun onSaveClicked(view: View) {
        val userId = AuthManager.shared.userId

        if (binding?.editTextMessage?.text.toString().trim() == "") {
            Toast.makeText(requireContext(), "Text cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate GeoHash from stored latitude and longitude if available
        geoHashLocation = if (latitude != null && longitude != null) {
            val hash = GeoHash.withCharacterPrecision(latitude!!, longitude!!, 12).toBase32().toString()
            println("Encoded GeoHash: $hash from Lat: $latitude, Lon: $longitude")
            hash
        } else {
            geoHashLocation ?: ""
        }

        val userAlertReport = UserAlertReport(
            id = userAlertReport?.id ?: Model.shared.generateNewAlertReportId(),
            userId = userId,
            text = binding?.editTextMessage?.text.toString().trim(),
            time = userAlertReport?.time ?: Instant.now().toEpochMilli(),
            geohashLocation = geoHashLocation.toString(),
            alertTitle = binding?.textViewAlertTitle?.text.toString(),
            reportImageUrl = userAlertReport?.reportImageUrl ?: ""
        )

        showLoading(true)

        if (didSetImage) {
            binding?.imageView?.isDrawingCacheEnabled = true
            binding?.imageView?.buildDrawingCache()
            val bitmap = (binding?.imageView?.drawable as BitmapDrawable).bitmap

            Model.shared.addUserAlertReport(userAlertReport, bitmap) {
                showLoading(false)
                Navigation.findNavController(view).popBackStack()
            }
        } else {
            Model.shared.addUserAlertReport(userAlertReport, null) {
                showLoading(false)
                Navigation.findNavController(view).popBackStack()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    latitude = it.latitude
                    longitude = it.longitude

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]

                        binding?.textViewLocation?.text = "Location: ${address.thoroughfare ?: "Unknown Street"}, " +
                                "${address.locality ?: "Unknown City"}, ${address.countryName ?: "Unknown Country"}"
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Lat: ${it.latitude}, Lon: ${it.longitude}\nLocation details not found",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } ?: Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.addProgressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showReportImage() {
        if (!userAlertReport?.reportImageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(userAlertReport?.reportImageUrl)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(binding?.imageView)
        } else {
            binding?.imageView?.setImageResource(R.drawable.image_placeholder)
            showLoading(false)
        }
    }

    private fun showLocation() {
        if (!userAlertReport?.geohashLocation.isNullOrEmpty()) {
            binding?.textViewLocation?.text = "Location: ${getLocationFromGeoHash(userAlertReport?.geohashLocation, requireContext())}"
            binding?.buttonAddLocation?.text = "Update Location"
        } else {
            binding?.textViewLocation?.text = "No Location Added."
            binding?.buttonAddLocation?.text = "Add Location"
        }
    }

    private fun onCancelClicked(view: View) {
        Navigation.findNavController(view).popBackStack()
    }
}