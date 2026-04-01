package ru.practicum.android.diploma.util

import android.text.Html
import android.text.Spanned

object VacancyDescriptionFormatter {

    fun formatDescription(description: String?): Spanned? {
        if (description.isNullOrBlank()) return null

        val lines = description
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .split("\n")

        val formattedLines = mutableListOf<String>()
        var isFirstHeader = true

        for (line in lines) {
            val trimmed = line.trim()
            when {
                isHeader(trimmed) -> {
                    addHeader(formattedLines, trimmed, isFirstHeader)
                    isFirstHeader = false
                }
                isBulletItem(trimmed) -> {
                    addBulletItem(formattedLines, trimmed)
                }
                trimmed.isNotEmpty() -> {
                    formattedLines.add(trimmed)
                }
            }
        }

        val result = formattedLines.joinToString("<br/>")
        @Suppress("DEPRECATION")
        return Html.fromHtml(result)
    }

    /**
     * Проверяет, является ли строка заголовком
     */
    private fun isHeader(line: String): Boolean {
        return line.endsWith(":") && line.length > 1
    }

    /**
     * Проверяет, является ли строка элементом списка
     */
    private fun isBulletItem(line: String): Boolean {
        return line.startsWith("-") || line.startsWith("•")
    }

    /**
     * Добавляет заголовок в список с отступом сверху
     */
    private fun addHeader(
        formattedLines: MutableList<String>,
        header: String,
        isFirstHeader: Boolean
    ) {
        if (!isFirstHeader && formattedLines.isNotEmpty()) {
            formattedLines.add("<br/>")
        }
        formattedLines.add("<b>$header</b>")
    }

    /**
     * Добавляет элемент списка с маркером
     */
    private fun addBulletItem(
        formattedLines: MutableList<String>,
        line: String
    ) {
        val bulletText = line.drop(1).trim()
        formattedLines.add("• $bulletText")
    }
}
