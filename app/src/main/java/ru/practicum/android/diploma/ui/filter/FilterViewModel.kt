package ru.practicum.android.diploma.ui.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.domain.repository.FilterRepository
import ru.practicum.android.diploma.util.Resource

/**
 * ViewModel для экрана фильтров
 *
 *  Пока это простая версия
 *  Хранит настройки фильтра
 *  Умеет сбрасывать настройки
 *  Умеет загружать отрасли (заглушка)
 *
 * В Epic 4 добавим:
 *  Сохранение в SharedPreferences
 *  Применение фильтров к поиску
 *  Полноценная работа с отраслями и регионами
 */
class FilterViewModel(
    private val filterRepository: FilterRepository
) : ViewModel() {

    // Настройки фильтра (сохраняются в памяти, не теряются при повороте)
    private val _filterSettings = MutableStateFlow(FilterSettings())
    val filterSettings: StateFlow<FilterSettings> = _filterSettings.asStateFlow()

    // Список отраслей (загружается из репозитория)
    private val _industries = MutableStateFlow<Resource<List<FilterIndustryDto>>>(Resource.Loading())
    val industries: StateFlow<Resource<List<FilterIndustryDto>>> = _industries.asStateFlow()

    init {
        loadIndustries()
    }

    /**
     * Загрузить список отраслей
     * В Epic 4 здесь будет реальный запрос через filterRepository.getIndustries()
     * Пока это заглушка
     */
    private fun loadIndustries() {
        viewModelScope.launch {
            // В Epic 4 добавим реализацию

            // Пока просто заглушка
            _industries.value = Resource.Success(
                listOf(
                    FilterIndustryDto(1, "IT"),
                    FilterIndustryDto(2, "Маркетинг"),
                    FilterIndustryDto(3, "Продажи"),
                    FilterIndustryDto(4, "Дизайн")
                )
            )
        }
    }

    /**
     * Обновить зарплату
     */
    fun updateSalary(salary: Int?) {
        _filterSettings.value = _filterSettings.value.copy(salary = salary)
    }

    /**
     * Обновить чекбокс "Не показывать без зарплаты"
     */
    fun updateOnlyWithSalary(onlyWithSalary: Boolean) {
        _filterSettings.value = _filterSettings.value.copy(onlyWithSalary = onlyWithSalary)
    }

    /**
     * Обновить отрасль
     */
    fun updateIndustry(industryId: Int?, industryName: String?) {
        _filterSettings.value = _filterSettings.value.copy(
            industryId = industryId,
            industryName = industryName
        )
    }

    /**
     * Обновить местоположение
     */
    fun updateLocation(countryId: Int?, countryName: String?, regionId: Int?, regionName: String?) {
        _filterSettings.value = _filterSettings.value.copy(
            countryId = countryId,
            countryName = countryName,
            regionId = regionId,
            regionName = regionName
        )
    }

    /**
     * Сбросить все настройки фильтра
     */
    fun resetFilters() {
        _filterSettings.value = FilterSettings()
    }

    /**
     * Проверить, есть ли активные фильтры
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

/**
 * Класс для хранения настроек фильтрации
 */
data class FilterSettings(
    val salary: Int? = null,
    val onlyWithSalary: Boolean = false,
    val industryId: Int? = null,
    val industryName: String? = null,
    val countryId: Int? = null,
    val countryName: String? = null,
    val regionId: Int? = null,
    val regionName: String? = null
)
