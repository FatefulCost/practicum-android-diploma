package ru.practicum.android.diploma.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    companion object {
        private const val SCROLL_LOAD_THRESHOLD = 3
        private const val INITIAL_PAGE = 1
    }

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModel()
    private lateinit var adapter: VacancyAdapter
    private var isLoadingMore = false

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
        setupFabFilter()
        setupSearchEditText()
        setupPaginationScrollListener()
    }

    private fun setupRecyclerView() {
        adapter = VacancyAdapter(emptyList()) { vacancy ->
            val bundle = Bundle().apply {
                putString("vacancyId", vacancy.id)
            }
            findNavController().navigate(
                R.id.action_searchFragment_to_vacancyDetailFragment,
                bundle
            )
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupFabFilter() {
        binding.fabFilter.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_filterFragment)
        }
    }

    private fun setupSearchEditText() {
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateSearchQuery(s?.toString() ?: "")
            }
        })

        binding.editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.editTextSearch.text.toString().trim()
                if (query.isNotBlank()) {
                    viewModel.performSearch(query, INITIAL_PAGE)
                } else {
                    Toast.makeText(requireContext(), R.string.empty_search_query, Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupPaginationScrollListener() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = adapter.itemCount

                if (!isLoadingMore && lastVisiblePosition >= totalItemCount - SCROLL_LOAD_THRESHOLD && totalItemCount > 0) {
                    isLoadingMore = true
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun observeState() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.Empty -> showEmptyState()
                is SearchState.Loading -> showLoadingState()
                is SearchState.LoadingMore -> showLoadingMore()
                is SearchState.EmptyResult -> showEmptyResult()
                is SearchState.Success -> showResults(state.vacancies)
                is SearchState.LoadMoreError -> showLoadMoreError()
                is SearchState.Error -> showErrorState(state.error)
            }
            resetLoadingFlagIfNeeded(state)
        }
    }

    private fun showEmptyState() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.textViewEmpty.visibility = View.VISIBLE
        binding.textViewEmpty.text = getString(R.string.search_hint)
    }

    private fun showLoadingState() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.textViewEmpty.visibility = View.VISIBLE
        binding.textViewEmpty.text = getString(R.string.loading)
    }

    private fun showLoadingMore() {
        Toast.makeText(requireContext(), getString(R.string.loading_more_vacancies), Toast.LENGTH_SHORT).show()
    }

    private fun showEmptyResult() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.textViewEmpty.visibility = View.VISIBLE
        binding.textViewEmpty.text = getString(R.string.no_results)
    }

    private fun showResults(vacancies: List<VacancyDetailDto>) {
        binding.recyclerView.visibility = View.VISIBLE
        binding.textViewEmpty.visibility = View.GONE
        adapter.updateData(vacancies)
    }

    private fun showLoadMoreError() {
        Toast.makeText(requireContext(), getString(R.string.load_more_error), Toast.LENGTH_SHORT).show()
    }

    private fun showErrorState(error: ErrorType) {
        binding.recyclerView.visibility = View.VISIBLE
        binding.textViewEmpty.visibility = View.VISIBLE
        binding.textViewEmpty.text = when (error) {
            ErrorType.NO_INTERNET -> getString(R.string.error_no_internet)
            ErrorType.SERVER_ERROR -> getString(R.string.error_loading_data)
        }
    }

    private fun resetLoadingFlagIfNeeded(state: SearchState) {
        if (state !is SearchState.Loading && state !is SearchState.LoadingMore) {
            isLoadingMore = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
