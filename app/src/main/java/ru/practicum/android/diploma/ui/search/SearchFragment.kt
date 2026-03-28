package ru.practicum.android.diploma.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModel()

    private lateinit var adapter: VacancyAdapter
    private var isLoadingMore = false  // Флаг, чтобы не вызывать loadNextPage несколько раз

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
        observeViewModel()
    }

    private fun setupUI() {
        adapter = VacancyAdapter { vacancy ->
            val bundle = Bundle()
            bundle.putString("vacancy_id", vacancy.id)
            findNavController().navigate(
                R.id.action_searchFragment_to_vacancyDetailFragment,
                bundle
            )
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchFragment.adapter

            // скролл-листенер для пагинации
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = adapter?.itemCount ?: 0

                    // Если дошли до конца, загружаем следующую страницу
                    if (!isLoadingMore && lastVisiblePosition >= totalItemCount - 3 && totalItemCount > 0) {
                        isLoadingMore = true
                        viewModel.loadNextPage()
                    }
                }
            })
        }

        // Обработка ввода текста
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateSearchQuery(s?.toString() ?: "")
            }
        })

        // Кнопка фильтра
        binding.fabFilter.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_filterFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.Empty -> showEmptyState()
                is SearchState.Loading -> showLoadingState()
                is SearchState.LoadingMore -> showLoadingMoreState()
                is SearchState.EmptyResult -> showEmptyResultState()
                is SearchState.Success -> showContentState(state.vacancies, state.totalFound)
                is SearchState.LoadMoreError -> showLoadMoreError(state.message)
                is SearchState.Error -> showErrorState(state.error)
            }
            // Сбрасываем флаг, когда загрузка закончена
            if (state !is SearchState.Loading && state !is SearchState.LoadingMore) {
                isLoadingMore = false
            }
        }
    }

    private fun showEmptyState() {
        hideAllPlaceholders()
        binding.tvFoundCount.visibility = View.GONE
        binding.placeholderEnterQuery.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        requireActivity().title = getString(R.string.menu_search)
    }

    private fun showLoadingState() {
        hideAllPlaceholders()
        binding.tvFoundCount.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        requireActivity().title = getString(R.string.menu_search)
    }

    // Показываем индикатор загрузки внизу списка
    private fun showLoadingMoreState() {
        // TO DO
        // Нужно показать ProgressBar внизу списка
    }

    private fun showEmptyResultState() {
        hideAllPlaceholders()
        binding.tvFoundCount.visibility = View.GONE
        binding.placeholderNotFound.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        requireActivity().title = getString(R.string.menu_search)
    }

    private fun showContentState(vacancies: List<VacancyDetailDto>, totalFound: Int) {
        hideAllPlaceholders()

        if (vacancies.isEmpty()) {
            showEmptyResultState()
            return
        }

        binding.tvFoundCount.text = "Найдено: $totalFound вакансий"
        binding.tvFoundCount.visibility = View.VISIBLE

        binding.recyclerView.visibility = View.VISIBLE
        adapter.updateVacancies(vacancies)

        requireActivity().title = getString(R.string.menu_search)
    }

    // Ошибка при дозагрузке
    private fun showLoadMoreError(message: String) {
        Toast.makeText(requireContext(), "Не удалось загрузить следующие вакансии", Toast.LENGTH_SHORT).show()
    }

    private fun showErrorState(errorType: ErrorType) {
        hideAllPlaceholders()
        binding.tvFoundCount.visibility = View.GONE
        when (errorType) {
            ErrorType.NO_INTERNET -> {
                binding.placeholderNoInternet.visibility = View.VISIBLE
                Toast.makeText(requireContext(), R.string.error_no_internet, Toast.LENGTH_SHORT).show()
            }
            ErrorType.SERVER_ERROR -> {
                binding.placeholderError.visibility = View.VISIBLE
                Toast.makeText(requireContext(), R.string.error_loading_data, Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerView.visibility = View.GONE
        requireActivity().title = getString(R.string.menu_search)
    }

    private fun hideAllPlaceholders() {
        binding.tvFoundCount.visibility = View.GONE
        binding.placeholderEnterQuery.visibility = View.GONE
        binding.placeholderNotFound.visibility = View.GONE
        binding.placeholderNoInternet.visibility = View.GONE
        binding.placeholderError.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
