package ru.practicum.android.diploma.domain.models

fun FilterSettings.hasActiveFilters(): Boolean {
    return salary != null ||
        onlyWithSalary ||
        industryId != null ||
        countryId != null ||
        regionId != null
}
