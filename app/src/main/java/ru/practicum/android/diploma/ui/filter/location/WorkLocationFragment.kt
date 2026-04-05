package ru.practicum.android.diploma.ui.filter.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentWorkLocationBinding
import ru.practicum.android.diploma.ui.filter.FilterViewModel

class WorkLocationFragment : Fragment() {

    companion object {
        const val KEY_SELECTED_COUNTRY_ID = "selected_country_id"
        const val KEY_SELECTED_COUNTRY_NAME = "selected_country_name"
        const val KEY_SELECTED_REGION_ID = "selected_region_id"
        const val KEY_SELECTED_REGION_NAME = "selected_region_name"
    }

    private var _binding: FragmentWorkLocationBinding? = null
    private val binding get() = _binding!!
    private val filterViewModel: FilterViewModel by viewModel()

    private var selectedCountryId: Int? = null
    private var selectedCountryName: String? = null
    private var selectedRegionId: Int? = null
    private var selectedRegionName: String? = null

    private var selectedCountryId: Int = -1
    private var selectedCountryName: String = ""
    private var selectedRegionId: Int = -1
    private var selectedRegionName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener("country_selection", viewLifecycleOwner) { _, bundle ->
            val countryId = bundle.getInt("selectedCountryId", -1)
            val countryName = bundle.getString("selectedCountryName")
            if (countryId != -1) {
                selectedCountryId = countryId
                selectedCountryName = countryName
                selectedRegionId = null
                selectedRegionName = null
                updateDisplay()
            }
        }

        parentFragmentManager.setFragmentResultListener("region_selection", viewLifecycleOwner) { _, bundle ->
            val regionId = bundle.getInt("selectedRegionId", -1)
            val regionName = bundle.getString("selectedRegionName")
            if (regionId != -1) {
                selectedRegionId = regionId
                selectedRegionName = regionName
                updateDisplay()
            }
        }

        loadSavedLocation()
        setupUI()
        observeFilters()
        updateDisplay()
    }

    private fun loadSavedLocation() {
        val settings = filterViewModel.filterSettings.value
        selectedCountryId = settings.countryId
        selectedCountryName = settings.countryName
        selectedRegionId = settings.regionId
        selectedRegionName = settings.regionName
        observeNavigationResults()
        restoreArgumentsIfNeeded()
    }

    private fun restoreArgumentsIfNeeded() {
        val args = arguments
        if (args != null) {
            val countryId = args.getInt("selectedCountryId", -1)
            val countryName = args.getString("selectedCountryName", "")
            val regionId = args.getInt("selectedRegionId", -1)
            val regionName = args.getString("selectedRegionName", "")
            if (countryId != -1) {
                selectedCountryId = countryId
                selectedCountryName = countryName ?: ""
            }
            if (regionId != -1) {
                selectedRegionId = regionId
                selectedRegionName = regionName ?: ""
            }
            updateLocationUI()
        }
    }

    private fun setupUI() {
        updateLocationUI()

        binding.layoutCountry.setOnClickListener {
            navigateToCountrySelection()
        }

        binding.layoutRegion.setOnClickListener {
            if (selectedCountryId != null && selectedCountryId != -1) {
                navigateToRegionSelection()
            } else {
                binding.tvRegionValue.text = "Сначала выберите страну"
            }
            findNavController().navigate(R.id.action_workLocationFragment_to_countrySelectionFragment)
        }

        binding.layoutRegion.setOnClickListener {
            val args = Bundle().apply { putInt("countryId", selectedCountryId) }
            findNavController().navigate(R.id.action_workLocationFragment_to_regionSelectionFragment, args)
        }

        binding.btnSelect.setOnClickListener {
            saveLocationAndReturn()
        }
    }

    private fun observeFilters() {
        filterViewModel.filterSettings.onEach { settings ->
            selectedCountryId = settings.countryId
            selectedCountryName = settings.countryName
            selectedRegionId = settings.regionId
            selectedRegionName = settings.regionName
            updateDisplay()
        }.launchIn(lifecycleScope)
    }

    private fun updateDisplay() {
        if (!selectedCountryName.isNullOrBlank() && selectedCountryId != null && selectedCountryId != -1) {
            binding.tvCountryValue.text = selectedCountryName
        } else {
            binding.tvCountryValue.text = getString(R.string.not_selected)
        }

        if (!selectedRegionName.isNullOrBlank() && selectedRegionId != null && selectedRegionId != -1) {
            binding.tvRegionValue.text = selectedRegionName
        } else {
            binding.tvRegionValue.text = getString(R.string.not_selected)
        }
    }

    private fun navigateToCountrySelection() {
        val action = ru.practicum.android.diploma.R.id.action_workLocationFragment_to_countrySelectionFragment
        findNavController().navigate(action)
    }

    private fun navigateToRegionSelection() {
        val bundle = Bundle().apply {
            putInt("countryId", selectedCountryId ?: -1)
            putString("countryName", selectedCountryName ?: "")
        }
        val action = ru.practicum.android.diploma.R.id.action_workLocationFragment_to_regionSelectionFragment
        findNavController().navigate(action, bundle)
    }

    private fun saveLocationAndReturn() {
        val bundle = Bundle().apply {
            putInt("countryId", selectedCountryId ?: -1)
            putString("countryName", selectedCountryName)
            putInt("regionId", selectedRegionId ?: -1)
            putString("regionName", selectedRegionName)
        }
        parentFragmentManager.setFragmentResult("work_location", bundle)
        findNavController().popBackStack()
    }

    private fun observeNavigationResults() {
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle

        savedStateHandle?.getLiveData<Int>(KEY_SELECTED_COUNTRY_ID)?.observe(viewLifecycleOwner) { id ->
            if (id != null && id != -1) {
                selectedCountryId = id
                selectedRegionId = -1
                selectedRegionName = ""
                updateLocationUI()
            }
        }

        savedStateHandle?.getLiveData<String>(KEY_SELECTED_COUNTRY_NAME)?.observe(viewLifecycleOwner) { name ->
            if (!name.isNullOrEmpty()) {
                selectedCountryName = name
                updateLocationUI()
            }
        }

        savedStateHandle?.getLiveData<Int>(KEY_SELECTED_REGION_ID)?.observe(viewLifecycleOwner) { id ->
            if (id != null && id != -1) {
                selectedRegionId = id
                updateLocationUI()
            }
        }

        savedStateHandle?.getLiveData<String>(KEY_SELECTED_REGION_NAME)?.observe(viewLifecycleOwner) { name ->
            if (!name.isNullOrEmpty()) {
                selectedRegionName = name
                updateLocationUI()
            }
        }
    }

    private fun updateLocationUI() {
        binding.tvCountryValue.text = if (selectedCountryName.isNotEmpty()) {
            selectedCountryName
        } else {
            getString(ru.practicum.android.diploma.R.string.not_selected)
        }

        binding.tvRegionValue.text = if (selectedRegionName.isNotEmpty()) {
            selectedRegionName
        } else {
            getString(ru.practicum.android.diploma.R.string.not_selected)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
