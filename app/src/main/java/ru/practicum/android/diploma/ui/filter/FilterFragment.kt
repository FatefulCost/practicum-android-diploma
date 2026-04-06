package ru.practicum.android.diploma.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import androidx.lifecycle.Lifecycle
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
    }

    private fun setupUI() {
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
            findNavController().popBackStack()
        }

        binding.btnReset.setOnClickListener {
            viewModel.resetFilters()
            Toast.makeText(requireContext(), R.string.filters_reset, Toast.LENGTH_SHORT).show()
        }

        binding.layoutWorkLocation.setOnClickListener {
            findNavController().navigate(R.id.action_filterFragment_to_workLocationFragment)
        }

        binding.layoutIndustry.setOnClickListener {
            findNavController().navigate(R.id.action_filterFragment_to_industrySelectionFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.filterSettings.onEach { settings ->
            updateUI(settings)
        }.launchIn(lifecycleScope)
    }

    private fun updateUI(settings: FilterSettings) {
        // Зарплата
        settings.salary?.let {
            binding.etSalary.setText(it.toString())
        } ?: binding.etSalary.text?.clear()

        // Чекбокс
        binding.cbHideWithoutSalary.isChecked = settings.onlyWithSalary

        // Отрасль
        if (!settings.industryName.isNullOrBlank()) {
            binding.tvIndustryValue.text = settings.industryName
        } else {
            binding.tvIndustryValue.text = getString(R.string.not_selected)
        }

        // Место работы
        val locationText = buildString {
            if (!settings.regionName.isNullOrBlank()) {
                append(settings.regionName)
            } else if (!settings.countryName.isNullOrBlank()) {
                append(settings.countryName)
            }
        }

        val locationValue = if (locationText.isNotEmpty()) {
            locationText
        } else {
            getString(R.string.not_selected)
        }
        binding.tvWorkLocationValue.text = locationValue
    }

    private fun applyFilters() {
        val salaryText = binding.etSalary.text?.toString()
        val salary = if (!salaryText.isNullOrBlank()) salaryText.toIntOrNull() else null

        viewModel.updateSalary(salary)
        viewModel.updateOnlyWithSalary(binding.cbHideWithoutSalary.isChecked)

        Toast.makeText(requireContext(), "Фильтры применены", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun resetFilters() {
        viewModel.resetFilters()
        Toast.makeText(requireContext(), "Фильтры сброшены", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
