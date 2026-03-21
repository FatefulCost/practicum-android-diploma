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

    suspend fun getAreas(): Result<List<FilterAreaDto>> {
        return try {
            Result.success(apiService.getAreas())
        } catch (e: HttpException) {
            // Ошибка HTTP (404, 500 и т.д.)
            Result.failure(e)
        } catch (e: SocketTimeoutException) {
            // Таймаут соединения
            Result.failure(e)
        } catch (e: UnknownHostException) {
            // Нет интернета или сервер недоступен
            Result.failure(e)
        } catch (e: IOException) {
            // Проблемы с вводом-выводом
            Result.failure(e)
        }
    }

    suspend fun getIndustries(): Result<List<FilterIndustryDto>> {
        return try {
            Result.success(apiService.getIndustries())
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

    suspend fun getVacancyDetails(vacancyId: String): Result<VacancyDetailDto> {
        return try {
            Result.success(apiService.getVacancyDetails(vacancyId))
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
}
