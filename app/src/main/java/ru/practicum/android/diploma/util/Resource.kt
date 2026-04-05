package ru.practicum.android.diploma.util

// Класс для представления состояния загрузки данных
// Используется для передачи состояния из ViewModel во Fragment

sealed class Resource<T>(
    val data: T? = null,
    val message: String = ""  // Изменяем String? на String с значением по умолчанию
) {
    class Success<T>(data: T) : Resource<T>(data = data)
    class Loading<T> : Resource<T>()
    class Error<T>(message: String) : Resource<T>(message = message)  // Здесь message: String
}
