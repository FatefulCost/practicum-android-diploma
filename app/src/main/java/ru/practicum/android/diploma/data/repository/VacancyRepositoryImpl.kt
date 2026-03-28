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
        return networkClient.searchVacancies(
            area = area,
            industry = industry,
            text = text,
            salary = salary,
            page = page,
            onlyWithSalary = onlyWithSalary
        )
    }

    override suspend fun getVacancyDetails(vacancyId: String): Result<VacancyDetailDto> {
        return networkClient.getVacancyDetails(vacancyId)
    }

    override suspend fun getFavoriteVacancies(): List<VacancyEntity> {
        return vacancyDao.getAllFavorites()
    }

    override suspend fun addToFavorites(vacancy: VacancyDetailDto) {
        val entity = VacancyEntity(
            id = vacancy.id,
            name = vacancy.name,
            employerName = vacancy.employer?.name ?: "",
            employerLogo = vacancy.employer?.logo,
            salaryFrom = vacancy.salary?.from,
            salaryTo = vacancy.salary?.to,
            salaryCurrency = vacancy.salary?.currency,
            areaName = vacancy.area?.name ?: ""
        )
        vacancyDao.insert(entity)
    }

    override suspend fun removeFromFavorites(vacancyId: String) {
        vacancyDao.delete(vacancyId)
    }

    override suspend fun isFavorite(vacancyId: String): Boolean {
        return vacancyDao.isFavorite(vacancyId)
    }
}
