package ru.practicum.android.diploma.util

import android.text.Html
import android.text.Spanned

object VacancyDescriptionFormatter {

    fun formatDescription(description: String?): Spanned? {
        if (description.isNullOrBlank()) return null

        var text = description
            .replace("\r\n", "\n")
            .replace("\r", "\n")

        // Разбиваем на строки
        val lines = text.split("\n")
        val formattedLines = mutableListOf<String>()
        var isFirstHeader = true

        for (line in lines) {
            val trimmed = line.trim()

            when {
                // Заголовок (заканчивается на :)
                trimmed.endsWith(":") && trimmed.length > 1 -> {
                    if (!isFirstHeader) {
                        formattedLines.add("<br/>")
                    }
                    formattedLines.add("<b>$trimmed</b>")
                    isFirstHeader = false
                }

                // Элемент списка (начинается с - или •)
                trimmed.startsWith("-") || trimmed.startsWith("•") -> {
                    val bulletText = trimmed.drop(1).trim()
                    formattedLines.add("• $bulletText")
                }

                trimmed.matches(Regex("^\\d+\\..*")) || trimmed.endsWith(";") -> {
                    formattedLines.add("• $trimmed")
                }

                // Пустая строка
                trimmed.isEmpty() -> {
                }

                // Обычный текст
                else -> {
                    formattedLines.add(trimmed)
                }
            }
        }

        val result = formattedLines.joinToString("<br/>")
        @Suppress("DEPRECATION")
        return Html.fromHtml(result)
    }
}
