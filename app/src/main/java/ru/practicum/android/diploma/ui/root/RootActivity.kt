package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.ActivityRootBinding

class RootActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.root.post {
            setupNavigation()
        }
    }

    private fun setupNavigation() {

        val navController = try {
            binding.navHostFragment.findNavController()
        } catch (e: Exception) {

            binding.root.post { setupNavigation() }
            return
        }


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
    }
}
