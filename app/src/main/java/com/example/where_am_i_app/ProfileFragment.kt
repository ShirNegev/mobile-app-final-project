package com.example.where_am_i_app

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.squareup.picasso.Picasso
import android.util.Log
import com.example.where_am_i_app.databinding.FragmentProfileBinding
import com.example.where_am_i_app.model.AuthManager
import com.example.where_am_i_app.model.Model

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    var previousBitmap: Bitmap? = null
    private var user: User? = null
    private var isUploadingImage = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        setupCameraLauncher()
        setupClickListeners()
        fetchUserProfile()

        return binding?.root
    }

    private fun setupClickListeners() {
        binding?.profileImageView?.setOnClickListener {
            cameraLauncher?.launch(null)
        }

        binding?.changeNameText?.setOnClickListener {

        }

        binding?.logoutButton?.setOnClickListener {
            logout()
        }
    }

    private fun setupCameraLauncher() {
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.profile)
        previousBitmap = (drawable as BitmapDrawable).bitmap
        cameraLauncher = registerForActivityResult((ActivityResultContracts.TakePicturePreview())) { bitmap ->
            if (bitmap != null) {
                previousBitmap = bitmap
                binding?.profileImageView?.setImageBitmap(bitmap)

                uploadProfilePicture(bitmap)
            } else {
                binding?.profileImageView?.setImageBitmap(previousBitmap)
            }
        }
    }

    private fun uploadProfilePicture(bitmap: Bitmap) {
        if (isUploadingImage) return

        isUploadingImage = true

        if (user != null) {
            Model.shared.addUser(user = user!!, bitmap) {}
        }
    }

    private fun fetchUserProfile() {
        val userId = AuthManager.shared.userId

        Model.shared.getUserById(userId,
            { fetchedUser ->
                user = fetchedUser

                binding?.profileNameText?.text = user?.name ?: ""

                if (!user?.profileImageUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(user?.profileImageUrl)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(binding?.profileImageView, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                Log.d("TAG", "Loaded profile image successfully")
                            }

                            override fun onError(e: Exception?) {
                                Log.e("TAG", "Loading profile image failed: ${e?.message}")
                            }
                        })
                } else {
                    binding?.profileImageView?.setImageResource(R.drawable.profile)
                }
            },
            { error ->
                Toast.makeText(context, "Error loading profile: ${error}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun logout() {
        AuthManager.shared.signOut()
        Navigation.findNavController(requireView()).navigate(R.id.action_profileFragment_to_LoginFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}