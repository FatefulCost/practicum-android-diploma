package ru.practicum.android.diploma.util

// Класс для представления состояния загрузки данных
// Используется для передачи состояния из ViewModel во Fragment

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {

    class Success<T>(data: T) : Resource<T>(data = data)

    class Error<T>(message: String, data: T? = null) : Resource<T>(message = message, data = data)

    class Loading<T> : Resource<T>()
}
