package ru.practicum.android.diploma.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.domain.repository.FilterRepository
import ru.practicum.android.diploma.ui.filter.FilterSettings

private const val NUMBERFORMAGIC1 = 1
private const val NUMBERFORMAGIC2 = 2
private const val NUMBERFORMAGIC3 = 3
private const val NUMBERFORMAGIC4 = 4

class FilterRepositoryImpl(
    private val networkClient: NetworkClient,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : FilterRepository {

    companion object {
        private const val KEY_AREAS_CACHE = "cached_areas"
        private const val KEY_INDUSTRIES_CACHE = "cached_industries"
        private const val KEY_FILTER_SETTINGS = "filter_settings"
    }

    // Заглушка для регионов
    override suspend fun getAreas(): Result<List<FilterAreaDto>> {
        val testAreas = listOf(
            FilterAreaDto(
                id = NUMBERFORMAGIC1,
                name = "Россия",
                parentId = null,
                areas = listOf(
                    FilterAreaDto(
                        id = NUMBERFORMAGIC2,
                        name = "Москва",
                        parentId = NUMBERFORMAGIC1,
                        areas = emptyList()
                    )
                )
            ),
            FilterAreaDto(
                id = NUMBERFORMAGIC3,
                name = "Беларусь",
                parentId = null,
                areas = listOf(
                    FilterAreaDto(
                        id = NUMBERFORMAGIC4,
                        name = "Минск",
                        parentId = NUMBERFORMAGIC3,
                        areas = emptyList()
                    )
                )
            )
        )
        return Result.success(testAreas)
    }

    // Заглушка для отраслей
    override suspend fun getIndustries(): Result<List<FilterIndustryDto>> {
        return Result.success(
            listOf(
                FilterIndustryDto(NUMBERFORMAGIC1, "IT"),
                FilterIndustryDto(NUMBERFORMAGIC2, "Маркетинг"),
                FilterIndustryDto(NUMBERFORMAGIC3, "Продажи")
            )
        )
    }

    // Кэширование (через SharedPreferences)
    override suspend fun getCachedAreas(): List<FilterAreaDto>? {
        val json = sharedPreferences.getString(KEY_AREAS_CACHE, null)
        return json?.let {
            val type = object : TypeToken<List<FilterAreaDto>>() {}.type
            gson.fromJson(it, type)
        }
    }

    override suspend fun cacheAreas(areas: List<FilterAreaDto>) {
        sharedPreferences.edit().putString(KEY_AREAS_CACHE, gson.toJson(areas)).apply()
    }

    override suspend fun getCachedIndustries(): List<FilterIndustryDto>? {
        val json = sharedPreferences.getString(KEY_INDUSTRIES_CACHE, null)
        return json?.let {
            val type = object : TypeToken<List<FilterIndustryDto>>() {}.type
            gson.fromJson(it, type)
        }
    }

    override suspend fun cacheIndustries(industries: List<FilterIndustryDto>) {
        sharedPreferences.edit().putString(KEY_INDUSTRIES_CACHE, gson.toJson(industries)).apply()
    }

    override fun saveFilterSettings(settings: FilterSettings) {
        sharedPreferences.edit().putString(KEY_FILTER_SETTINGS, gson.toJson(settings)).apply()
    }

    override fun getFilterSettings(): FilterSettings? {
        val json = sharedPreferences.getString(KEY_FILTER_SETTINGS, null)
        return json?.let {
            gson.fromJson(it, FilterSettings::class.java)
        }
    }
}
