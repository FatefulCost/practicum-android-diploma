package ru.practicum.android.diploma.ui.filter

object FilterSettingsHelper {

    /**
     * Проверить, есть ли активные фильтры
     */
    fun hasActiveFilters(settings: FilterSettings): Boolean {
        return settings.salary != null ||
            settings.onlyWithSalary ||
            settings.industryId != null ||
            settings.countryId != null ||
            settings.regionId != null
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
        settings: FilterSettings,
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
