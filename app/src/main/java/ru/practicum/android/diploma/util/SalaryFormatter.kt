package ru.practicum.android.diploma.util

import ru.practicum.android.diploma.data.dto.SalaryDto
import java.text.NumberFormat
import java.util.Locale

object SalaryFormatter {

    private const val SALARY_NOT_SPECIFIED = "Зарплата не указана"

    fun format(salary: SalaryDto?): String {
        if (salary == null) return SALARY_NOT_SPECIFIED

        val from = salary.from
        val to = salary.to
        val currency = salary.currency ?: ""

        return when {
            from != null && to != null -> "от ${formatNumber(from)} до ${formatNumber(to)} $currency"
            from != null -> "от ${formatNumber(from)} $currency"
            to != null -> "до ${formatNumber(to)} $currency"
            else -> SALARY_NOT_SPECIFIED
        }
    }

    fun format(from: Int?, to: Int?, currency: String?): String {
        val cur = currency ?: ""
        return when {
            from != null && to != null -> "от ${formatNumber(from)} до ${formatNumber(to)} $cur"
            from != null -> "от ${formatNumber(from)} $cur"
            to != null -> "до ${formatNumber(to)} $cur"
            else -> SALARY_NOT_SPECIFIED
        }
    }

    private fun formatNumber(number: Int): String {
        return NumberFormat.getInstance(Locale.getDefault()).format(number)
    }
}
