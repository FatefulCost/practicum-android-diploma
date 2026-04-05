package ru.practicum.android.diploma.ui.filter

import android.util.Log
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

    companion object {
        private const val TAG = "FilterViewModel"
    }

    private val _filterSettings = MutableStateFlow(FilterSettings())
    val filterSettings: StateFlow<FilterSettings> = _filterSettings.asStateFlow()

    private val _industries = MutableStateFlow<Resource<List<FilterIndustryDto>>>(Resource.Loading())
    val industries: StateFlow<Resource<List<FilterIndustryDto>>> = _industries.asStateFlow()

    private val _areas = MutableStateFlow<Resource<List<FilterAreaDto>>>(Resource.Loading())
    val areas: StateFlow<Resource<List<FilterAreaDto>>> = _areas.asStateFlow()

    init {
        loadSavedFilters()
        loadIndustries()
        loadAreas()
    }

    private fun loadSavedFilters() {
        val savedSettings = filterStorage.loadFilterSettings()
        Log.d(TAG, "loadSavedFilters: $savedSettings")
        _filterSettings.value = savedSettings
    }

    private fun saveFilters() {
        Log.d(TAG, "saveFilters: ${_filterSettings.value}")
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
                    _industries.value = Resource.Error(
                        error.message ?: "Ошибка загрузки отраслей"
                    )
                }
            )
        }
    }

    fun loadAreas() {
        viewModelScope.launch {
            Log.d(TAG, "loadAreas - START")
            _areas.value = Resource.Loading()
            val result = filterRepository.getAreas()
            Log.d(TAG, "loadAreas - result: $result")
            result.fold(
                onSuccess = { areas ->
                    Log.d(TAG, "loadAreas - SUCCESS, count: ${areas.size}")
                    _areas.value = Resource.Success(areas)
                },
                onFailure = { error ->
                    Log.e(TAG, "loadAreas - ERROR: ${error.message}")
                    _areas.value = Resource.Error(
                        error.message ?: "Ошибка загрузки регионов"
                    )
                }
            )
        }
    }

    fun updateSalary(salary: Int?) {
        Log.d(TAG, "updateSalary: $salary")
        _filterSettings.update { it.copy(salary = salary) }
        saveFilters()
    }

    fun updateOnlyWithSalary(onlyWithSalary: Boolean) {
        Log.d(TAG, "updateOnlyWithSalary: $onlyWithSalary")
        _filterSettings.update { it.copy(onlyWithSalary = onlyWithSalary) }
        saveFilters()
    }

    fun updateIndustry(industryId: Int?, industryName: String?) {
        Log.d(TAG, "updateIndustry: id=$industryId, name=$industryName")
        _filterSettings.update {
            it.copy(
                industryId = industryId,
                industryName = industryName
            )
        }
        saveFilters()
    }

    fun updateLocation(
        countryId: Int?,
        countryName: String?,
        regionId: Int?,
        regionName: String?
    ) {
        Log.d(
            TAG,
            "updateLocation: countryId=$countryId, countryName=$countryName, " +
                "regionId=$regionId, regionName=$regionName"
        )

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
        Log.d(TAG, "resetFilters")
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
