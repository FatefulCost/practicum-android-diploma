package ru.practicum.android.diploma.ui.search

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
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

    private var shouldRestoreSearchState = false

    // Флаг для предотвращения повторных Toast
    private var hasShownLoadMoreError = false

    private val adapter: VacancyAdapter by lazy {
        VacancyAdapter { vacancy ->
            val bundle = Bundle()
            bundle.putString("vacancyId", vacancy.id)
            findNavController().navigate(
                R.id.action_searchFragment_to_vacancyDetailFragment,
                bundle
            )
        }
    }

    private var isLoadingMore = false // Флаг, чтобы не вызывать loadNextPage несколько раз
    companion object {
        private const val SCROLL_LAST = 3
    }

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
        observeFilterState()
        observeFilterChanges()
        observeSearchRequest()
    }

    override fun onResume() {
        super.onResume()
        updateFilterIconState()
        shouldRestoreSearchState = false
        hasShownLoadMoreError = false
    }

    override fun onPause() {
        super.onPause()
        shouldRestoreSearchState = true
    }

    private fun updateFilterIconState() {
        viewModel.refreshFilterState()
    }

    /**
     * Наблюдаем за запросом на поиск после нажатия "Применить"
     */
    private fun observeSearchRequest() {
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        val needSearchLiveData = savedStateHandle?.getLiveData<Boolean>("need_search")

        needSearchLiveData?.observe(viewLifecycleOwner) { needSearch ->
            if (needSearch == true) {
                val currentQuery = binding.editTextSearch.text.toString()
                if (currentQuery.isNotBlank()) {
                    viewModel.searchWithAppliedFilters()
                }
                savedStateHandle?.set("need_search", false)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI() {
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
                    if (!isLoadingMore && lastVisiblePosition >= totalItemCount - SCROLL_LAST && totalItemCount > 0) {
                        isLoadingMore = true
                        viewModel.loadNextPage()
                    }
                }
            })
        }

        // Обработка ввода текста и иконок
        setupSearchTextListener()

        // Обработка нажатия на иконку справа
        setupClearIconTouchListener()

        // Кнопка фильтра
        binding.fabFilter.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_filterFragment)
        }
    }

    /**
     * Наблюдаем за состоянием активных фильтров и меняем цвет кнопки
     */
    private fun observeFilterState() {
        viewModel.hasActiveFilters.observe(viewLifecycleOwner) { hasFilters ->
            updateFilterButtonColor(hasFilters)
        }
    }

    /**
     * Меняем цвет иконки фильтра в зависимости от наличия активных фильтров
     */
    private fun updateFilterButtonColor(hasFilters: Boolean) {
        val colorRes = if (hasFilters) {
            R.color.white_day // Активные фильтры — белая иконка
        } else {
            R.color.filter_icon // Нет фильтров — стандартный цвет
        }
        val color = ContextCompat.getColor(requireContext(), colorRes)
        binding.fabFilter.setColorFilter(color, PorterDuff.Mode.SRC_IN)

        val backgroundRes = if (hasFilters) {
            R.drawable.bg_filter_button_active
        } else {
            R.drawable.bg_filter_button_inactive
        }
        binding.fabFilter.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_filter)
        )
        binding.fabFilter.background = ContextCompat.getDrawable(requireContext(), backgroundRes)
    }

    /**
     * Наблюдаем за изменениями фильтров (когда возвращаемся с экрана фильтрации)
     */
    private fun observeFilterChanges() {
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle

        // Обработка нажатия "Применить" — выполняем поиск
        val applyFiltersLiveData = savedStateHandle?.getLiveData<Boolean>("apply_filters")
        applyFiltersLiveData?.observe(viewLifecycleOwner) { shouldApply ->
            if (shouldApply == true) {
                android.util.Log.d("SearchFragment", "apply_filters triggered - perform search")
                viewModel.refreshFilterState()
                val currentQuery = binding.editTextSearch.text.toString()
                if (currentQuery.isNotBlank()) {
                    viewModel.searchWithAppliedFilters()
                }
                savedStateHandle.set("apply_filters", false)
            }
        }

        // Обработка возврата через "Назад" — отменяем поиск и обновляем иконку
        val filtersChangedLiveData = savedStateHandle?.getLiveData<Boolean>("filters_changed")
        filtersChangedLiveData?.observe(viewLifecycleOwner) { changed ->
            if (changed == true) {
                android.util.Log.d("SearchFragment", "filters_changed triggered - cancel search and update icon")
                // Отменяем текущий поиск
                viewModel.cancelSearch()
                viewModel.refreshFilterState()
                savedStateHandle.set("filters_changed", false)
            }
        }
    }

    /**
     * Настройка обработчика ввода текста и смены иконок
     */
    private fun setupSearchTextListener() {
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                updateSearchIcon(s)

                // Если возвращаемся на экран - не запускаем поиск
                if (shouldRestoreSearchState) {
                    shouldRestoreSearchState = false
                    return
                }

                viewModel.updateSearchQuery(query)
            }
        })
    }

    /**
     * Обновляет иконку справа в зависимости от наличия текста
     */
    private fun updateSearchIcon(text: CharSequence?) {
        val iconRes = if (text.isNullOrEmpty()) {
            R.drawable.search_24px
        } else {
            R.drawable.close_24px_for_edit_text // коммент для коммита
        }
        binding.editTextSearch.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, iconRes, 0)
    }

    /**
     * Настройка обработчика нажатия на иконку справа
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupClearIconTouchListener() {
        binding.editTextSearch.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                handleClearIconClick(event.rawX)
            }
            false
        }
    }

    /**
     * Обработка нажатия на иконку очистки
     */
    private fun handleClearIconClick(touchX: Float) {
        val drawableEnd = binding.editTextSearch.compoundDrawablesRelative[2]
        val rightEdge = binding.editTextSearch.right - binding.editTextSearch.compoundPaddingRight

        if (drawableEnd != null && touchX >= rightEdge) {
            binding.editTextSearch.text?.clear()
        }
    }

    private fun observeViewModel() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.Empty -> showEmptyState()
                is SearchState.Loading -> {
                    showLoadingState()
                    // При новом поиске сбрасываем флаг ошибки
                    hasShownLoadMoreError = false
                }
                is SearchState.LoadingMore -> showLoadingMoreState()
                is SearchState.EmptyResult -> showEmptyResultState()
                is SearchState.Success -> {
                    showContentState(state.vacancies, state.totalFound)
                    // При успешной загрузке сбрасываем флаг ошибки
                    hasShownLoadMoreError = false
                }
                is SearchState.LoadMoreError -> {
                    // Показываем Toast только один раз
                    if (!hasShownLoadMoreError) {
                        showLoadMoreError(state.message)
                        hasShownLoadMoreError = true
                    }
                    binding.progressBarBottom.visibility = View.GONE
                    isLoadingMore = false
                }
                is SearchState.Error -> {
                    if (adapter.itemCount == 0) {
                        showErrorState(state.error)
                    } else if (!hasShownLoadMoreError) {
                        val message = if (state.error == ErrorType.NO_INTERNET) {
                            R.string.error_no_internet
                        } else {
                            R.string.error_loading_data
                        }
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        hasShownLoadMoreError = true
                    }
                    isLoadingMore = false
                }
            }
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
    }

    private fun showLoadingState() {
        hideAllPlaceholders()
        binding.tvFoundCount.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    // Показываем индикатор загрузки внизу списка
    private fun showLoadingMoreState() {
        binding.progressBarBottom.visibility = View.VISIBLE
    }

    private fun showEmptyResultState() {
        hideAllPlaceholders()
        binding.tvFoundCount.text = "Таких вакансий нет"
        binding.tvFoundCount.visibility = View.VISIBLE
        binding.placeholderNotFound.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun showContentState(vacancies: List<VacancyDetailDto>, totalFound: Int) {
        hideAllPlaceholders()

        if (vacancies.isEmpty()) {
            showEmptyResultState()
            return
        }

        binding.tvFoundCount.text = "Найдено: $totalFound вакансий"
        binding.tvFoundCount.visibility = View.VISIBLE
        binding.progressBarBottom.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        adapter.updateVacancies(vacancies)
    }

    // Ошибка при дозагрузке
    private fun showLoadMoreError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorState(errorType: ErrorType) {
        // Показываем плейсхолдер только если список пуст
        if (adapter.itemCount == 0) {
            hideAllPlaceholders()
            binding.tvFoundCount.visibility = View.GONE
            when (errorType) {
                ErrorType.NO_INTERNET -> {
                    binding.placeholderNoInternet.visibility = View.VISIBLE
                }
                ErrorType.SERVER_ERROR -> {
                    binding.placeholderError.visibility = View.VISIBLE
                }
            }
            binding.recyclerView.visibility = View.GONE
        } else {
            // Если уже есть данные - просто показываем Toast
            val message = if (errorType == ErrorType.NO_INTERNET) {
                R.string.error_no_internet
            } else {
                R.string.error_loading_data
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
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
