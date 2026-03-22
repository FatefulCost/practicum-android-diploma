package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.ActivityRootBinding

class RootActivity : AppCompatActivity() {
    companion object {
        private const val NAVIGATION_SETUP_DELAY_MS = 100L
    }

    private var _binding: ActivityRootBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(NAVIGATION_SETUP_DELAY_MS)
            setupNavigation()
        }
    }

    private fun setupNavigation() {
        if (isFinishing || isDestroyed) return

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (navHostFragment != null && navHostFragment.isAdded) {
            val navController = binding.navHostFragment.findNavController()
            binding.bottomNavigation.setupWithNavController(navController)

            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.vacancyDetailFragment,
                    R.id.filterFragment,
                    R.id.workLocationFragment,
                    R.id.countrySelectionFragment,
                    R.id.regionSelectionFragment,
                    R.id.industrySelectionFragment -> {
                        binding.bottomNavigation.visibility = View.GONE
                    }
                    else -> {
                        binding.bottomNavigation.visibility = View.VISIBLE
                    }
                }
            }
        } else {
            binding.root.postDelayed({
                setupNavigation()
            }, NAVIGATION_SETUP_DELAY_MS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
