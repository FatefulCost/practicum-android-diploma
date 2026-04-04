package ru.practicum.android.diploma.ui.filter

data class FilterSettings(
    val salary: Int? = null,
    val onlyWithSalary: Boolean = false,
    val industryId: Int? = null,
    val industryName: String? = null,
    val countryId: Int? = null,
    val countryName: String? = null,
    val regionId: Int? = null,
    val regionName: String? = null
)
