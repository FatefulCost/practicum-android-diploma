package ru.practicum.android.diploma.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding
import ru.practicum.android.diploma.domain.models.FilterSettings
import kotlinx.coroutines.launch

private val NOT_SELECTED: Int = R.string.not_selected

class FilterFragment : Fragment() {
    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FilterViewModel by viewModel()

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
        setupUI()
        observeViewModel()
        setupResultListener()
    }

    private fun setupResultListener() {
        // Обработка выбора отрасли
        parentFragmentManager.setFragmentResultListener("industry_selection", viewLifecycleOwner) { _, bundle ->
            val industryId = bundle.getInt("industry_id", -1)
            val industryName = bundle.getString("industry_name")
            if (industryId != -1 && !industryName.isNullOrBlank()) {
                viewModel.updateIndustry(industryId, industryName)
                // Обновляем UI
                binding.tvIndustryValue.text = industryName
            }
        }

        // Обработка выбора места работы
        parentFragmentManager.setFragmentResultListener("work_location_selection", viewLifecycleOwner) { _, bundle ->
            val countryId = bundle.getInt("country_id", -1)
            val countryName = bundle.getString("country_name")
            val regionId = bundle.getInt("region_id", -1)
            val regionName = bundle.getString("region_name")

            viewModel.updateLocation(
                if (countryId != -1) countryId else null,
                countryName,
                if (regionId != -1) regionId else null,
                regionName
            )

            // обновляем UI (без ожидания обсервера)
            updateLocationUI(countryName, regionName)

            // Обновляем видимость иконок
            val hasWorkLocation = !countryName.isNullOrBlank() || !regionName.isNullOrBlank()
            if (hasWorkLocation) {
                binding.ivWorkLocationChevron.visibility = View.GONE
                binding.ivWorkLocationClear.visibility = View.VISIBLE
            } else {
                binding.ivWorkLocationChevron.visibility = View.VISIBLE
                binding.ivWorkLocationClear.visibility = View.GONE
            }
        }
    }

    private fun updateLocationUI(countryName: String?, regionName: String?) {
        val countryPart = countryName.takeIf { !it.isNullOrBlank() } ?: ""
        val regionPart = regionName.takeIf { !it.isNullOrBlank() } ?: ""

        val locationText = when {
            countryPart.isNotEmpty() && regionPart.isNotEmpty() -> "$countryPart, $regionPart"
            countryPart.isNotEmpty() -> countryPart
            regionPart.isNotEmpty() -> regionPart
            else -> getString(NOT_SELECTED)
        }
        binding.tvWorkLocationValue.text = locationText
        val hasLocation = countryPart.isNotEmpty() || regionPart.isNotEmpty()
        binding.tvWorkLocationValue.visibility = if (hasLocation) View.VISIBLE else View.GONE
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.etSalary.addTextChangedListener { text ->
            val salary = text?.toString()?.toIntOrNull()
            viewModel.updateSalary(salary)
        }

        binding.cbHideWithoutSalary.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateOnlyWithSalary(isChecked)
        }

        binding.btnApply.setOnClickListener {
            viewModel.saveSettings()
            Toast.makeText(requireContext(), R.string.filters_applied, Toast.LENGTH_SHORT).show()
            findNavController().previousBackStackEntry?.savedStateHandle?.set("filters_changed", true)
            findNavController().popBackStack()
        }

        binding.btnReset.setOnClickListener {
            viewModel.resetFilters()
            Toast.makeText(requireContext(), R.string.filters_reset, Toast.LENGTH_SHORT).show()
            findNavController().previousBackStackEntry?.savedStateHandle?.set("filters_changed", true)
        }

        binding.layoutWorkLocation.setOnClickListener {
            findNavController().navigate(R.id.action_filterFragment_to_workLocationFragment)
        }

        binding.layoutIndustry.setOnClickListener {
            findNavController().navigate(R.id.action_filterFragment_to_industrySelectionFragment)
        }

        // Очистка отрасли
        binding.ivIndustryClear.setOnClickListener {
            viewModel.updateIndustry(null, null)
            binding.tvIndustryValue.text = getString(R.string.not_selected)
            binding.tvIndustryValue.visibility = View.GONE
            binding.ivIndustryChevron.visibility = View.VISIBLE
            binding.ivIndustryClear.visibility = View.GONE
        }

        binding.ivWorkLocationClear.setOnClickListener {
            clearWorkLocation()
        }

        binding.ivSalaryClear.setOnClickListener {
            binding.etSalary.setText("")
            viewModel.updateSalary(null)
        }

    }

    /**
     * Очистка выбранного места работы
     */
    private fun clearWorkLocation() {
        viewModel.updateLocation(null, null, null, null)
        binding.tvWorkLocationValue.text = getString(R.string.not_selected)
        binding.tvWorkLocationValue.visibility = View.GONE
        binding.ivWorkLocationChevron.visibility = View.VISIBLE
        binding.ivWorkLocationClear.visibility = View.GONE
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filterSettings.collect { settings ->
                    updateUI(settings)
                }
            }
        }
    }

    private fun updateUI(settings: FilterSettings) {
        if (binding.etSalary.text?.toString() != settings.salary?.toString()) {
            binding.etSalary.setText(settings.salary?.toString() ?: "")
        }
        binding.ivSalaryClear.isVisible = !binding.etSalary.text.isNullOrEmpty()

        if (binding.cbHideWithoutSalary.isChecked != settings.onlyWithSalary) {
            binding.cbHideWithoutSalary.isChecked = settings.onlyWithSalary
        }

        updateLocationUI(settings.countryName, settings.regionName)

        // Обновляем видимость иконок для места работы
        val hasWorkLocation = !settings.countryName.isNullOrBlank() || !settings.regionName.isNullOrBlank()
        if (hasWorkLocation) {
            binding.ivWorkLocationChevron.visibility = View.GONE
            binding.ivWorkLocationClear.visibility = View.VISIBLE
        } else {
            binding.ivWorkLocationChevron.visibility = View.VISIBLE
            binding.ivWorkLocationClear.visibility = View.GONE
        }

        val hasIndustry = !settings.industryName.isNullOrBlank()
        binding.tvIndustryValue.text = settings.industryName ?: getString(NOT_SELECTED)
        binding.tvIndustryValue.visibility = if (hasIndustry) View.VISIBLE else View.GONE

        // Обновляем видимость иконок для отрасли
        if (hasIndustry) {
            binding.ivIndustryChevron.visibility = View.GONE
            binding.ivIndustryClear.visibility = View.VISIBLE
        } else {
            binding.ivIndustryChevron.visibility = View.VISIBLE
            binding.ivIndustryClear.visibility = View.GONE
        }

        updateButtonsVisibility(settings)
    }

    private fun updateButtonsVisibility(settings: FilterSettings) {
        val hasFilters = settings.salary != null ||
            settings.onlyWithSalary ||
            settings.industryId != null ||
            settings.countryId != null ||
            settings.regionId != null
        binding.btnApply.parent.let { (it as View).visibility = if (hasFilters) View.VISIBLE else View.GONE }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
