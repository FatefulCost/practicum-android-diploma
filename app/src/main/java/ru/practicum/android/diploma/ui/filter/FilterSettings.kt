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
) {

    /**
     * Проверить, есть ли активные фильтры
     */
    fun hasActiveFilters(): Boolean {
        return salary != null ||
            onlyWithSalary ||
            industryId != null ||
            countryId != null ||
            regionId != null
    }

    /**
     * Сбросить все настройки
     */
    fun reset(): FilterSettings {
        return FilterSettings()
    }

    /**
     * Применить фильтры к параметрам поиска
     */
    fun applyToSearchParams(
        areaId: Int?,
        industryId: Int?,
        salary: Int?,
        onlyWithSalary: Boolean
    ): Map<String, Any> {
        val params = mutableMapOf<String, Any>()

        areaId?.let { params["area"] = it }
        industryId?.let { params["industry"] = it }
        salary?.let { params["salary"] = it }
        if (onlyWithSalary) {
            params["only_with_salary"] = true
        }

        return params
    }
}
