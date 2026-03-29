package ru.practicum.android.diploma.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModel()
    private lateinit var adapter: VacancyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeState()
    }

    private fun setupUI() {
        setupRecyclerView()

        binding.fabFilter.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_filterFragment)
        }

        binding.editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.editTextSearch.text.toString()
                if (query.isNotBlank()) {
                    viewModel.performSearch(query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = VacancyAdapter(emptyList()) { vacancy ->
            val bundle = Bundle().apply {
                putString("vacancyId", vacancy.id)
            }
            findNavController().navigate(R.id.action_searchFragment_to_vacancyDetailFragment, bundle)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeState() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.Empty -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.textViewEmpty.visibility = View.VISIBLE
                    binding.textViewEmpty.text = getString(R.string.search_hint)
                }
                is SearchState.Loading -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.textViewEmpty.visibility = View.VISIBLE
                    binding.textViewEmpty.text = "Загрузка..."
                }
                is SearchState.EmptyResult -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.textViewEmpty.visibility = View.VISIBLE
                    binding.textViewEmpty.text = "Ничего не найдено"
                }
                is SearchState.Success -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.textViewEmpty.visibility = View.GONE
                    adapter.updateData(state.vacancies)
                }
                is SearchState.Error -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.textViewEmpty.visibility = View.VISIBLE
                    binding.textViewEmpty.text = when (state.error) {
                        ErrorType.NO_INTERNET -> getString(R.string.error_no_internet)
                        ErrorType.SERVER_ERROR -> getString(R.string.error_loading_data)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
