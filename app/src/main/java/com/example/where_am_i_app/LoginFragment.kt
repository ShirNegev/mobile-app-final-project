package com.example.where_am_i_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.where_am_i_app.databinding.FragmentLoginBinding
import com.example.where_am_i_app.model.AuthManager

class LoginFragment : Fragment() {
    private var binding: FragmentLoginBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Check if user is already signed in
        if (AuthManager.shared.isSignedIn) {
            navigateToMainApp()
            return binding?.root
        }

        binding?.loginLoginButton?.setOnClickListener {
            attemptLogin()
        }

        binding?.loginRegisterText?.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_RegisterFragment)
        }

        return binding?.root
    }

    private fun attemptLogin() {
        val email = binding?.loginEmailEditText?.text.toString().trim()
        val password = binding?.loginPasswordEditText?.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        AuthManager.shared.signIn(email, password) { success, message ->
            showLoading(false)

            if (success) {
                navigateToMainApp()
            } else {
                Toast.makeText(context, message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMainApp() {
        Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_FeedFragment)
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.loginProgressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding?.loginLoginButton?.isEnabled = !isLoading
        binding?.loginEmailEditText?.isEnabled = !isLoading
        binding?.loginPasswordEditText?.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}