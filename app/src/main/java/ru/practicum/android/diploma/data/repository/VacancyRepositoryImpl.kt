package ru.practicum.android.diploma.data.repository

import ru.practicum.android.diploma.data.database.VacancyDao
import ru.practicum.android.diploma.data.database.VacancyEntity
import ru.practicum.android.diploma.data.dto.AddressDto
import ru.practicum.android.diploma.data.dto.ContactsDto
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.data.dto.EmployerDto
import ru.practicum.android.diploma.data.dto.EmploymentDto
import ru.practicum.android.diploma.data.dto.ExperienceDto
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.data.dto.SalaryDto
import ru.practicum.android.diploma.data.dto.ScheduleDto
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.data.dto.VacancyResponseDto
import ru.practicum.android.diploma.domain.repository.VacancyRepository

private const val SALARY_FROM = 100_000
private const val SALARY_TO = 150_000
private const val CURRENCY_RUB = "RUB"
private const val COMPANY_NAME = "Test Company"
private const val COMPANY_ID = "1"
private const val AREA_NAME = "Москва"
private const val AREA_ID = 1
private const val VACANCY_ID = "test_1"
private const val VACANCY_NAME = "Android Developer"
private const val VACANCY_URL = "https://test.com"
private const val SKILL_1 = "Kotlin"
private const val SKILL_2 = "Android"

// Пока это заглушка — возвращает тестовые данные
class VacancyRepositoryImpl(
    private val networkClient: NetworkClient,
    private val vacancyDao: VacancyDao
) : VacancyRepository {

    // Заглушка для поиска
    override suspend fun searchVacancies(
        area: Int?,
        industry: Int?,
        text: String?,
        salary: Int?,
        page: Int,
        onlyWithSalary: Boolean
    ): Result<VacancyResponseDto> {
        // Если нет поискового запроса — возвращаем пустой результат
        if (text.isNullOrBlank()) {
            return Result.success(VacancyResponseDto(0, 0, 0, emptyList()))
        }

        // Создаем тестовую вакансию для отладки
        val testVacancy = VacancyDetailDto(
            id = VACANCY_ID,
            name = VACANCY_NAME,
            description = "Тестовая вакансия",
            salary = SalaryDto(from = SALARY_FROM, to = SALARY_TO, currency = CURRENCY_RUB),
            employer = EmployerDto(id = COMPANY_ID, name = COMPANY_NAME, logo = null),
            area = FilterAreaDto(id = AREA_ID, name = AREA_NAME, parentId = null, areas = emptyList()),
            skills = listOf(SKILL_1, SKILL_2),
            url = VACANCY_URL
        )

        return Result.success(
            VacancyResponseDto(
                found = 1,
                pages = 1,
                page = page,
                vacancies = listOf(testVacancy)
            )
        )
    }

    // Заглушка для деталей вакансии
    override suspend fun getVacancyDetails(vacancyId: String): Result<VacancyDetailDto> {
        val testVacancy = VacancyDetailDto(
            id = vacancyId,
            name = VACANCY_NAME,
            description = "Описание вакансии...",
            salary = SalaryDto(from = SALARY_FROM, to = SALARY_TO, currency = CURRENCY_RUB),
            employer = EmployerDto(id = COMPANY_ID, name = COMPANY_NAME, logo = null),
            area = FilterAreaDto(id = AREA_ID, name = AREA_NAME, parentId = null, areas = emptyList()),
            skills = listOf(SKILL_1, SKILL_2, "MVVM"),
            url = VACANCY_URL,
            experience = ExperienceDto("1", "1-3 года"),
            schedule = ScheduleDto("1", "Полный день"),
            employment = EmploymentDto("1", "Полная занятость"),
            address = AddressDto("Москва", "Тверская", "1", "Случайный полный адрес"),
            contacts = ContactsDto("1", "Иван", "ivan@test.com", listOf("+7 999 123-45-67")),
            industry = FilterIndustryDto(1, "IT")
        )
        return Result.success(testVacancy)
    }

    // Заглушки для избранного
    override suspend fun getFavoriteVacancies(): List<VacancyEntity> = emptyList()
    override suspend fun addToFavorites(vacancy: VacancyDetailDto) = Unit
    override suspend fun removeFromFavorites(vacancyId: String) = Unit
    override suspend fun isFavorite(vacancyId: String): Boolean = false
}
