package com.example.where_am_i_app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.where_am_i_app.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var binding: FragmentSplashBinding? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(inflater, container, false)

        handler.postDelayed({
            checkAuthenticationStatus()
        }, 2500)

        AuthManager.shared.signOut()
        return binding?.root
    }

    private fun checkAuthenticationStatus() {
        if (AuthManager.shared.isSignedIn) {
            Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_FeedFragment)
        } else {
            Navigation.findNavController(requireView()).navigate(R.id.action_splashFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        binding = null
    }
}