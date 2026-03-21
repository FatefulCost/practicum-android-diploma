package ru.practicum.android.diploma.data.network

import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.data.dto.VacancyResponseDto

class NetworkClient(
    private val apiService: ApiService
) {

    suspend fun getAreas(): Result<List<FilterAreaDto>> {
        return try {
            Result.success(apiService.getAreas())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getIndustries(): Result<List<FilterIndustryDto>> {
        return try {
            Result.success(apiService.getIndustries())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchVacancies(
        area: Int? = null,
        industry: Int? = null,
        text: String? = null,
        salary: Int? = null,
        page: Int = 0,
        onlyWithSalary: Boolean = false
    ): Result<VacancyResponseDto> {
        return try {
            Result.success(apiService.searchVacancies(area, industry, text, salary, page, onlyWithSalary))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVacancyDetails(vacancyId: String): Result<VacancyDetailDto> {
        return try {
            Result.success(apiService.getVacancyDetails(vacancyId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
