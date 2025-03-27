package com.example.where_am_i_app

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.Navigation
import com.example.where_am_i_app.databinding.FragmentAddUserAlertReportBinding
import com.example.where_am_i_app.model.AuthManager
import com.example.where_am_i_app.model.Model
import com.example.where_am_i_app.model.UserAlertReport
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class AddUserAlertReportFragment : Fragment() {
    private var binding: FragmentAddUserAlertReportBinding? = null
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var didSetProfileImage = false
    var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.let { AddUserAlertReportFragmentArgs.fromBundle(it).alertTitle }
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
        binding?.textViewAlertTitle?.text = title
        binding?.buttonSubmitReport?.setOnClickListener(::onSaveClicked)

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                binding?.imageView?.setImageBitmap(bitmap)
                didSetProfileImage = true
            } else {
                didSetProfileImage = true
            }
        }

        binding?.takePhotoButton?.setOnClickListener {
            cameraLauncher?.launch(null)
        }

        return binding?.root
    }

    private fun onSaveClicked(view: View) {
        val userId = AuthManager.shared.userId

        val userAlertReport = UserAlertReport(
            id = Model.shared.generateNewAlertReportId(),
            userId = userId,
            text = binding?.editTextMessage?.text.toString().trim(),
            time = Instant.now().toEpochMilli(),
            geohashLocation = "AddLocationGeoHash",
            alertTitle = binding?.textViewAlertTitle?.text.toString().trim(),
            reportImageUrl = ""
        )

        showLoading(true)

        if (didSetProfileImage) {
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
}