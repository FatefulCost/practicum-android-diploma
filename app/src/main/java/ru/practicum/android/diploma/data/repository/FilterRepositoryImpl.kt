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
        private const val SUFFIX_SALARY = "_salary"
        private const val SUFFIX_ONLY_WITH_SALARY = "_onlyWithSalary"
        private const val SUFFIX_INDUSTRY_ID = "_industryId"
        private const val SUFFIX_INDUSTRY_NAME = "_industryName"
        private const val SUFFIX_COUNTRY_ID = "_countryId"
        private const val SUFFIX_COUNTRY_NAME = "_countryName"
        private const val SUFFIX_REGION_ID = "_regionId"
        private const val SUFFIX_REGION_NAME = "_regionName"
    }

    override fun saveFilterSettings(settings: FilterSettings) {
        sharedPreferences.edit()
            .putInt(KEY_FILTER_SETTINGS + SUFFIX_SALARY, settings.salary ?: -1)
            .putBoolean(KEY_FILTER_SETTINGS + SUFFIX_ONLY_WITH_SALARY, settings.onlyWithSalary)
            .putInt(KEY_FILTER_SETTINGS + SUFFIX_INDUSTRY_ID, settings.industryId ?: -1)
            .putString(KEY_FILTER_SETTINGS + SUFFIX_INDUSTRY_NAME, settings.industryName)
            .putInt(KEY_FILTER_SETTINGS + SUFFIX_COUNTRY_ID, settings.countryId ?: -1)
            .putString(KEY_FILTER_SETTINGS + SUFFIX_COUNTRY_NAME, settings.countryName)
            .putInt(KEY_FILTER_SETTINGS + SUFFIX_REGION_ID, settings.regionId ?: -1)
            .putString(KEY_FILTER_SETTINGS + SUFFIX_REGION_NAME, settings.regionName)
            .apply()
    }

    override fun getFilterSettings(): FilterSettings? {
        val salary = sharedPreferences.getInt(KEY_FILTER_SETTINGS + SUFFIX_SALARY, -1)
        if (salary == -1 && !sharedPreferences.contains(KEY_FILTER_SETTINGS + SUFFIX_SALARY)) {
            return null
        }
        return FilterSettings(
            salary = if (salary == -1) null else salary,
            onlyWithSalary = sharedPreferences.getBoolean(KEY_FILTER_SETTINGS + SUFFIX_ONLY_WITH_SALARY, false),
            industryId = sharedPreferences.getInt(KEY_FILTER_SETTINGS + SUFFIX_INDUSTRY_ID, -1).takeIf { it != -1 },
            industryName = sharedPreferences.getString(KEY_FILTER_SETTINGS + SUFFIX_INDUSTRY_NAME, null),
            countryId = sharedPreferences.getInt(KEY_FILTER_SETTINGS + SUFFIX_COUNTRY_ID, -1).takeIf { it != -1 },
            countryName = sharedPreferences.getString(KEY_FILTER_SETTINGS + SUFFIX_COUNTRY_NAME, null),
            regionId = sharedPreferences.getInt(KEY_FILTER_SETTINGS + SUFFIX_REGION_ID, -1).takeIf { it != -1 },
            regionName = sharedPreferences.getString(KEY_FILTER_SETTINGS + SUFFIX_REGION_NAME, null)
        )
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
}
