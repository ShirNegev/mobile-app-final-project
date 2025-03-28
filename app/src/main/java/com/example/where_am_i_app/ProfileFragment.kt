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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.where_am_i_app.adapter.UserAlertReportAdapter
import com.example.where_am_i_app.databinding.FragmentProfileBinding
import com.example.where_am_i_app.model.AuthManager
import com.example.where_am_i_app.model.Model
import com.example.where_am_i_app.model.UserAlertReport

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    var previousBitmap: Bitmap? = null
    private var user: User? = null
    private var isUploadingImage = false

    private val viewModel: UserAlertReportsViewModel by viewModels()
    private var adapter: UserAlertReportAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.layoutManager = LinearLayoutManager(context)
        binding?.profileProgressBar?.visibility = View.VISIBLE

        adapter = UserAlertReportAdapter(null, requireContext())
        adapter?.listener = object : OnUserAlertReportClickListener {
            override fun onEditClick(userAlertReport: UserAlertReport?) {
                userAlertReport?.let {
                    val action = ProfileFragmentDirections.actionProfileFragmentToAddUserAlertReportFragment(null, it.id)
                    binding?.root?.let {
                        Navigation.findNavController(it).navigate(action)
                    }
                }
            }

            override fun onDeleteClick(userAlertReport: UserAlertReport?) {
                viewModel.deleteUserAlertReport(userAlertReport) { success: Boolean ->
                    if (success) {
                        Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding?.recyclerView?.adapter = adapter

        viewModel.userAlertReportById.observe(viewLifecycleOwner) {
            adapter?.update(it)
            binding?.profileProgressBar?.visibility = View.GONE
        }
        binding?.swipeToRefreshProfileAlerts?.setOnRefreshListener {
            viewModel.refreshAllUserAlertReports()
        }
        Model.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding?.swipeToRefreshProfileAlerts?.isRefreshing = state == Model.LoadingState.LOADING
        }

        setupCameraLauncher()
        setupClickListeners()
        fetchUserProfile()

        return _binding?.root
    }

    private fun setupClickListeners() {
        binding?.profileImageView?.setOnClickListener {
            cameraLauncher?.launch(null)
        }

        binding?.editNameIcon?.setOnClickListener {
            binding?.editProfileNameTextText?.setText(binding?.profileNameText?.text)
            binding?.editProfileNameText?.visibility = View.VISIBLE
            binding?.profileNameText?.visibility = View.GONE
            binding?.editNameIcon?.visibility = View.GONE
            binding?.saveNameButton?.visibility = View.VISIBLE
            binding?.cancelNameButton?.visibility = View.VISIBLE
        }

        binding?.saveNameButton?.setOnClickListener {
            val newName = binding?.editProfileNameTextText?.text?.toString() ?: ""
            if (newName != "") {
                binding?.profileNameText?.text = newName
                val user = user?.copy(name = newName)
                if (user != null) {
                    Model.shared.addUser(user = user) {}
                }
            }

            binding?.editProfileNameText?.visibility = View.GONE
            binding?.profileNameText?.visibility = View.VISIBLE
            binding?.editNameIcon?.visibility = View.VISIBLE
            binding?.saveNameButton?.visibility = View.GONE
            binding?.cancelNameButton?.visibility = View.GONE
        }

        binding?.cancelNameButton?.setOnClickListener {
            binding?.editProfileNameText?.visibility = View.GONE
            binding?.profileNameText?.visibility = View.VISIBLE
            binding?.editNameIcon?.visibility = View.VISIBLE
            binding?.saveNameButton?.visibility = View.GONE
            binding?.cancelNameButton?.visibility = View.GONE
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

                showLoading(true)
                uploadProfilePicture(bitmap)
                showLoading(false)
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

        showLoading(true)

        Model.shared.getUserById(userId,
            { fetchedUser ->
                user = fetchedUser

                binding?.profileNameText?.text = user?.name ?: ""

                if (!user?.profileImageUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(user?.profileImageUrl)
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(binding?.profileImageView, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                Log.d("TAG", "Loaded profile image successfully")
                                showLoading(false)
                            }

                            override fun onError(e: Exception?) {
                                Log.e("TAG", "Loading profile image failed: ${e?.message}")
                                showLoading(false)
                            }
                        })
                } else {
                    binding?.profileImageView?.setImageResource(R.drawable.profile)
                    showLoading(false)
                }
            },
            { error ->
                Toast.makeText(context, "Error loading profile: ${error}", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
        )
    }

    private fun logout() {
        AuthManager.shared.signOut()
        Navigation.findNavController(requireView()).navigate(R.id.action_profileFragment_to_LoginFragment)
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.profileProgressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshAllUserAlertReports()
    }
}