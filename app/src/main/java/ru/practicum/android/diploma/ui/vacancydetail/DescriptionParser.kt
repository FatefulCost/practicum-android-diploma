package ru.practicum.android.diploma.ui.vacancydetail

object DescriptionParser {

    fun extractSection(text: String, startMarker: String, endMarker: String?): String? {
        val startIndex = text.indexOf(startMarker)
        if (startIndex == -1) {
            return null
        }

        val contentStart = startIndex + startMarker.length
        val endIndex = endMarker?.let { text.indexOf(it, contentStart) } ?: text.length

        return if (endIndex == -1) {
            null
        } else {
            text.substring(contentStart, endIndex).trim().takeIf { it.isNotEmpty() }
        }
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
