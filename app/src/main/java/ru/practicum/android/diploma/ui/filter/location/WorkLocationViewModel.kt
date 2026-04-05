package ru.practicum.android.diploma.ui.filter.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.domain.repository.FilterRepository
import ru.practicum.android.diploma.util.Resource

class WorkLocationViewModel(
    private val filterRepository: FilterRepository
) : ViewModel() {

    private val _countries = MutableStateFlow<Resource<List<FilterAreaDto>>>(Resource.Loading())
    val countries: StateFlow<Resource<List<FilterAreaDto>>> = _countries.asStateFlow()

    private val _regions = MutableStateFlow<Resource<List<FilterAreaDto>>>(Resource.Loading())
    val regions: StateFlow<Resource<List<FilterAreaDto>>> = _regions.asStateFlow()

    init {
        loadCountries()
    }

    fun loadCountries() {
        viewModelScope.launch {
            _countries.value = Resource.Loading()
            val result = filterRepository.getAreas()
            result.fold(
                onSuccess = { areas ->
                    val countries = areas.filter { it.parentId == null }
                    _countries.value = Resource.Success(countries)
                },
                onFailure = { error ->
                    _countries.value = Resource.Error(error.message ?: "Ошибка загрузки стран")
                }
            )
        }
    }

    fun loadRegions(countryId: Int) {
        viewModelScope.launch {
            _regions.value = Resource.Loading()
            val result = filterRepository.getAreas()
            result.fold(
                onSuccess = { areas ->
                    val country = areas.find { it.id == countryId }
                    val regions = country?.areas ?: emptyList()
                    _regions.value = Resource.Success(regions)
                },
                onFailure = { error ->
                    _regions.value = Resource.Error(error.message ?: "Ошибка загрузки регионов")
                }
            )
        }
    }
}
