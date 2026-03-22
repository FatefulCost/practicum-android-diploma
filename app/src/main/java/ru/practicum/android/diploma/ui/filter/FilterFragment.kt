package ru.practicum.android.diploma.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding

class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun setupUI() {
        binding.btnApply.setOnClickListener {
            val salary = binding.etSalary.text?.toString() ?: ""
            val hideWithoutSalary = binding.cbHideWithoutSalary.isChecked

            val message = buildString {
                append("Фильтры применены\n")
                append("Зарплата: ${if (salary.isNotEmpty()) salary else "не указана"}\n")
                append("Скрывать без зарплаты: $hideWithoutSalary")
            }

            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        binding.btnReset.setOnClickListener {

            binding.etSalary.text?.clear()
            binding.cbHideWithoutSalary.isChecked = false
            binding.tvWorkLocationValue.text = getString(R.string.not_selected)
            binding.tvIndustryValue.text = getString(R.string.not_selected)
            Toast.makeText(requireContext(), "Фильтры сброшены", Toast.LENGTH_SHORT).show()
        }

        binding.layoutWorkLocation.setOnClickListener {
            findNavController().navigate(R.id.action_filterFragment_to_workLocationFragment)
        }

        binding.layoutIndustry.setOnClickListener {
            findNavController().navigate(R.id.action_filterFragment_to_industrySelectionFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
