package ru.practicum.android.diploma.data.network

import retrofit2.HttpException
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.data.dto.VacancyResponseDto
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkClient(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "NetworkClient"
    }

    private suspend fun <T> safeApiCall(
        action: suspend () -> T,
        operationName: String
    ): Result<T> {
        return try {
            Result.success(action())
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: SocketTimeoutException) {
            Result.failure(e)
        } catch (e: UnknownHostException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    suspend fun getAreas(): Result<List<FilterAreaDto>> {
        return safeApiCall(
            action = { apiService.getAreas() },
            operationName = "getAreas"
        )
    }

    suspend fun getIndustries(): Result<List<FilterIndustryDto>> {
        return safeApiCall(
            action = { apiService.getIndustries() },
            operationName = "getIndustries"
        )
    }

    suspend fun searchVacancies(
        area: Int? = null,
        industry: Int? = null,
        text: String? = null,
        salary: Int? = null,
        page: Int = 0,
        onlyWithSalary: Boolean = false
    ): Result<VacancyResponseDto> {
        return safeApiCall(
            action = {
                val response = apiService.searchVacancies(
                    area = area,
                    industry = industry,
                    text = text,
                    salary = salary,
                    page = page,
                    onlyWithSalary = onlyWithSalary
                )

                val vacancies = response.vacancies ?: emptyList()

                VacancyResponseDto(
                    found = response.found,
                    pages = response.pages,
                    page = response.page,
                    vacancies = vacancies
                )
            },
            operationName = "searchVacancies"
        )
    }

    suspend fun getVacancyDetails(vacancyId: String): Result<VacancyDetailDto> {
        return safeApiCall(
            action = { apiService.getVacancyDetails(vacancyId) },
            operationName = "getVacancyDetails"
        )
    }
}
