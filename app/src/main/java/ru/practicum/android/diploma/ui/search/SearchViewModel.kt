package ru.practicum.android.diploma.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.data.dto.VacancyResponseDto
import ru.practicum.android.diploma.domain.models.hasActiveFilters
import ru.practicum.android.diploma.domain.repository.FilterRepository
import ru.practicum.android.diploma.domain.repository.VacancyRepository
import ru.practicum.android.diploma.util.NetworkUtils
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SearchViewModel(
    private val repository: VacancyRepository,
    private val filterRepository: FilterRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private var searchJob: Job? = null

    // Параметры для пагинации
    private var currentQuery = ""
    private var currentPage = 1
    private var totalPages = 0
    private var isLoading = false
    private var isLastPage = false

    // Список всех загруженных вакансий
    private var allVacancies = mutableListOf<VacancyDetailDto>()

    // Состояние активных фильтров
    private val _hasActiveFilters = MutableLiveData(false)
    val hasActiveFilters: LiveData<Boolean> = _hasActiveFilters

    companion object {
        private const val DEBOUNCE_DELAY = 2000L
    }

    init {
        _searchState.value = SearchState.Empty
        refreshFilterState()
    }

    fun cancelSearch() {
        searchJob?.cancel()
        searchJob = null
        isLoading = false
    }

    fun refreshFilterState() {
        viewModelScope.launch {
            val settings = filterRepository.getFilterSettings()
            _hasActiveFilters.value = settings?.hasActiveFilters() ?: false
        }
    }

    fun updateSearchQuery(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            clearSearch()
            return
        }

        resetPagination()
        currentQuery = query

        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_DELAY)
            performSearchWithFilters(query, 1, isLoadMore = false)
        }
    }

    fun loadNextPage() {
        // Проверка: уже идет загрузка
        if (isLoading) return

        // Проверка: это последняя страница
        if (isLastPage) return

        // Проверка: нет поискового запроса
        if (currentQuery.isBlank()) return

        // Проверка: текущий список пуст
        if (allVacancies.isEmpty()) return

        val nextPage = currentPage + 1
        if (nextPage < totalPages) {
            performSearchWithFilters(currentQuery, nextPage, isLoadMore = true)
        }
    }

    fun searchWithAppliedFilters() {
        val query = currentQuery
        if (query.isNotBlank()) {
            searchJob?.cancel()
            resetPagination()
            performSearchWithFilters(query, 1, isLoadMore = false)
        }
    }

    private fun performSearchWithFilters(query: String, page: Int, isLoadMore: Boolean = false) {
        val isValidQuery = query.isNotBlank()
        val canPerformSearch = isValidQuery && !isLoading

        if (!canPerformSearch) return

        isLoading = true

        // Проверяем интернет только для первого поиска
        // Для подгрузки страниц - пробуем загрузить, если нет интернета - показываем Toast
        if (!isLoadMore && !networkUtils.isNetworkAvailable()) {
            _searchState.value = SearchState.Error(ErrorType.NO_INTERNET)
            isLoading = false
            return
        }

        val filterSettings = filterRepository.getFilterSettings()
        val areaId = filterRepository.loadSavedRegionId() ?: filterRepository.loadSavedCountryId()

        if (!isLoadMore) {
            _searchState.value = SearchState.Loading
        } else {
            // Показываем индикатор загрузки внизу списка
            _searchState.value = SearchState.LoadingMore
        }

        viewModelScope.launch {
            val result = repository.searchVacancies(
                text = query,
                page = page,
                area = areaId,
                salary = filterSettings?.salary,
                industry = filterSettings?.industryId,
                onlyWithSalary = filterSettings?.onlyWithSalary ?: false
            )
            result.fold(
                onSuccess = { response ->
                    handleSearchSuccess(response, query, page, isLoadMore)
                },
                onFailure = { exception ->
                    handleSearchError(exception, isLoadMore)
                }
            )
        }
    }

    private fun handleSearchSuccess(
        response: VacancyResponseDto,
        query: String,
        page: Int,
        isLoadMore: Boolean
    ) {
        currentQuery = query
        currentPage = response.page
        totalPages = response.pages
        isLastPage = response.page >= response.pages

        val newVacancies = response.vacancies ?: emptyList()

        if (newVacancies.isEmpty() && page == 1) {
            _searchState.value = SearchState.EmptyResult
            isLoading = false
            return
        }

        if (isLoadMore) {
            // Добавляем новые вакансии к уже существующим
            allVacancies.addAll(newVacancies)
            _searchState.value = SearchState.Success(
                vacancies = allVacancies.toList(),
                totalFound = response.found,
                isLoadingMore = false
            )
        } else {
            // Новая страница, заменяем список
            allVacancies.clear()
            allVacancies.addAll(newVacancies)
            _searchState.value = SearchState.Success(
                vacancies = allVacancies.toList(),
                totalFound = response.found,
                isLoadingMore = false
            )
        }

        isLoading = false
    }

    private fun handleSearchError(exception: Throwable, isLoadMore: Boolean) {
        isLoading = false

        if (isLoadMore) {
            // При ошибке подгрузки:
            // 1. Скрываем индикатор загрузки
            // 2. Показываем Toast с сообщением
            // 3. Оставляем уже загруженный список
            val errorMessage = when (exception) {
                is UnknownHostException, is SocketTimeoutException -> "Нет подключения к интернету"
                else -> "Ошибка загрузки данных"
            }
            _searchState.value = SearchState.LoadMoreError(errorMessage)
        } else {
            _searchState.value = SearchState.Error(ErrorType.SERVER_ERROR)
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        resetPagination()
        allVacancies.clear()
        _searchState.value = SearchState.Empty
    }

    private fun resetPagination() {
        currentQuery = ""
        currentPage = 1
        totalPages = 0
        isLastPage = false
        isLoading = false
        allVacancies.clear()
    }
}

sealed class SearchState {
    object Empty : SearchState()
    object Loading : SearchState()
    object LoadingMore : SearchState()
    object EmptyResult : SearchState()
    data class Success(
        val vacancies: List<VacancyDetailDto>,
        val totalFound: Int,
        val isLoadingMore: Boolean = false
    ) : SearchState()
    data class LoadMoreError(val message: String) : SearchState()  // Только Toast, без плейсхолдера
    data class Error(val error: ErrorType) : SearchState()
}

enum class ErrorType {
    NO_INTERNET,
    SERVER_ERROR
}
