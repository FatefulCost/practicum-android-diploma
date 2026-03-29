package ru.practicum.android.diploma.ui.vacancydetail

import ru.practicum.android.diploma.data.dto.VacancyDetailDto

internal fun formatSalary(vacancy: VacancyDetailDto, noSalaryText: String): String {
    val salary = vacancy.salary ?: return noSalaryText
    val from = salary.from
    val to = salary.to
    val currency = formatCurrency(salary.currency)
    return when {
        from != null && to != null -> "от $from до $to $currency"
        from != null -> "от $from $currency"
        to != null -> "до $to $currency"
        else -> noSalaryText
    }
}

internal fun formatCurrency(currency: String?): String {
    return when (currency) {
        "RUR", "RUB" -> "₽"
        "USD" -> "$"
        "EUR" -> "€"
        "KZT" -> "₸"
        "UAH" -> "₴"
        "BYR", "BYN" -> "Br"
        else -> currency.orEmpty()
    }
}

internal fun buildEmploymentSchedule(vacancy: VacancyDetailDto): String {
    val parts = mutableListOf<String>()
    vacancy.employment?.name?.let { parts.add(it) }
    vacancy.schedule?.name?.let { parts.add(it) }
    return parts.joinToString(", ")
}
