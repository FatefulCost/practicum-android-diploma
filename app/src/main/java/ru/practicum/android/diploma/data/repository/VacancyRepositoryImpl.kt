package ru.practicum.android.diploma.data.repository

import ru.practicum.android.diploma.data.database.VacancyDao
import ru.practicum.android.diploma.data.database.VacancyEntity
import ru.practicum.android.diploma.data.dto.AddressDto
import ru.practicum.android.diploma.data.dto.ContactsDto
import ru.practicum.android.diploma.data.dto.EmployerDto
import ru.practicum.android.diploma.data.dto.EmploymentDto
import ru.practicum.android.diploma.data.dto.ExperienceDto
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.data.dto.LogoUrlsDto
import ru.practicum.android.diploma.data.dto.SalaryDto
import ru.practicum.android.diploma.data.dto.ScheduleDto
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.data.dto.VacancyResponseDto
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.domain.repository.VacancyRepository

private const val SALARY_FROM = 100_000
private const val SALARY_TO = 150_000
private const val CURRENCY_RUB = "RUB"
private const val COMPANY_NAME = "Test Company"
private const val COMPANY_ID = "1"
private const val COMPANY_URL = "https://test-company.com"
private const val COMPANY_ALTERNATE_URL = "https://test-company.com/hh"
private const val AREA_NAME = "Москва"
private const val AREA_ID = 1
private const val VACANCY_ID = "test_1"
private const val VACANCY_NAME = "Android Developer"
private const val VACANCY_URL = "https://test.com/vacancy"
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
            employer = EmployerDto(
                id = COMPANY_ID,
                name = COMPANY_NAME,
                logoUrls = null,
                url = COMPANY_URL,
                alternateUrl = COMPANY_ALTERNATE_URL
            ),
            area = FilterAreaDto(id = AREA_ID, name = AREA_NAME, parentId = null, areas = emptyList()),
            skills = listOf(SKILL_1, SKILL_2),
            url = VACANCY_URL,
            address = null,
            experience = null,
            schedule = null,
            employment = null,
            contacts = null,
            industry = null
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
        // Создаем тестовые логотипы
        val logoUrls = LogoUrlsDto(
            logo90 = "https://test.com/logo90.png",
            logo240 = "https://test.com/logo240.png"
        )

        val testVacancy = VacancyDetailDto(
            id = vacancyId,
            name = VACANCY_NAME,
            description = "Описание вакансии...\n\n" +
                "Обязанности:\n" +
                "• Разработка новых функций приложения\n" +
                "• Поддержка и оптимизация существующего кода\n" +
                "• Участие в code review и планировании задач\n\n" +
                "Требования:\n" +
                "• Опыт разработки на Kotlin от 3 лет\n" +
                "• Глубокое знание Android SDK\n" +
                "• Понимание архитектурных паттернов (MVVM, Clean Architecture)\n\n" +
                "Условия:\n" +
                "• Оформление по ТК РФ\n" +
                "• Гибкий график работы\n" +
                "• ДМС со стоматологией",
            salary = SalaryDto(from = SALARY_FROM, to = SALARY_TO, currency = CURRENCY_RUB),
            employer = EmployerDto(
                id = COMPANY_ID,
                name = COMPANY_NAME,
                logoUrls = logoUrls,
                url = COMPANY_URL,
                alternateUrl = COMPANY_ALTERNATE_URL
            ),
            area = FilterAreaDto(id = AREA_ID, name = AREA_NAME, parentId = null, areas = emptyList()),
            skills = listOf(SKILL_1, SKILL_2, "MVVM", "Coroutines", "Flow", "Retrofit", "Room"),
            url = VACANCY_URL,
            experience = ExperienceDto(id = "1", name = "от 3 до 6 лет"),
            schedule = ScheduleDto(id = "1", name = "Полный день"),
            employment = EmploymentDto(id = "1", name = "Полная занятость"),
            address = AddressDto(
                city = "Москва",
                street = "Тверская",
                building = "1",
                fullAddress = "Москва, Тверская улица, 1"),
            contacts = ContactsDto(
                id = "1",
                name = "Иван Иванов",
                email = "ivan@test.com",
                phone = listOf("+7 999 123-45-67", "+7 999 765-43-21")
            ),
            industry = FilterIndustryDto(id = 1, name = "IT")
        )
        return Result.success(testVacancy)
    }

    // Заглушки для избранного
    override suspend fun getFavoriteVacancies(): List<VacancyEntity> = emptyList()
    override suspend fun addToFavorites(vacancy: VacancyDetailDto) = Unit
    override suspend fun removeFromFavorites(vacancyId: String) = Unit
    override suspend fun isFavorite(vacancyId: String): Boolean = false
}
