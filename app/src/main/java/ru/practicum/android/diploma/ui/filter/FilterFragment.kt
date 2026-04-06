package ru.practicum.android.diploma.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding
import ru.practicum.android.diploma.domain.model.FilterSettings
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

        if (binding.cbHideWithoutSalary.isChecked != settings.onlyWithSalary) {
            binding.cbHideWithoutSalary.isChecked = settings.onlyWithSalary
        }

        binding.tvWorkLocationValue.text = when {
            settings.regionName != null -> settings.regionName
            settings.countryName != null -> settings.countryName
            else -> getString(NOT_SELECTED)
        }

        binding.tvIndustryValue.text = settings.industryName ?: getString(NOT_SELECTED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
