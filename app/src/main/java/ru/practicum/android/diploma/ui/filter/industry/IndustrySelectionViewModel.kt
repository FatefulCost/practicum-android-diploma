package ru.practicum.android.diploma.ui.filter.industry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.domain.repository.FilterRepository

sealed class IndustrySelectionState {
    object Loading : IndustrySelectionState()
    data class Content(val industries: List<FilterIndustryDto>) : IndustrySelectionState()
    object Empty : IndustrySelectionState()
    object Error : IndustrySelectionState()
}

class IndustrySelectionViewModel(
    private val filterRepository: FilterRepository
) : ViewModel() {

    private val _state = MutableStateFlow<IndustrySelectionState>(IndustrySelectionState.Loading)
    val state: StateFlow<IndustrySelectionState> = _state.asStateFlow()

    private var allIndustries: List<FilterIndustryDto> = emptyList()

    private var selectedIndustryId: Int? = null

    init {
        loadIndustries()
        loadSelectedIndustry()
    }

    /**
     * Загружает сохраненную отрасль из FilterRepository
     */
    private fun loadSelectedIndustry() {
        val settings = filterRepository.getFilterSettings()
        selectedIndustryId = settings?.industryId
        android.util.Log.d("IndustrySelectionVM", "Loaded selected industry: $selectedIndustryId")
    }

    fun loadIndustries() {
        _state.value = IndustrySelectionState.Loading
        viewModelScope.launch {
            val result = filterRepository.getIndustries()
            result.fold(
                onSuccess = { industries ->
                    allIndustries = industries
                    if (industries.isEmpty()) {
                        _state.value = IndustrySelectionState.Empty
                    } else {
                        _state.value = IndustrySelectionState.Content(industries)
                    }
                },
                onFailure = {
                    _state.value = IndustrySelectionState.Error
                }
            )
        }
    }

    fun filterIndustries(query: String) {
        if (query.isBlank()) {
            if (allIndustries.isEmpty()) {
                _state.value = IndustrySelectionState.Empty
            } else {
                _state.value = IndustrySelectionState.Content(allIndustries)
            }
            return
        }

        val filtered = allIndustries.filter {
            it.name.contains(query.trim(), ignoreCase = true)
        }
        _state.value = if (filtered.isEmpty()) {
            IndustrySelectionState.Empty
        } else {
            IndustrySelectionState.Content(filtered)
        }
    }

    /**
     * Возвращает ID выбранной отрасли
     */
    fun getSelectedIndustryId(): Int? = selectedIndustryId
}
