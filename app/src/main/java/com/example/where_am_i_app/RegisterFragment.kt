package com.example.where_am_i_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.where_am_i_app.databinding.FragmentRegisterBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {
    private var binding: FragmentRegisterBinding? = null
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        binding?.registerButton?.setOnClickListener {
            attemptRegister()
        }

        binding?.registerLoginText?.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        return binding?.root
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

        db.collection("users").document(userId).set(user.toMap())
            .addOnSuccessListener {
                showLoading(false)
                navigateToMainApp()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(context, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
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