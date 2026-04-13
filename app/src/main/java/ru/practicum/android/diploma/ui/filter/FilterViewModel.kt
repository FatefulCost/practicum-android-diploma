package ru.practicum.android.diploma.ui.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.domain.models.FilterSettings
import ru.practicum.android.diploma.domain.repository.FilterRepository
import ru.practicum.android.diploma.util.Resource

class FilterViewModel(
    private val filterRepository: FilterRepository
) : ViewModel() {

    // Эталонное состояние настроек из SharedPreferences на момент загрузки/сохранения
    private var initialSettings: FilterSettings? = null

    // Текущее состояние (черновик), с которым взаимодействует пользователь в UI
    private val _filterSettings = MutableStateFlow(FilterSettings())
    val filterSettings: StateFlow<FilterSettings> = _filterSettings.asStateFlow()

    private val _industries = MutableStateFlow<Resource<List<FilterIndustryDto>>>(Resource.Loading())
    val industries: StateFlow<Resource<List<FilterIndustryDto>>> = _industries.asStateFlow()

    init {
        loadAllSettings()
        loadIndustries()
    }

    /**
     * Загружает настройки и локацию, формируя "начальное состояние" для сравнения
     */
    private fun loadAllSettings() {
        val settings = filterRepository.getFilterSettings() ?: FilterSettings()

        // Дозагружаем локацию, так как она может храниться отдельно
        val countryId = filterRepository.loadSavedCountryId()
        val countryName = filterRepository.loadSavedCountryName()
        val regionId = filterRepository.loadSavedRegionId()
        val regionName = filterRepository.loadSavedRegionName()

        val fullSettings = settings.copy(
            countryId = countryId,
            countryName = countryName,
            regionId = regionId,
            regionName = regionName
        )

        initialSettings = fullSettings
        _filterSettings.value = fullSettings
    }

    private fun loadIndustries() {
        viewModelScope.launch {
            _industries.value = Resource.Loading()
            val result = filterRepository.getIndustries()
            _industries.value = result.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.message ?: "Error loading industries") }
            )
        }
    }

    fun updateSalary(salary: Int?) {
        _filterSettings.value = _filterSettings.value.copy(salary = salary)
    }

    fun updateOnlyWithSalary(onlyWithSalary: Boolean) {
        _filterSettings.value = _filterSettings.value.copy(onlyWithSalary = onlyWithSalary)
    }

    fun updateIndustry(industryId: Int?, industryName: String?) {
        _filterSettings.value = _filterSettings.value.copy(
            industryId = industryId,
            industryName = industryName
        )
    }

    fun updateLocation(countryId: Int?, countryName: String?, regionId: Int?, regionName: String?) {
        _filterSettings.value = _filterSettings.value.copy(
            countryId = countryId,
            countryName = countryName,
            regionId = regionId,
            regionName = regionName
        )
        // Сохраняем локацию сразу, так как этого требует текущая логика репозитория
        filterRepository.saveLocation(countryId, countryName, regionId, regionName)
    }

    /**
     * Сбросить все настройки фильтра.
     * Мы перезаписываем SharedPreferences пустым объектом.
     */
    fun resetFilters() {
        val emptySettings = FilterSettings()
        _filterSettings.value = emptySettings
        filterRepository.saveFilterSettings(emptySettings)
        filterRepository.saveLocation(null, null, null, null)
        initialSettings = emptySettings // Теперь "пустота" — наше новое эталонное состояние
    }

    /**
     * Сохранить текущий черновик настроек в SharedPreferences
     */
    fun saveSettings() {
        val current = _filterSettings.value
        filterRepository.saveFilterSettings(current)
        initialSettings = current // После сохранения черновик становится эталоном
    }

    /**
     * Проверка: отличается ли то, что ввел пользователь, от того, что было сохранено?
     */
    fun isSettingsChanged(): Boolean {
        return _filterSettings.value != initialSettings
    }

    /**
     * Проверить, есть ли активные фильтры (для показа кнопки "Сбросить")
     */
    fun hasActiveFilters(): Boolean {
        val settings = _filterSettings.value
        return settings.salary != null ||
            settings.onlyWithSalary ||
            settings.industryId != null ||
            settings.countryId != null ||
            settings.regionId != null
    }
}
