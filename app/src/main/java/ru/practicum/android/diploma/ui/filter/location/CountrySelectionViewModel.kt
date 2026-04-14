package ru.practicum.android.diploma.ui.filter.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.domain.repository.FilterRepository

sealed class CountrySelectionState {
    object Loading : CountrySelectionState()
    data class Content(val countries: List<FilterAreaDto>) : CountrySelectionState()
    object Empty : CountrySelectionState()
    object Error : CountrySelectionState()
}

class CountrySelectionViewModel(
    private val filterRepository: FilterRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CountrySelectionState>(CountrySelectionState.Loading)
    val state: StateFlow<CountrySelectionState> = _state.asStateFlow()

    init {
        loadCountries()
    }

    fun loadCountries() {
        _state.value = CountrySelectionState.Loading
        viewModelScope.launch {
            val result = filterRepository.getAreas()
            result.fold(
                onSuccess = { areas ->
                    val countries = areas.filter { it.parentId == null }
                    if (countries.isEmpty()) {
                        _state.value = CountrySelectionState.Empty
                    } else {
                        _state.value = CountrySelectionState.Content(countries)
                    }
                },
                onFailure = {
                    _state.value = CountrySelectionState.Error
                }
            )
        }
    }
}
