package com.example.bullback.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bullback.R
import com.example.bullback.data.model.auth.User
import com.example.bullback.databinding.FragmentProfileBinding
import com.example.bullback.ui.profile.marginSettings.MarginSettingsFragment
import com.example.bullback.ui.profile.tradelogs.TradeLogsFragment
import com.example.bullback.ui.profile.wallet.WalletFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentProfileBinding.bind(view)

        // Load profile data
        viewModel.loadProfile()

        //Wallet Screen navigation
        binding.option1.setOnClickListener{
            navigateToFragment(WalletFragment())
        }

        // Margin Settings navigation
        binding.option3.setOnClickListener {
            navigateToFragment(MarginSettingsFragment())
        }

        //trade logs navigation
        binding.option4.setOnClickListener {
            navigateToFragment(TradeLogsFragment())
        }
        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                androidx.lifecycle.Lifecycle.State.STARTED
            ) {
                viewModel.state.collect { state ->

                    // Error handling
                    state.error?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }

                    // Profile data
                    state.user?.let { userResponse ->
                        val user = User(
                            id = userResponse.id,
                            username = userResponse.username,
                            email = userResponse.email,
                            createdAt = userResponse.createdAt
                        )
                        bindUserData(user)
                    }
                }
            }
        }
    }

    private fun bindUserData(user: User) {
        binding.tvUsername.text = user.username
        binding.tvInitial.text = user.username.firstOrNull()?.toString() ?: "?"

        binding.tvMemberSince.text =
            "Member since ${formatDate(user.createdAt)}"
    }

    private fun formatDate(date: String?): String {
        if (date.isNullOrEmpty()) return "N/A"

        return try {
            val input = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            )
            val output = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            output.format(input.parse(date)!!)
        } catch (e: Exception) {
            "N/A"
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
