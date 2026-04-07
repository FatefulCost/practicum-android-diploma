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

    fun loadRegions(countryId: Int) {
        _state.value = RegionSelectionState.Loading
        viewModelScope.launch {
            val result = filterRepository.getAreas()
            result.fold(
                onSuccess = { areas ->
                    buildRegionToCountryMap(areas)
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
     * Строит карту соответствия регион - страна
     * Разбито на несколько маленьких функций
     */
    private fun buildRegionToCountryMap(areas: List<FilterAreaDto>) {
        regionToCountryMap.clear()

        val countries = extractCountries(areas)
        countries.forEach { country ->
            addCountryRegionsToMap(country)
        }
    }

    /**
     * Извлекает страны из списка областей
     */
    private fun extractCountries(areas: List<FilterAreaDto>): List<FilterAreaDto> {
        return areas.filter { it.parentId == null }
    }

    /**
     * Добавляет все регионы страны в карту
     */
    private fun addCountryRegionsToMap(country: FilterAreaDto) {
        val regions = country.areas.orEmpty()
        val validRegions = filterValidRegions(regions)

        validRegions.forEach { region ->
            regionToCountryMap[region.id] = Pair(country.id, country.name)
        }
    }

    /**
     * Фильтрует только регионы
     */
    private fun filterValidRegions(regions: List<FilterAreaDto>): List<FilterAreaDto> {
        return regions.filter { it.parentId != null }
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
     * Извлекает регионы
     */
    private fun extractRegions(areas: List<FilterAreaDto>, countryId: Int): List<FilterAreaDto> {
        return if (countryId != -1) {
            extractRegionsForCountry(areas, countryId)
        } else {
            extractAllRegions(areas)
        }
    }

    /**
     * Извлекает регионы для конкретной страны
     */
    private fun extractRegionsForCountry(areas: List<FilterAreaDto>, countryId: Int): List<FilterAreaDto> {
        val country = areas.find { it.id == countryId }
        val regions = country?.areas.orEmpty()
        return regions.filter { it.parentId != null }.sortedBy { it.name }
    }

    /**
     * Извлекает все регионы из всех стран
     */
    private fun extractAllRegions(areas: List<FilterAreaDto>): List<FilterAreaDto> {
        val countries = extractCountries(areas)
        return countries.flatMap { country -> country.areas.orEmpty() }
            .filter { it.parentId != null }
            .sortedBy { it.name }
    }
}
