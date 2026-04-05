package ru.practicum.android.diploma.ui.filter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding

class FilterFragment : Fragment() {
    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FilterViewModel by viewModel()

    companion object {
        private const val TAG = "FilterFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener("work_location", viewLifecycleOwner) { _, bundle ->
            val countryId = bundle.getInt("countryId", -1)
            val countryName = bundle.getString("countryName")
            val regionId = bundle.getInt("regionId", -1)
            val regionName = bundle.getString("regionName")

            viewModel.updateLocation(
                countryId = if (countryId != -1) countryId else null,
                countryName = countryName,
                regionId = if (regionId != -1) regionId else null,
                regionName = regionName
            )
        }

        setupUI()
        observeViewModel()
        loadSavedFilters()
    }

    private fun loadSavedFilters() {
        val settings = viewModel.filterSettings.value
        Log.d(TAG, "Loading saved filters: $settings")

        settings.salary?.let {
            binding.etSalary.setText(it.toString())
        }
        binding.cbHideWithoutSalary.isChecked = settings.onlyWithSalary
        updateWorkLocationDisplay(settings.countryName, settings.regionName)

        if (!settings.industryName.isNullOrBlank()) {
            binding.tvIndustryValue.text = settings.industryName
        } else {
            binding.tvIndustryValue.text = getString(R.string.not_selected)
        }
    }

    private fun setupUI() {
        binding.btnApply.setOnClickListener {
            applyFilters()
        }

        binding.btnReset.setOnClickListener {
            resetFilters()
        }

        binding.layoutWorkLocation.setOnClickListener {
            navigateToWorkLocation()
        }

        binding.layoutIndustry.setOnClickListener {
            navigateToIndustrySelection()
        }
    }

    private fun observeViewModel() {
        viewModel.filterSettings.onEach { settings ->
            Log.d(TAG, "Settings updated: $settings")
            updateWorkLocationDisplay(settings.countryName, settings.regionName)

            if (!settings.industryName.isNullOrBlank()) {
                binding.tvIndustryValue.text = settings.industryName
            } else {
                binding.tvIndustryValue.text = getString(R.string.not_selected)
            }
        }.launchIn(lifecycleScope)
    }

    private fun updateWorkLocationDisplay(countryName: String?, regionName: String?) {
        val locationText = buildString {
            if (!regionName.isNullOrBlank()) {
                append(regionName)
            } else if (!countryName.isNullOrBlank()) {
                append(countryName)
            }
        }
        val displayText = if (locationText.isNotEmpty()) {
            locationText
        } else {
            getString(R.string.not_selected)
        }
        binding.tvWorkLocationValue.text = displayText
    }

    private fun navigateToWorkLocation() {
        val action = R.id.action_filterFragment_to_workLocationFragment
        findNavController().navigate(action)
    }

    private fun navigateToIndustrySelection() {
        val action = R.id.action_filterFragment_to_industrySelectionFragment
        findNavController().navigate(action)
    }

    private fun applyFilters() {
        val salaryText = binding.etSalary.text?.toString()
        val salary = if (!salaryText.isNullOrBlank()) salaryText.toIntOrNull() else null

        viewModel.updateSalary(salary)
        viewModel.updateOnlyWithSalary(binding.cbHideWithoutSalary.isChecked)

        Log.d(
            TAG,
            "Applying filters - Salary: $salary, OnlyWithSalary: ${binding.cbHideWithoutSalary.isChecked}"
        )
        Log.d(TAG, "Current settings after apply: ${viewModel.filterSettings.value}")

        Toast.makeText(requireContext(), "Фильтры применены", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun resetFilters() {
        binding.etSalary.text?.clear()
        binding.cbHideWithoutSalary.isChecked = false
        viewModel.resetFilters()
        Toast.makeText(requireContext(), "Фильтры сброшены", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
