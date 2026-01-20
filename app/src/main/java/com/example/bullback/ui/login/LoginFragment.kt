package com.example.bullback.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bullback.MainActivity
import com.example.bullback.databinding.FragmentLoginBinding
import com.example.bullback.R
import com.example.bullback.ui.signup.SignupFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        // Back button
        binding.backArrow.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Login button
        binding.bntSignin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                viewModel.onUsernameChange(username)
                viewModel.onPasswordChange(password)
                viewModel.login()
            } else {
                showSnackbar("Please fill in all fields")
            }
        }


    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: LoginState) {
        // Loading state
        binding.bntSignin.isEnabled = !state.isLoading
        binding.bntSignin.text = if (state.isLoading) "Logging in..." else "SIGN IN"

        // Error handling
        state.error?.let { error ->
            showSnackbar(error)
            viewModel.clearError()
        }

        // Success handling
        if (state.isSuccess) {
            showToast("Login successful!")
            navigateToHomeActivity()
        }
    }

    private fun navigateToHomeActivity() {
        (requireActivity() as? MainActivity)?.navigateToMainScreen()

    }

    private fun openSignupFragment() {
        // Replace with SignupFragment
        val signupFragment = SignupFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, signupFragment)
            .addToBackStack("signup")
            .commit()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}