package ru.practicum.android.diploma.data.network

import retrofit2.HttpException
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.data.dto.VacancyResponseDto
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import android.util.Log

class NetworkClient(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "NetworkClient"
    }

    suspend fun getAreas(): Result<List<FilterAreaDto>> {
        return try {
            Result.success(apiService.getAreas())
        } catch (e: Exception) {
            Log.e(TAG, "getAreas error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getIndustries(): Result<List<FilterIndustryDto>> {
        return try {
            Result.success(apiService.getIndustries())
        } catch (e: Exception) {
            Log.e(TAG, "getIndustries error: ${e.message}")
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
            Log.d(TAG, "=== SEARCH REQUEST ===")
            Log.d(TAG, "text: $text")
            Log.d(TAG, "page: $page")

            val response = apiService.searchVacancies(
                area = area,
                industry = industry,
                text = text,
                salary = salary,
                page = page,
                onlyWithSalary = onlyWithSalary
            )

            val vacancies = response.vacancies ?: emptyList()

            Log.d(TAG, "=== SEARCH RESPONSE ===")
            Log.d(TAG, "found: ${response.found}")
            Log.d(TAG, "pages: ${response.pages}")
            Log.d(TAG, "page: ${response.page}")
            Log.d(TAG, "vacancies count: ${vacancies.size}")

            if (vacancies.isNotEmpty()) {
                Log.d(TAG, "First vacancy: ${vacancies.first().name}")
            }

            val safeResponse = VacancyResponseDto(
                found = response.found,
                pages = response.pages,
                page = response.page,
                vacancies = vacancies
            )

            Result.success(safeResponse)
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error: ${e.code()} - ${e.message()}")
            Result.failure(e)
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout: ${e.message}")
            Result.failure(e)
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Unknown host: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            Log.e(TAG, "IO error: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getVacancyDetails(vacancyId: String): Result<VacancyDetailDto> {
        return try {
            Log.d(TAG, "Getting details for vacancy: $vacancyId")
            val response = apiService.getVacancyDetails(vacancyId)
            Log.d(TAG, "Got details for: ${response.name}")
            Result.success(response)
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error: ${e.code()} - ${e.message()}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            Result.failure(e)
        }
    }
}
