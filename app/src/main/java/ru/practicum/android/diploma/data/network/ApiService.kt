package ru.practicum.android.diploma.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.data.dto.VacancyResponseDto

interface ApiService {

    @GET("areas")
    suspend fun getAreas(): List<FilterAreaDto>

    @GET("industries")
    suspend fun getIndustries(): List<FilterIndustryDto>

    @GET("vacancies")
    suspend fun searchVacancies(
        @Query("area") area: Int? = null,
        @Query("industry") industry: Int? = null,
        @Query("text") text: String? = null,
        @Query("salary") salary: Int? = null,
        @Query("page") page: Int = 0,
        @Query("per_page") perPage: Int = 20,
        @Query("only_with_salary") onlyWithSalary: Boolean = false
    ): VacancyResponseDto

    @GET("vacancies/{id}")
    suspend fun getVacancyDetails(
        @Path("id") id: String
    ): VacancyDetailDto
}
