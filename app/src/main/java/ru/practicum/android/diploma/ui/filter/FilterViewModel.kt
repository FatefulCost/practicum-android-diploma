package ru.practicum.android.diploma.ui.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.data.storage.FilterStorage
import ru.practicum.android.diploma.domain.repository.FilterRepository
import ru.practicum.android.diploma.util.Resource

class FilterViewModel(
    private val filterRepository: FilterRepository,
    private val filterStorage: FilterStorage
) : ViewModel() {

    private val _filterSettings = MutableStateFlow(FilterSettings())
    val filterSettings: StateFlow<FilterSettings> = _filterSettings.asStateFlow()

    private val _industries = MutableStateFlow<Resource<List<FilterIndustryDto>>>(Resource.Loading())
    val industries: StateFlow<Resource<List<FilterIndustryDto>>> = _industries.asStateFlow()

    init {
        loadSavedFilters()
        loadIndustries()
    }

    private fun loadSavedFilters() {
        val savedSettings = filterStorage.loadFilterSettings()
        _filterSettings.value = savedSettings
    }

    private fun saveFilters() {
        filterStorage.saveFilterSettings(_filterSettings.value)
    }

    private fun loadIndustries() {
        viewModelScope.launch {
            _industries.value = Resource.Loading()
            val result = filterRepository.getIndustries()
            result.fold(
                onSuccess = { industries ->
                    _industries.value = Resource.Success(industries)
                },
                onFailure = { error ->
                    _industries.value = Resource.Error(error.message ?: "Ошибка загрузки отраслей")
                }
            )
        }
    }

    private val _areas = MutableStateFlow<Resource<List<FilterAreaDto>>>(Resource.Loading())
    val areas: StateFlow<Resource<List<FilterAreaDto>>> = _areas.asStateFlow()

    private fun loadAreas() {
        viewModelScope.launch {
            _areas.value = Resource.Loading()
            val result = filterRepository.getAreas()
            result.fold(
                onSuccess = { areas ->
                    _areas.value = Resource.Success(areas)
                },
                onFailure = { error ->
                    _areas.value = Resource.Error(error.message ?: "Ошибка загрузки регионов")
                }
            )
        }
    }

    init {
        loadSavedFilters()
        loadIndustries()
        loadAreas()
    }

    fun updateSalary(salary: Int?) {
        _filterSettings.update { it.copy(salary = salary) }
        saveFilters()
    }

    fun updateOnlyWithSalary(onlyWithSalary: Boolean) {
        _filterSettings.update { it.copy(onlyWithSalary = onlyWithSalary) }
        saveFilters()
    }

    fun updateIndustry(industryId: Int?, industryName: String?) {
        _filterSettings.update {
            it.copy(
                industryId = industryId,
                industryName = industryName
            )
        }
        saveFilters()
    }

    fun updateLocation(countryId: Int?, countryName: String?, regionId: Int?, regionName: String?) {
        _filterSettings.update {
            it.copy(
                countryId = countryId,
                countryName = countryName,
                regionId = regionId,
                regionName = regionName
            )
        }
        saveFilters()
    }

    fun resetFilters() {
        _filterSettings.value = FilterSettings()
        filterStorage.clearFilterSettings()
    }

    fun refreshFilters() {
        _filterSettings.value = _filterSettings.value
    }

    fun getSearchParams(): Map<String, Any> {
        val settings = _filterSettings.value
        val params = mutableMapOf<String, Any>()

        settings.salary?.let { params["salary"] = it }
        if (settings.onlyWithSalary) {
            params["only_with_salary"] = true
        }
        settings.industryId?.let { params["industry"] = it }

        settings.regionId?.let {
            params["area"] = it
        } ?: settings.countryId?.let {
            params["area"] = it
        }

        return params
    }

    fun hasActiveFilters(): Boolean {
        val settings = _filterSettings.value
        return settings.salary != null ||
            settings.onlyWithSalary ||
            settings.industryId != null ||
            settings.countryId != null ||
            settings.regionId != null
    }
}
