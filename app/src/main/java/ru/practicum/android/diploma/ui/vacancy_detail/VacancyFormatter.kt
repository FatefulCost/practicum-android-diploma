package ru.practicum.android.diploma.ui.vacancy_detail

import ru.practicum.android.diploma.data.dto.SalaryDto

object VacancyFormatter {
    private const val GROUP_SIZE = 3

    fun formatSalary(salary: SalaryDto?): String {
        return when {
            salary == null -> "Зарплата не указана"
            salary.from != null && salary.to != null -> {
                "от ${formatNumber(salary.from)} до ${formatNumber(salary.to)} ${salary.currency ?: "₽"}"
            }
            salary.from != null -> {
                "от ${formatNumber(salary.from)} ${salary.currency ?: "₽"}"
            }
            salary.to != null -> {
                "до ${formatNumber(salary.to)} ${salary.currency ?: "₽"}"
            }
            else -> "Зарплата не указана"
        }
    }

    fun formatNumber(number: Int): String {
        return number.toString()
            .reversed()
            .chunked(GROUP_SIZE)
            .joinToString(" ")
            .reversed()
    }
}
