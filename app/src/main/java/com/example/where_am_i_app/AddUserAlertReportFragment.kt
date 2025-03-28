package com.example.where_am_i_app

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.Navigation
import com.example.where_am_i_app.databinding.FragmentAddUserAlertReportBinding
import com.example.where_am_i_app.model.AuthManager
import com.example.where_am_i_app.model.Model
import com.example.where_am_i_app.model.UserAlertReport
import com.example.where_am_i_app.utils.getLocationFromGeoHash
import com.squareup.picasso.Picasso
import java.time.Instant

class AddUserAlertReportFragment : Fragment() {
    private var binding: FragmentAddUserAlertReportBinding? = null
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var didSetImage = false
    private var title: String? = null
    private var userAlertReportId: String? = null
    private var userAlertReport: UserAlertReport? = null
    private var geoHashLocation: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.let { AddUserAlertReportFragmentArgs.fromBundle(it).alertTitle }
        userAlertReportId =
            arguments?.let { AddUserAlertReportFragmentArgs.fromBundle(it).userAlertReportId }

        setHasOptionsMenu(true)
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
        binding?.buttonAddLocation?.setOnClickListener(::onLocationClicked)

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

        return binding?.root
    }

    private fun onSaveClicked(view: View) {
        val userId = AuthManager.shared.userId

        if (binding?.editTextMessage?.text.toString().trim() == "") {
            Toast.makeText(requireContext(), "Text cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val userAlertReport = UserAlertReport(
            id = userAlertReport?.id ?: Model.shared.generateNewAlertReportId(),
            userId = userId,
            text = binding?.editTextMessage?.text.toString().trim(),
            time = userAlertReport?.time ?: Instant.now().toEpochMilli(),
            geohashLocation = geoHashLocation ?: "",
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
        if(!userAlertReport?.geohashLocation.isNullOrEmpty()) {
            binding?.textViewLocation?.text = "Location: ${getLocationFromGeoHash(userAlertReport?.geohashLocation)}"
            binding?.buttonAddLocation?.text = "Update Location"
        } else {
            binding?.textViewLocation?.text = "Location: no location detected"
            binding?.buttonAddLocation?.text = "Add Location"
        }
    }

    private fun onLocationClicked(view: View) {
        //TODO: ROTEM
        geoHashLocation = "";
        binding?.textViewLocation?.text = "Location: ${getLocationFromGeoHash(geoHashLocation)}"
        binding?.buttonAddLocation?.text = "Update Location"
    }

    private fun onCancelClicked(view: View) {
        Navigation.findNavController(view).popBackStack()
    }
}