package ru.practicum.android.diploma.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.domain.repository.FilterRepository

class FilterRepositoryImpl(
    private val networkClient: NetworkClient,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : FilterRepository {

    companion object {
        private const val KEY_AREAS_CACHE = "cached_areas"
        private const val KEY_INDUSTRIES_CACHE = "cached_industries"
    }

    override suspend fun getAreas(): Result<List<FilterAreaDto>> {
        // Сначала пробуем получить из кэша
        val cachedAreas = getCachedAreas()
        if (cachedAreas != null && cachedAreas.isNotEmpty()) {
            return Result.success(cachedAreas)
        }

        // Если кэша нет, делаем реальный запрос к API
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

    override suspend fun getIndustries(): Result<List<FilterIndustryDto>> {
        // Сначала пробуем получить из кэша
        val cachedIndustries = getCachedIndustries()
        if (cachedIndustries != null && cachedIndustries.isNotEmpty()) {
            return Result.success(cachedIndustries)
        }

        // Если кэша нет, делаем реальный запрос к API
        val result = networkClient.getIndustries()
        result.onSuccess { industries ->
            cacheIndustries(industries)
        }
        return result
    }

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
