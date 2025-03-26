package com.example.where_am_i_app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.where_am_i_app.databinding.FragmentRegisterBinding
import android.net.Uri
import android.view.Menu
import android.view.MenuInflater
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.where_am_i_app.model.AuthManager
import com.example.where_am_i_app.model.Model

class RegisterFragment : Fragment() {
    private var binding: FragmentRegisterBinding? = null
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var didSetProfileImage = false
    var previousBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)

        setupCameraLauncher()

        binding?.registerProfileImage?.setOnClickListener {
            cameraLauncher?.launch(null)
        }

        binding?.registerButton?.setOnClickListener {
            attemptRegister()
        }

        binding?.registerLoginText?.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        return binding?.root
    }

    private fun setupCameraLauncher() {
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.profile)
        previousBitmap = (drawable as BitmapDrawable).bitmap
        cameraLauncher = registerForActivityResult((ActivityResultContracts.TakePicturePreview())) { bitmap ->
            if (bitmap != null) {
                previousBitmap = bitmap
                binding?.registerProfileImage?.setImageBitmap(bitmap)
                didSetProfileImage = true
            } else {
                binding?.registerProfileImage?.setImageBitmap(previousBitmap)
                didSetProfileImage = true
            }
        }
    }

    private fun attemptRegister() {
        val name = binding?.registerNameEditText?.text.toString().trim()
        val email = binding?.registerEmailEditText?.text.toString().trim()
        val password = binding?.registerPasswordEditText?.text.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill all fields !!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(context, "Password must be at least 6 characters !!", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        AuthManager.shared.signUp(email, password) { success, message ->
            if (success) {
                saveUserProfile(name)
            } else {
                showLoading(false)
                Toast.makeText(context, message ?: "Registration failed :(", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserProfile(name: String) {
        val userId = AuthManager.shared.userId

        val user = User(
            id = userId,
            name = name,
            profileImageUrl = ""
        )

        if (didSetProfileImage) {
            binding?.registerProfileImage?.isDrawingCacheEnabled = true
            binding?.registerProfileImage?.buildDrawingCache()
            val bitmap = (binding?.registerProfileImage?.drawable as BitmapDrawable).bitmap

            Model.shared.addUser(user, bitmap) {
                showLoading(false)
                navigateToMainApp()
            }
        } else {
            Model.shared.addUser(user, null) {
                showLoading(false)
                navigateToMainApp()
            }
        }
    }

    private fun navigateToMainApp() {
        Navigation.findNavController(requireView()).navigate(R.id.action_registerFragment_to_FeedFragment)
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.registerProgressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding?.registerButton?.isEnabled = !isLoading
        binding?.registerNameEditText?.isEnabled = !isLoading
        binding?.registerEmailEditText?.isEnabled = !isLoading
        binding?.registerPasswordEditText?.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}