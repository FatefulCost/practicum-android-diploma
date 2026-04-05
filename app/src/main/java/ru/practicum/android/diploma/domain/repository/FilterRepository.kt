// domain/repository/FilterRepository.kt
package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.ui.filter.FilterSettings

interface FilterRepository {

    // Получить регионы из сети
    suspend fun getAreas(): Result<List<FilterAreaDto>>

    // Получить отрасли из сети
    suspend fun getIndustries(): Result<List<FilterIndustryDto>>

    // Работа с кэшем
    suspend fun getCachedAreas(): List<FilterAreaDto>?
    suspend fun cacheAreas(areas: List<FilterAreaDto>)
    suspend fun getCachedIndustries(): List<FilterIndustryDto>?
    suspend fun cacheIndustries(industries: List<FilterIndustryDto>)

    // Сохранение и загрузка настроек фильтра
    fun saveFilterSettings(settings: FilterSettings)
    fun getFilterSettings(): FilterSettings?

    // Сохранение и загрузка местоположения
    fun saveLocation(countryId: Int?, countryName: String?, regionId: Int?, regionName: String?)
    fun loadSavedCountryId(): Int?
    fun loadSavedCountryName(): String?
    fun loadSavedRegionId(): Int?
    fun loadSavedRegionName(): String?
}
