package ru.practicum.android.diploma.data.repository

import ru.practicum.android.diploma.data.database.VacancyDao
import ru.practicum.android.diploma.data.database.VacancyEntity
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.data.dto.*
import ru.practicum.android.diploma.domain.repository.VacancyRepository

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
            id = "test_1",
            name = "Android Developer",
            description = "Тестовая вакансия",
            salary = SalaryDto(from = 100000, to = 150000, currency = "RUB"),
            employer = EmployerDto(id = "1", name = "Test Company", logo = null),
            area = FilterAreaDto(id = 1, name = "Москва", parentId = null, areas = emptyList()),
            skills = listOf("Kotlin", "Android"),
            url = "https://test.com"
        )

        return Result.success(VacancyResponseDto(1, 1, page, listOf(testVacancy)))
    }

    // Заглушка для деталей вакансии
    override suspend fun getVacancyDetails(vacancyId: String): Result<VacancyDetailDto> {
        val testVacancy = VacancyDetailDto(
            id = vacancyId,
            name = "Android Developer",
            description = "Описание вакансии...",
            salary = SalaryDto(from = 100000, to = 150000, currency = "RUB"),
            employer = EmployerDto(id = "1", name = "Test Company", logo = null),
            area = FilterAreaDto(id = 1, name = "Москва", parentId = null, areas = emptyList()),
            skills = listOf("Kotlin", "Android", "MVVM"),
            url = "https://test.com",
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
