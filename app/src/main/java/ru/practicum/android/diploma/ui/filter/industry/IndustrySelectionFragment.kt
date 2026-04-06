package ru.practicum.android.diploma.ui.filter.industry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.R

class IndustrySelectionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_industry_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSelect = view.findViewById<android.widget.Button>(R.id.btnSelect)

        btnSelect.setOnClickListener {
            Toast.makeText(requireContext(), R.string.industry_selected, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        Toast.makeText(requireContext(), R.string.industries_loading_coming_soon, Toast.LENGTH_SHORT).show()
    }
}
