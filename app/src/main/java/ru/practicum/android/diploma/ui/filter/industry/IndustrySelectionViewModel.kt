package ru.practicum.android.diploma.ui.filter.industry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.domain.repository.FilterRepository

class IndustrySelectionViewModel(
    private val filterRepository: FilterRepository
) : ViewModel() {

    private val _filteredIndustries = MutableStateFlow<List<FilterIndustryDto>>(emptyList())
    val filteredIndustries: StateFlow<List<FilterIndustryDto>> = _filteredIndustries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var allIndustries = listOf<FilterIndustryDto>()

    init {
        loadIndustries()
    }

    private fun loadIndustries() {
        viewModelScope.launch {
            _isLoading.value = true
            val cached = filterRepository.getCachedIndustries()
            if (cached != null) {
                allIndustries = cached
                _filteredIndustries.value = cached
            }

            val result = filterRepository.getIndustries()
            result.onSuccess { industries ->
                allIndustries = industries
                _filteredIndustries.value = industries
                filterRepository.cacheIndustries(industries)
            }
            _isLoading.value = false
        }
    }

    fun searchIndustries(query: String) {
        _filteredIndustries.value = if (query.isBlank()) {
            allIndustries
        } else {
            allIndustries.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
}