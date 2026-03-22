package ru.practicum.android.diploma.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
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

    // Заглушка для регионов
    override suspend fun getAreas(): Result<List<FilterAreaDto>> {
        val testAreas = listOf(
            FilterAreaDto(id = 1, name = "Россия", parentId = null, areas = listOf(
                FilterAreaDto(id = 2, name = "Москва", parentId = 1, areas = emptyList())
            )),
            FilterAreaDto(id = 3, name = "Беларусь", parentId = null, areas = listOf(
                FilterAreaDto(id = 4, name = "Минск", parentId = 3, areas = emptyList())
            ))
        )
        return Result.success(testAreas)
    }

    // Заглушка для отраслей
    override suspend fun getIndustries(): Result<List<FilterIndustryDto>> {
        return Result.success(listOf(
            FilterIndustryDto(1, "IT"),
            FilterIndustryDto(2, "Маркетинг"),
            FilterIndustryDto(3, "Продажи")
        ))
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
