package ru.practicum.android.diploma.util

import ru.practicum.android.diploma.data.dto.SalaryDto
import java.text.NumberFormat
import java.util.Locale

object SalaryFormatter {

    // Превращает объект Salary в читаемую строку
    fun format(salary: SalaryDto?): String {
        if (salary == null) return "Зарплата не указана"

        val from = salary.from
        val to = salary.to
        val currency = salary.currency ?: ""

        return when {
            from != null && to != null -> "от ${formatNumber(from)} до ${formatNumber(to)} $currency"
            from != null -> "от ${formatNumber(from)} $currency"
            to != null -> "до ${formatNumber(to)} $currency"
            else -> "Зарплата не указана"
        }
    }

    private fun formatNumber(number: Int): String {
        return NumberFormat.getInstance(Locale.getDefault()).format(number)
    }
}
