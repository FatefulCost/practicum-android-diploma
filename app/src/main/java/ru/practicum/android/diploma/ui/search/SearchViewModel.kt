package ru.practicum.android.diploma.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.domain.repository.VacancyRepository
import ru.practicum.android.diploma.util.NetworkUtils
import android.util.Log

class SearchViewModel(
    private val vacancyRepository: VacancyRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private var searchJob: Job? = null

    companion object {
        private const val DEBOUNCE_DELAY = 500L
        private const val TAG = "SearchViewModel"
    }

    init {
        _searchState.value = SearchState.Empty
    }

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchState.value = SearchState.Empty
            return
        }

        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_DELAY)
            performSearch(query)
        }
    }

    fun performSearch(query: String) {
        if (query.isBlank()) {
            _searchState.value = SearchState.Empty
            return
        }

        if (!networkUtils.isNetworkAvailable()) {
            _searchState.value = SearchState.Error(ErrorType.NO_INTERNET)
            return
        }

        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            Log.d(TAG, "Поиск: $query")

            val result = vacancyRepository.searchVacancies(text = query)
            result.fold(
                onSuccess = { response ->
                    // КРИТИЧЕСКИ ВАЖНО: проверяем на null
                    val vacancies = response.vacancies ?: emptyList()
                    Log.d(TAG, "Успех! found: ${response.found}, pages: ${response.pages}, vacancies size: ${vacancies.size}")

                    if (vacancies.isEmpty()) {
                        Log.d(TAG, "Список вакансий пуст")
                        _searchState.value = SearchState.EmptyResult
                    } else {
                        Log.d(TAG, "Найдено ${vacancies.size} вакансий")
                        _searchState.value = SearchState.Success(vacancies)
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Ошибка: ${error.message}", error)
                    _searchState.value = SearchState.Error(ErrorType.SERVER_ERROR)
                }
            )
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchState.value = SearchState.Empty
    }
}

sealed class SearchState {
    object Empty : SearchState()
    object Loading : SearchState()
    object EmptyResult : SearchState()
    data class Success(val vacancies: List<VacancyDetailDto>) : SearchState()
    data class Error(val error: ErrorType) : SearchState()
}

enum class ErrorType {
    NO_INTERNET,
    SERVER_ERROR
}
