package ru.practicum.android.diploma.ui.filter.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.domain.repository.FilterRepository

sealed class RegionSelectionState {
    object Loading : RegionSelectionState()
    data class Content(val regions: List<FilterAreaDto>) : RegionSelectionState()
    object Empty : RegionSelectionState()
    object Error : RegionSelectionState()
}

class RegionSelectionViewModel(
    private val filterRepository: FilterRepository
) : ViewModel() {

    private val _state = MutableStateFlow<RegionSelectionState>(RegionSelectionState.Loading)
    val state: StateFlow<RegionSelectionState> = _state.asStateFlow()

    private var allRegions: List<FilterAreaDto> = emptyList()

    fun loadRegions(countryId: Int) {
        _state.value = RegionSelectionState.Loading
        viewModelScope.launch {
            val result = filterRepository.getAreas()
            result.fold(
                onSuccess = { areas ->
                    allRegions = extractRegions(areas, countryId)
                    if (allRegions.isEmpty()) {
                        _state.value = RegionSelectionState.Empty
                    } else {
                        _state.value = RegionSelectionState.Content(allRegions)
                    }
                },
                onFailure = {
                    _state.value = RegionSelectionState.Error
                }
            )
        }
    }

    fun filterRegions(query: String) {
        if (query.isBlank()) {
            if (allRegions.isEmpty()) {
                _state.value = RegionSelectionState.Empty
            } else {
                _state.value = RegionSelectionState.Content(allRegions)
            }
            return
        }
        val filtered = allRegions.filter { it.name.contains(query.trim(), ignoreCase = true) }
        _state.value = if (filtered.isEmpty()) {
            RegionSelectionState.Empty
        } else {
            RegionSelectionState.Content(filtered)
        }
    }

    private fun extractRegions(areas: List<FilterAreaDto>, countryId: Int): List<FilterAreaDto> {
        return if (countryId == -1) {
            areas.flatMap { country -> country.areas.orEmpty() }
                .sortedBy { it.name }
        } else {
            val country = areas.find { it.id == countryId }
            country?.areas.orEmpty().sortedBy { it.name }
        }
    }
}
