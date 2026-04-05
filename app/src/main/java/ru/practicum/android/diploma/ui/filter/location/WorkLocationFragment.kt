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
    private var _binding: FragmentWorkLocationBinding? = null
    private val binding get() = _binding!!
    private val filterViewModel: FilterViewModel by viewModel()

    // Локальные переменные для хранения выбранных значений
    private var selectedCountryId: Int? = null
    private var selectedCountryName: String? = null
    private var selectedRegionId: Int? = null
    private var selectedRegionName: String? = null

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

        // Слушаем результат выбора страны
        parentFragmentManager.setFragmentResultListener("country_selection", viewLifecycleOwner) { _, bundle ->
            val countryId = bundle.getInt("selectedCountryId", -1)
            val countryName = bundle.getString("selectedCountryName")
            if (countryId != -1) {
                selectedCountryId = countryId
                selectedCountryName = countryName
                // При выборе новой страны сбрасываем регион
                selectedRegionId = null
                selectedRegionName = null
                updateDisplay()
                // Сохраняем только страну (регион сбрасываем)
                filterViewModel.updateLocation(
                    countryId = selectedCountryId,
                    countryName = selectedCountryName,
                    regionId = null,
                    regionName = null
                )
            }
        }

        // Слушаем результат выбора региона
        parentFragmentManager.setFragmentResultListener("region_selection", viewLifecycleOwner) { _, bundle ->
            val regionId = bundle.getInt("selectedRegionId", -1)
            val regionName = bundle.getString("selectedRegionName")
            if (regionId != -1) {
                selectedRegionId = regionId
                selectedRegionName = regionName
                updateDisplay()
                // Сохраняем и страну, и регион
                filterViewModel.updateLocation(
                    countryId = selectedCountryId,
                    countryName = selectedCountryName,
                    regionId = selectedRegionId,
                    regionName = selectedRegionName
                )
            }
        }

        // Загружаем сохраненные значения
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
    }

    private fun setupUI() {
        binding.layoutCountry.setOnClickListener {
            navigateToCountrySelection()
        }

        binding.layoutRegion.setOnClickListener {
            if (selectedCountryId != null && selectedCountryId != -1) {
                navigateToRegionSelection()
            } else {
                binding.tvRegionValue.text = "Сначала выберите страну"
            }
        }

        binding.btnSelect.setOnClickListener {
            // Возвращаем результат в FilterFragment
            val bundle = Bundle().apply {
                putInt("countryId", selectedCountryId ?: -1)
                putString("countryName", selectedCountryName)
                putInt("regionId", selectedRegionId ?: -1)
                putString("regionName", selectedRegionName)
            }
            parentFragmentManager.setFragmentResult("work_location", bundle)
            findNavController().popBackStack()
        }
    }

    private fun observeFilters() {
        filterViewModel.filterSettings.onEach { settings ->
            // Обновляем локальные переменные из ViewModel (для синхронизации)
            if (settings.countryId != selectedCountryId || settings.countryName != selectedCountryName) {
                selectedCountryId = settings.countryId
                selectedCountryName = settings.countryName
            }
            if (settings.regionId != selectedRegionId || settings.regionName != selectedRegionName) {
                selectedRegionId = settings.regionId
                selectedRegionName = settings.regionName
            }
            updateDisplay()
        }.launchIn(lifecycleScope)
    }

    private fun updateDisplay() {
        // Обновляем отображение страны
        if (!selectedCountryName.isNullOrBlank() && selectedCountryId != null && selectedCountryId != -1) {
            binding.tvCountryValue.text = selectedCountryName
        } else {
            binding.tvCountryValue.text = getString(R.string.not_selected)
        }

        // Обновляем отображение региона
        if (!selectedRegionName.isNullOrBlank() && selectedRegionId != null && selectedRegionId != -1) {
            binding.tvRegionValue.text = selectedRegionName
        } else {
            binding.tvRegionValue.text = getString(R.string.not_selected)
        }
    }

    private fun navigateToCountrySelection() {
        findNavController().navigate(R.id.action_workLocationFragment_to_countrySelectionFragment)
    }

    private fun navigateToRegionSelection() {
        val bundle = Bundle().apply {
            putInt("countryId", selectedCountryId ?: -1)
            putString("countryName", selectedCountryName ?: "")
        }
        findNavController().navigate(R.id.action_workLocationFragment_to_regionSelectionFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
