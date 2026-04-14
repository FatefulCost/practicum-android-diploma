package ru.practicum.android.diploma.data.repository

import com.google.gson.Gson
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

    private val gson = Gson()

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

    override suspend fun getFavoriteVacancies(): List<VacancyEntity> {
        return vacancyDao.getAllFavorites()
    }

    override suspend fun addToFavorites(vacancy: VacancyDetailDto) {
        // Сохраняем все данные, включая описание, навыки и контакты
        val skillsJson = vacancy.skills?.let { gson.toJson(it) }

        val entity = VacancyEntity(
            id = vacancy.id,
            name = vacancy.name,
            employerName = vacancy.employer.name,
            employerLogo = vacancy.employer.logo,
            salaryFrom = vacancy.salary?.from,
            salaryTo = vacancy.salary?.to,
            salaryCurrency = vacancy.salary?.currency,
            areaName = vacancy.area.name,
            experienceName = vacancy.experience?.name,
            scheduleName = vacancy.schedule?.name,
            employmentName = vacancy.employment?.name,
            description = vacancy.description,
            skillsJson = skillsJson,
            contactsName = vacancy.contacts?.name,
            contactsEmail = vacancy.contacts?.email,
            contactsPhone = vacancy.contacts?.phone?.firstOrNull(),
            vacancyUrl = vacancy.url
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
