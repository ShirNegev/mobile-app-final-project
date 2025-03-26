package com.example.where_am_i_app

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

class AddUserAlertReportFragment : Fragment() {
    private var binding: FragmentAddUserAlertReportBinding? = null
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
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
            binding?.imageView?.setImageBitmap(bitmap)
        }

        binding?.takePhotoButton?.setOnClickListener {
            cameraLauncher?.launch(null)
        }

        return binding?.root
    }

    private fun onSaveClicked(view: View) {

        Navigation.findNavController(view).popBackStack()
    }
}