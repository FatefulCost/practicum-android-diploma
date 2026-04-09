package ru.practicum.android.diploma.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
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
            viewModel.updateLocation(countryId, countryName, regionId, regionName)

            viewModel.updateLocation(
                if (countryId != -1) countryId else null,
                countryName,
                if (regionId != -1) regionId else null,
                regionName
            )

            // обновляем UI (без ожидания обсервера)
            updateLocationUI(countryName, regionName)
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

        binding.etSalary.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.etSalary.clearFocus()
                true
            } else false
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

        binding.ivWorkLocationIcon.setOnClickListener {
            if (binding.tvWorkLocationValue.isVisible) {
                viewModel.updateLocation(null, null, null, null)
                binding.tvWorkLocationValue.visibility = View.GONE
                updateWorkLocationIcon(false)
            }
        }

        binding.ivIndustryIcon.setOnClickListener {
            if (binding.tvIndustryValue.isVisible) {
                viewModel.updateIndustry(null, null)
                binding.tvIndustryValue.visibility = View.GONE
                updateIndustryIcon(false)
            }
        }

        binding.ivSalaryClear.setOnClickListener {
            binding.etSalary.setText("")
            viewModel.updateSalary(null)
        }
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

        val hasIndustry = !settings.industryName.isNullOrBlank()
        binding.tvIndustryValue.text = settings.industryName ?: getString(NOT_SELECTED)
        binding.tvIndustryValue.visibility = if (hasIndustry) View.VISIBLE else View.GONE

        val hasLocation = !settings.countryName.isNullOrBlank() || !settings.regionName.isNullOrBlank()
        updateWorkLocationIcon(hasLocation)
        updateIndustryIcon(hasIndustry)

        updateButtonsVisibility(settings)
    }

    private fun updateWorkLocationIcon(hasLocation: Boolean) {
        binding.ivWorkLocationIcon.setImageResource(
            if (hasLocation) R.drawable.close_24px else R.drawable.ic_arrow_forward_go
        )
    }

    private fun updateIndustryIcon(hasIndustry: Boolean) {
        binding.ivIndustryIcon.setImageResource(
            if (hasIndustry) R.drawable.close_24px else R.drawable.ic_arrow_forward_go
        )
    }

    private fun updateButtonsVisibility(settings: FilterSettings) {
        val hasChanges = viewModel.isSettingsChanged()
        binding.btnApply.parent.let { (it as View).visibility = if (hasChanges) View.VISIBLE else View.GONE }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
