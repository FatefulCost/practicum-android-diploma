package ru.practicum.android.diploma.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.domain.repository.FilterRepository

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

        private const val KEY_COUNTRY_ID = "filter_country_id"
        private const val KEY_COUNTRY_NAME = "filter_country_name"
        private const val KEY_REGION_ID = "filter_region_id"
        private const val KEY_REGION_NAME = "filter_region_name"
        private const val NO_ID = -1
    }

    override suspend fun getAreas(): Result<List<FilterAreaDto>> {
        val cached = getCachedAreas()
        if (cached != null) {
            return Result.success(cached)
        }
        val result = networkClient.getAreas()
        result.onSuccess { areas ->
            cacheAreas(areas)
        }
        return result
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

    override fun saveLocation(countryId: Int?, countryName: String?, regionId: Int?, regionName: String?) {
        sharedPreferences.edit()
            .putInt(KEY_COUNTRY_ID, countryId ?: NO_ID)
            .putString(KEY_COUNTRY_NAME, countryName)
            .putInt(KEY_REGION_ID, regionId ?: NO_ID)
            .putString(KEY_REGION_NAME, regionName)
            .apply()
    }

    override fun loadSavedCountryId(): Int? {
        val id = sharedPreferences.getInt(KEY_COUNTRY_ID, NO_ID)
        return if (id == NO_ID) null else id
    }

    override fun loadSavedCountryName(): String? =
        sharedPreferences.getString(KEY_COUNTRY_NAME, null)

    override fun loadSavedRegionId(): Int? {
        val id = sharedPreferences.getInt(KEY_REGION_ID, NO_ID)
        return if (id == NO_ID) null else id
    }

    override fun loadSavedRegionName(): String? =
        sharedPreferences.getString(KEY_REGION_NAME, null)
}
