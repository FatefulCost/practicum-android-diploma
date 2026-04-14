package ru.practicum.android.diploma.ui.filter.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

        private const val TEXT_SIZE_SELECTED = 12f
        private const val TEXT_SIZE_DEFAULT = 16f
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
        loadSavedSelection()
        setupUI()
        observeNavigationResults()
        updateSelectButtonVisibility()
    }

    /**
     * Загружаем сохраненные настройки из SharedPreferences
     */
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
        binding.toolbar.setNavigationOnClickListener {
            // Только обновляем иконку фильтра
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

        updateLocationUI()
        updateSelectButtonVisibility()

        // Переход на экран выбора страны
        binding.layoutCountry.setOnClickListener {
            findNavController().navigate(R.id.action_workLocationFragment_to_countrySelectionFragment)
        }

        // Переход на экран выбора региона
        binding.layoutRegion.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("selected_country_id", selectedCountryId)
                putString("selected_country_name", selectedCountryName)
            }
            findNavController().navigate(
                R.id.action_workLocationFragment_to_regionSelectionFragment,
                bundle
            )
        }

        // Обработка нажатия на крестик (очистка страны)
        binding.ivCountryClear.setOnClickListener {
            clearCountrySelection()
        }

        // Обработка нажатия на крестик (очистка региона)
        binding.ivRegionClear.setOnClickListener {
            clearRegionSelection()
        }

        binding.btnSelect.setOnClickListener {
            saveSelectionAndReturn()
        }
    }

    /**
     * Очистка выбранной страны
     */
    private fun clearCountrySelection() {
        selectedCountryId = -1
        selectedCountryName = ""
        selectedRegionId = -1
        selectedRegionName = ""

        updateLocationUI()
        updateSelectButtonVisibility()

        // Сохраняем изменения
        filterRepository.saveLocation(null, null, null, null)
    }

    /**
     * Очистка выбранного региона
     */
    private fun clearRegionSelection() {
        selectedRegionId = -1
        selectedRegionName = ""

        updateLocationUI()
        updateSelectButtonVisibility()

        // Сохраняем изменения
        filterRepository.saveLocation(
            if (selectedCountryId != -1) selectedCountryId else null,
            selectedCountryName,
            null,
            null
        )
    }

    /**
     * Кнопка "Выбрать" видна, если есть выбранная страна ИЛИ регион
     */
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

        // Обработка выбора региона (автоматически выбирает страну)
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

        // Обработка случая, когда регион выбрал страну автоматически
        savedStateHandle?.getLiveData<Int>("auto_selected_country_id")?.observe(viewLifecycleOwner) { id ->
            if (id != null && id != -1 && selectedCountryId == -1) {
                selectedCountryId = id
                updateLocationUI()
                updateSelectButtonVisibility()
            }
        }

        savedStateHandle?.getLiveData<String>("auto_selected_country_name")?.observe(viewLifecycleOwner) { name ->
            if (!name.isNullOrEmpty() && selectedCountryName.isEmpty()) {
                selectedCountryName = name
                updateLocationUI()
            }
        }
    }

    /**
     * Обновляет UI для страны и региона
     */
    private fun updateLocationUI() {
        updateCountryUI()
        updateRegionUI()
    }

    /**
     * Обновляет UI для страны
     */
    private fun updateCountryUI() {
        // Обновление текста при выборе страны
        val hasCountry = selectedCountryName.isNotEmpty()
        val countryTitleView = binding.layoutCountry.findViewById<TextView>(R.id.tvCountryTitle)
        val countryValue = binding.tvCountryValue

        if (hasCountry) {
            countryTitleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.title_color))
            countryTitleView.textSize = TEXT_SIZE_SELECTED
            countryValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.title_color))
            countryValue.text = selectedCountryName
            countryValue.visibility = View.VISIBLE

            // Показываем крестик, скрываем стрелку
            binding.ivCountryChevron.visibility = View.GONE
            binding.ivCountryClear.visibility = View.VISIBLE
        } else {
            countryTitleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            countryTitleView.textSize = TEXT_SIZE_DEFAULT
            countryValue.visibility = View.GONE

            // Показываем стрелку, скрываем крестик
            binding.ivCountryChevron.visibility = View.VISIBLE
            binding.ivCountryClear.visibility = View.GONE
        }
    }

    /**
     * Обновляет UI для региона
     */
    private fun updateRegionUI() {
        // Обновление текста при выборе региона
        val hasRegion = selectedRegionName.isNotEmpty()
        val regionTitleView = binding.layoutRegion.findViewById<TextView>(R.id.tvRegionTitle)
        val regionValue = binding.tvRegionValue

        if (hasRegion) {
            regionTitleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.title_color))
            regionTitleView.textSize = TEXT_SIZE_SELECTED
            regionValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.title_color))
            regionValue.text = selectedRegionName
            regionValue.visibility = View.VISIBLE

            // Показываем крестик, скрываем стрелку
            binding.ivRegionChevron.visibility = View.GONE
            binding.ivRegionClear.visibility = View.VISIBLE
        } else {
            regionTitleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            regionTitleView.textSize = TEXT_SIZE_DEFAULT
            regionValue.visibility = View.GONE

            // Показываем стрелку, скрываем крестик
            binding.ivRegionChevron.visibility = View.VISIBLE
            binding.ivRegionClear.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
