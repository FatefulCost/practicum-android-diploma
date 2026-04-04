package ru.practicum.android.diploma.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding

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
        setupFragmentResultListener()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnApply.setOnClickListener {
            val salary = binding.etSalary.text?.toString()?.toIntOrNull()
            viewModel.updateSalary(salary)
            viewModel.updateOnlyWithSalary(binding.cbHideWithoutSalary.isChecked)
            Toast.makeText(requireContext(), "Фильтры применены", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        binding.btnReset.setOnClickListener {
            viewModel.resetFilters()
            Toast.makeText(requireContext(), "Фильтры сброшены", Toast.LENGTH_SHORT).show()
        }

        binding.layoutWorkLocation.setOnClickListener {
            findNavController().navigate(R.id.action_filterFragment_to_workLocationFragment)
        }

        binding.layoutIndustry.setOnClickListener {
            findNavController().navigate(R.id.action_filterFragment_to_industrySelectionFragment)
        }
    }

    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener("industry_selection", this) { _, result ->
            val industryId = result.getInt("industryId", -1)
            val industryName = result.getString("industryName", null)
            if (industryId != -1 && industryName != null) {
                viewModel.updateIndustry(industryId, industryName)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filterSettings.collectLatest { settings ->
                binding.tvIndustryValue.text = settings.industryName ?: getString(R.string.not_selected)
                binding.tvWorkLocationValue.text = when {
                    settings.regionName != null -> "${settings.countryName}, ${settings.regionName}"
                    settings.countryName != null -> settings.countryName
                    else -> getString(R.string.not_selected)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
