package ru.practicum.android.diploma.ui.filter.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.databinding.FragmentWorkLocationBinding

class WorkLocationFragment : Fragment() {
    private var _binding: FragmentWorkLocationBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun setupUI() {
        binding.layoutCountry.setOnClickListener {
            Toast.makeText(requireContext(), "Выбор страны (будет реализовано позже)", Toast.LENGTH_SHORT).show()
        }

        binding.layoutRegion.setOnClickListener {
            Toast.makeText(requireContext(), "Выбор региона (будет реализовано позже)", Toast.LENGTH_SHORT).show()
        }

        binding.btnSelect.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
