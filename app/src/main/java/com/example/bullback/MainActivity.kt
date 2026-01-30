package com.example.bullback

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bullback.data.model.websocket.AppWebSocketManager
import com.example.bullback.databinding.ActivityMainBinding
import com.example.bullback.ui.login.LoginFragment
import com.example.bullback.ui.market.MarketFragment
import com.example.bullback.ui.orders.OrdersFragment
import com.example.bullback.ui.positions.PositionsFragment
import com.example.bullback.ui.profile.ProfileFragment
import com.example.bullback.ui.signup.SignupFragment
import com.example.bullback.ui.watchlist.WatchlistFragment
import com.example.bullback.data.repository.AuthRepository


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide bottom nav initially
        showBottomNav(false)

        if (savedInstanceState == null) {
            loadFragment(LoginFragment(), clearBackStack = true)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_market -> {
                    startMarketWebSocket()
                    loadFragment(MarketFragment())
                    true
                }

                R.id.nav_watchlist -> {
                    loadFragment(WatchlistFragment())
                    true
                }

                R.id.nav_orders -> {
                    loadFragment(OrdersFragment())
                    true
                }

                R.id.nav_positions -> {
                    loadFragment(PositionsFragment())
                    true
                }

                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }

                else -> false
            }
        }
    }

    /* ------------------ Navigation Helpers ------------------ */

    fun navigateToLogin() {
        showBottomNav(false)
        loadFragment(LoginFragment(), clearBackStack = true)
    }

    fun navigateToSignup() {
        showBottomNav(false)
        loadFragment(SignupFragment())
    }

    fun navigateToMainScreen() {
        showBottomNav(true)
        binding.bottomNavigation.selectedItemId = R.id.nav_market
        startMarketWebSocket()
        loadFragment(MarketFragment(), clearBackStack = true)
    }

    /* ------------------ WebSocket Start ------------------ */

    private fun startMarketWebSocket() {
        val token = AuthRepository.getInstance(this).getTokenSync()
        if (!token.isNullOrEmpty()) {
            AppWebSocketManager.connect(token)
        }
    }

    /* ------------------ Fragment Loader ------------------ */

    private fun loadFragment(
        fragment: Fragment,
        clearBackStack: Boolean = false
    ) {
        if (clearBackStack) {
            supportFragmentManager.popBackStack(
                null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /* ------------------ Bottom Nav ------------------ */

    fun showBottomNav(show: Boolean) {
        binding.bottomNavigation.visibility =
            if (show) View.VISIBLE else View.GONE
    }
}
