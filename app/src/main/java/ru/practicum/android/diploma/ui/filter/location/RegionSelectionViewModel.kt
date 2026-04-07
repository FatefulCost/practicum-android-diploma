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
    private val regionToCountryMap = mutableMapOf<Int, Pair<Int, String>>()
    private val allCountries = mutableListOf<FilterAreaDto>()

    fun loadRegions(countryId: Int) {
        _state.value = RegionSelectionState.Loading
        viewModelScope.launch {
            val result = filterRepository.getAreas()
            result.fold(
                onSuccess = { areas ->
                    parseAreas(areas)
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

    /**
     * Парсим все области и строим карту регион - страна
     */
    private fun parseAreas(areas: List<FilterAreaDto>) {
        regionToCountryMap.clear()
        allCountries.clear()

        for (country in areas) {
            // Страна это элемент с parentId == null
            if (country.parentId == null) {
                allCountries.add(country)

                // Регионы это элементы в списке areas у страны
                for (region in country.areas.orEmpty()) {
                    if (region.parentId != null) {
                        regionToCountryMap[region.id] = Pair(country.id, country.name)
                    }
                }
            }
        }
    }

    fun getCountryIdForRegion(regionId: Int): Int {
        return regionToCountryMap[regionId]?.first ?: -1
    }

    fun getCountryNameForRegion(regionId: Int): String {
        return regionToCountryMap[regionId]?.second ?: ""
    }

    fun filterRegions(query: String) {
        if (query.isBlank()) {
            _state.value = if (allRegions.isEmpty()) {
                RegionSelectionState.Empty
            } else {
                RegionSelectionState.Content(allRegions)
            }
            return
        }
        val filtered = allRegions.filter {
            it.name.contains(query.trim(), ignoreCase = true)
        }
        _state.value = if (filtered.isEmpty()) {
            RegionSelectionState.Empty
        } else {
            RegionSelectionState.Content(filtered)
        }
    }

    /**
     * Извлекаем регионы
     */
    private fun extractRegions(areas: List<FilterAreaDto>, countryId: Int): List<FilterAreaDto> {
        return if (countryId != -1) {
            // Страна выбрана - регионы только этой страны
            val country = areas.find { it.id == countryId }
            country?.areas.orEmpty()
                .filter { it.parentId != null }
                .sortedBy { it.name }
        } else {
            // Страна не выбрана - регионы из всех стран
            areas.flatMap { country -> country.areas.orEmpty() }
                .filter { it.parentId != null }
                .sortedBy { it.name }
        }
    }
}
