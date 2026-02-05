package com.example.bullback.ui.splashscreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bullback.R
import com.example.bullback.databinding.FragmentProfileBinding
import com.example.bullback.databinding.FragmentSplashScreenBinding
import com.example.bullback.ui.login.LoginFragment
import com.example.bullback.ui.market.MarketFragment
import com.example.bullback.ui.signup.SignupFragment

class SplashScreen : Fragment() {
    private lateinit var binding: FragmentSplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout with ViewBinding
        binding = FragmentSplashScreenBinding.bind(binding.root)


//        // Setup Logo
//        binding.logoImageView.setImageResource(R.drawable.ic_logo)
//
//        // Setup Title
//        binding.titleTextView.text = getString(R.string.trade_500x_margins_nwith_bullmargin)
//
//        // Setup Features
//        binding.feature1ImageView.setImageResource(R.drawable.ic_margin)
//        binding.feature1TextView.text = "Up to 500x\nMargin"
//
//        binding.feature2ImageView.setImageResource(R.drawable.ic_brokerage)
//        binding.feature2TextView.text = "Lowest\nBrokerage"
//
//        binding.feature3ImageView.setImageResource(R.drawable.ic_deposit)
//        binding.feature3TextView.text = "24/7 Deposit\nAnd Withdrawal"
//
//        binding.feature4ImageView.setImageResource(R.drawable.ic_globe)
//        binding.feature4TextView.text = "10+ Tradable\nSegments"

        // Buttons
        binding.loginButton.setOnClickListener {
            navigateToFragment(LoginFragment())
            // Handle login click
        }

        binding.registerButton.setOnClickListener {
            navigateToFragment(SignupFragment())
            // Handle register click
        }

        binding.demoButton.setOnClickListener {
            navigateToFragment(MarketFragment())
            // Handle demo click
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}