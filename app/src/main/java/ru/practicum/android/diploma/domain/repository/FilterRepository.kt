package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.domain.models.FilterSettings

interface FilterRepository {

    fun saveFilterSettings(settings: FilterSettings)

    fun getFilterSettings(): FilterSettings?

    suspend fun getAreas(): Result<List<FilterAreaDto>>

    suspend fun getIndustries(): Result<List<FilterIndustryDto>>

    suspend fun getCachedAreas(): List<FilterAreaDto>?
    suspend fun cacheAreas(areas: List<FilterAreaDto>)
    suspend fun getCachedIndustries(): List<FilterIndustryDto>?
    suspend fun cacheIndustries(industries: List<FilterIndustryDto>)

    fun saveLocation(countryId: Int?, countryName: String?, regionId: Int?, regionName: String?)
    fun loadSavedCountryId(): Int?
    fun loadSavedCountryName(): String?
    fun loadSavedRegionId(): Int?
    fun loadSavedRegionName(): String?
}
