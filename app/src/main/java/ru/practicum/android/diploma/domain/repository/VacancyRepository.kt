package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.data.database.VacancyEntity
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.data.dto.VacancyResponseDto

interface VacancyRepository {

    // Поиск вакансий
    suspend fun searchVacancies(
        area: Int? = null,
        industry: Int? = null,
        text: String? = null,
        salary: Int? = null,
        page: Int = 0,
        onlyWithSalary: Boolean = false
    ): Result<VacancyResponseDto>

    // Получить детали вакансии
    suspend fun getVacancyDetails(vacancyId: String): Result<VacancyDetailDto>

    // Работа с избранным
    suspend fun getFavoriteVacancies(): List<VacancyEntity>
    suspend fun addToFavorites(vacancy: VacancyDetailDto)
    suspend fun removeFromFavorites(vacancyId: String)
    suspend fun isFavorite(vacancyId: String): Boolean
}
