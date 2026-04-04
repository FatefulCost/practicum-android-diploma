package ru.practicum.android.diploma.ui.filter.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentWorkLocationBinding

class WorkLocationFragment : Fragment() {

    companion object {
        const val KEY_SELECTED_COUNTRY_ID = "selected_country_id"
        const val KEY_SELECTED_COUNTRY_NAME = "selected_country_name"
        const val KEY_SELECTED_REGION_ID = "selected_region_id"
        const val KEY_SELECTED_REGION_NAME = "selected_region_name"
    }

    private var _binding: FragmentWorkLocationBinding? = null
    private val binding get() = _binding!!

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
        setupUI()
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
            findNavController().navigate(R.id.action_workLocationFragment_to_countrySelectionFragment)
        }

        binding.layoutRegion.setOnClickListener {
            val args = Bundle().apply { putInt("countryId", selectedCountryId) }
            findNavController().navigate(R.id.action_workLocationFragment_to_regionSelectionFragment, args)
        }

        binding.btnSelect.setOnClickListener {
            findNavController().popBackStack()
        }
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
