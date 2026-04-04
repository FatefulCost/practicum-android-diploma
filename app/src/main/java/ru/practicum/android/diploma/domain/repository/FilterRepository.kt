package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto

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

    // Сохранение выбранного места работы (страна и регион)
    fun saveLocation(countryId: Int?, countryName: String?, regionId: Int?, regionName: String?)
    fun loadSavedCountryId(): Int?
    fun loadSavedCountryName(): String?
    fun loadSavedRegionId(): Int?
    fun loadSavedRegionName(): String?
}
