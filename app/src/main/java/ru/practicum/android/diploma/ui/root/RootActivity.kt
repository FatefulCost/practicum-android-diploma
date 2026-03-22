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

    private lateinit var binding: ActivityRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

                lifecycleScope.launch {
            delay(100)
            setupNavigation()
        }
    }

    private fun setupNavigation() {

        if (!isFinishing && !isDestroyed) {
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
                }, 100)
            }
        }
    }
}
