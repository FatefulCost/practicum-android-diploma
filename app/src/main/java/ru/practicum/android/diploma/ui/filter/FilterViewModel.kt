package ru.practicum.android.diploma.ui.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        loadSavedSettings()
        loadIndustries()
        loadSavedLocation()
    }

    private fun loadSavedSettings() {
        filterRepository.getFilterSettings()?.let { settings ->
            _filterSettings.value = settings
        }
    }

    /**
     * Загрузить сохраненные настройки из SharedPreferences
     */
    private fun loadSavedFilters() {
        val savedSettings = filterStorage.loadFilterSettings()
        _filterSettings.value = savedSettings
    }

    /**
     * Сохранить текущие настройки в SharedPreferences
     */
    private fun saveFilters() {
        filterStorage.saveFilterSettings(_filterSettings.value)
    }

    /**
     * Загрузить список отраслей из репозитория
     */
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

    /**
     * Обновить зарплату
     */
    fun updateSalary(salary: Int?) {
        _filterSettings.update { it.copy(salary = salary) }
        saveFilters()
    }

    /**
     * Обновить чекбокс "Не показывать без зарплаты"
     */
    fun updateOnlyWithSalary(onlyWithSalary: Boolean) {
        _filterSettings.update { it.copy(onlyWithSalary = onlyWithSalary) }
        saveFilters()
    }

    /**
     * Обновить отрасль
     */
    fun updateIndustry(industryId: Int?, industryName: String?) {
        _filterSettings.update {
            it.copy(
                industryId = industryId,
                industryName = industryName
            )
        }
        saveFilters()
    }

    /**
     * Загрузить сохранённое место работы из SharedPreferences
     */
    private fun loadSavedLocation() {
        val countryId = filterRepository.loadSavedCountryId()
        val countryName = filterRepository.loadSavedCountryName()
        val regionId = filterRepository.loadSavedRegionId()
        val regionName = filterRepository.loadSavedRegionName()
        if (countryId != null || regionId != null) {
            _filterSettings.value = _filterSettings.value.copy(
                countryId = countryId,
                countryName = countryName,
                regionId = regionId,
                regionName = regionName
            )
        }
    }

    /**
     * Обновить местоположение и сохранить в SharedPreferences
     */
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

    /**
     * Сбросить все настройки фильтра
     */
    fun resetFilters() {
        _filterSettings.value = FilterSettingsHelper.reset()
        filterStorage.clearFilterSettings()
    }

    /**
     * Получить параметры для поискового запроса
     */
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

    /**
     * Проверить, есть ли активные фильтры
     */
    fun hasActiveFilters(): Boolean {
        return FilterSettingsHelper.hasActiveFilters(_filterSettings.value)
    }
}
