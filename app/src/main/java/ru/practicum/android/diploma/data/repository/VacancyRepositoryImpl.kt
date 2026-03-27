package ru.practicum.android.diploma.data.repository

import ru.practicum.android.diploma.data.database.VacancyDao
import ru.practicum.android.diploma.data.database.VacancyEntity
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.data.dto.VacancyResponseDto
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.domain.repository.VacancyRepository

class VacancyRepositoryImpl(
    private val networkClient: NetworkClient,
    private val vacancyDao: VacancyDao
) : VacancyRepository {

    override suspend fun searchVacancies(
        area: Int?,
        industry: Int?,
        text: String?,
        salary: Int?,
        page: Int,
        onlyWithSalary: Boolean
    ): Result<VacancyResponseDto> {
        return networkClient.searchVacancies(area, industry, text, salary, page, onlyWithSalary)
    }

    override suspend fun getVacancyDetails(vacancyId: String): Result<VacancyDetailDto> {
        return networkClient.getVacancyDetails(vacancyId)
    }

    // Заглушки для избранного
    override suspend fun getFavoriteVacancies(): List<VacancyEntity> = emptyList()
    override suspend fun addToFavorites(vacancy: VacancyDetailDto) = Unit
    override suspend fun removeFromFavorites(vacancyId: String) = Unit
    override suspend fun isFavorite(vacancyId: String): Boolean = false
}
