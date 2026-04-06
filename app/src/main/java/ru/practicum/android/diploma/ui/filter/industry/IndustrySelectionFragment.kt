package ru.practicum.android.diploma.ui.filter.industry

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.databinding.FragmentIndustrySelectionBinding

class IndustrySelectionFragment : Fragment() {

    private var _binding: FragmentIndustrySelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IndustrySelectionViewModel by viewModel()
    private lateinit var adapter: IndustryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndustrySelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupButtons()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = IndustryAdapter(
            onItemClick = { industry ->
                adapter.selectItem(industry.id)
            },
            onItemSelected = { selectedIndustry ->
                binding.btnChoose.visibility = if (selectedIndustry != null) View.VISIBLE else View.GONE
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchIndustries(s?.toString() ?: "")
                // Reset selection when searching
                adapter.selectItem(null)
                binding.btnChoose.visibility = View.GONE
            }
        })
    }

        btnSelect.setOnClickListener {
            Toast.makeText(requireContext(), R.string.industry_selected, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredIndustries.collectLatest { industries ->
                adapter.updateIndustries(industries)
                // Reset selection when list changes (search results)
                adapter.selectItem(null)
                binding.btnChoose.visibility = View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                // We don't have progressBar in the layout, so we just ignore loading state
            }
        }
    }

        Toast.makeText(requireContext(), R.string.industries_loading_coming_soon, Toast.LENGTH_SHORT).show()
    }
}