package ru.practicum.android.diploma.ui.vacancy_detail

object DescriptionParser {

    fun extractSection(text: String, startMarker: String, endMarker: String?): String? {
        val startIndex = text.indexOf(startMarker)
        if (startIndex == -1) return null

        val contentStart = startIndex + startMarker.length
        val endIndex = endMarker?.let { text.indexOf(it, contentStart) } ?: text.length

        if (endIndex == -1) return null

        return text.substring(contentStart, endIndex).trim()
    }

    fun parseDescription(description: String?): DescriptionSections {
        val text = description ?: ""
        return DescriptionSections(
            responsibilities = extractSection(text, "Обязанности", "Требования"),
            requirements = extractSection(text, "Требования", "Условия"),
            conditions = extractSection(text, "Условия", null)
        )
    }

    data class DescriptionSections(
        val responsibilities: String?,
        val requirements: String?,
        val conditions: String?
    )
}
