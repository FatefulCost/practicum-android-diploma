package ru.practicum.android.diploma.ui.filter.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.android.ext.android.inject
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentWorkLocationBinding
import ru.practicum.android.diploma.domain.repository.FilterRepository

class WorkLocationFragment : Fragment() {

    companion object {
        const val KEY_SELECTED_COUNTRY_ID = "selected_country_id"
        const val KEY_SELECTED_COUNTRY_NAME = "selected_country_name"
        const val KEY_SELECTED_REGION_ID = "selected_region_id"
        const val KEY_SELECTED_REGION_NAME = "selected_region_name"
    }

    private var _binding: FragmentWorkLocationBinding? = null
    private val binding get() = _binding!!

    private val filterRepository: FilterRepository by inject()
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
        setupToolbar()
        loadSavedSelection()
        setupUI()
        observeNavigationResults()
        updateSelectButtonVisibility()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadSavedSelection() {
        val countryId = filterRepository.loadSavedCountryId()
        val countryName = filterRepository.loadSavedCountryName()
        val regionId = filterRepository.loadSavedRegionId()
        val regionName = filterRepository.loadSavedRegionName()

        if (countryId != null && countryId != -1) {
            selectedCountryId = countryId
            selectedCountryName = countryName ?: ""
        }
        if (regionId != null && regionId != -1) {
            selectedRegionId = regionId
            selectedRegionName = regionName ?: ""
        }

        updateLocationUI()
        updateSelectButtonVisibility()
    }

    private fun setupUI() {
        updateLocationUI()
        updateSelectButtonVisibility()

        // Переход на экран выбора страны
        binding.layoutCountry.setOnClickListener {
            findNavController().navigate(R.id.action_workLocationFragment_to_countrySelectionFragment)
        }

        // Переход на экран выбора региона
        binding.layoutRegion.setOnClickListener {
            if (selectedCountryId != -1) {
                val bundle = Bundle().apply {
                    putInt("selected_country_id", selectedCountryId)
                    putString("selected_country_name", selectedCountryName)
                }
                findNavController().navigate(
                    R.id.action_workLocationFragment_to_regionSelectionFragment,
                    bundle
                )
            }
        }

        // Очистка выбора страны
        binding.ivCountryClear.setOnClickListener {
            selectedCountryId = -1
            selectedCountryName = ""
            selectedRegionId = -1
            selectedRegionName = ""
            updateLocationUI()
            updateSelectButtonVisibility()
            saveSelectionAndReturn()
        }

        // Очистка выбора региона
        binding.ivRegionClear.setOnClickListener {
            selectedRegionId = -1
            selectedRegionName = ""
            updateLocationUI()
            updateSelectButtonVisibility()
            saveSelectionAndReturn()
        }

        binding.btnSelect.setOnClickListener {
            saveSelectionAndReturn()
        }
    }

    private fun updateSelectButtonVisibility() {
        val hasSelection = selectedCountryId != -1 || selectedRegionId != -1
        binding.btnSelect.visibility = if (hasSelection) View.VISIBLE else View.GONE
    }

    private fun saveSelectionAndReturn() {
        // Сохраняем выбор в SharedPreferences
        filterRepository.saveLocation(
            countryId = if (selectedCountryId != -1) selectedCountryId else null,
            countryName = selectedCountryName.takeIf { it.isNotEmpty() },
            regionId = if (selectedRegionId != -1) selectedRegionId else null,
            regionName = selectedRegionName.takeIf { it.isNotEmpty() }
        )

        // Отправляем результат в FilterFragment
        parentFragmentManager.setFragmentResult(
            "work_location_selection",
            Bundle().apply {
                putInt("country_id", selectedCountryId)
                putString("country_name", selectedCountryName)
                putInt("region_id", selectedRegionId)
                putString("region_name", selectedRegionName)
            }
        )
        findNavController().popBackStack()
    }

    private fun observeNavigationResults() {
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle

        // Обработка выбора страны
        savedStateHandle?.getLiveData<Int>(KEY_SELECTED_COUNTRY_ID)?.observe(viewLifecycleOwner) { id ->
            if (id != null && id != -1) {
                selectedCountryId = id
                selectedRegionId = -1
                selectedRegionName = ""
                updateLocationUI()
                updateSelectButtonVisibility()
            }
        }

        savedStateHandle?.getLiveData<String>(KEY_SELECTED_COUNTRY_NAME)?.observe(viewLifecycleOwner) { name ->
            if (!name.isNullOrEmpty()) {
                selectedCountryName = name
                updateLocationUI()
            }
        }

        // Обработка выбора региона
        savedStateHandle?.getLiveData<Int>(KEY_SELECTED_REGION_ID)?.observe(viewLifecycleOwner) { id ->
            if (id != null && id != -1) {
                selectedRegionId = id
                updateLocationUI()
                updateSelectButtonVisibility()
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
        // Обновляем отображение страны
        if (selectedCountryName.isNotEmpty() && selectedCountryId != -1) {
            binding.tvCountryLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.black_day))
            binding.tvCountryValue.text = selectedCountryName
            binding.tvCountryValue.visibility = View.VISIBLE
            binding.ivCountryArrow.visibility = View.GONE
            binding.ivCountryClear.visibility = View.VISIBLE
        } else {
            binding.tvCountryLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            binding.tvCountryValue.visibility = View.VISIBLE
            binding.ivCountryArrow.visibility = View.VISIBLE
            binding.ivCountryClear.visibility = View.GONE
        }

        // Обновляем отображение региона
        if (selectedRegionName.isNotEmpty() && selectedRegionId != -1) {
            binding.tvRegionLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.black_day))
            binding.tvRegionValue.text = selectedRegionName
            binding.tvRegionValue.visibility = View.VISIBLE
            binding.ivRegionArrow.visibility = View.GONE
            binding.ivRegionClear.visibility = View.VISIBLE
        } else {
            binding.tvRegionLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            binding.tvRegionValue.visibility = View.VISIBLE
            binding.ivRegionArrow.visibility = View.VISIBLE
            binding.ivRegionClear.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
