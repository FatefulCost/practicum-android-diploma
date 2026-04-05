package ru.practicum.android.diploma.util

sealed class Resource<T>(
    val data: T? = null,
    val message: String = ""
) {
    class Success<T>(data: T) : Resource<T>(data = data)
    class Loading<T> : Resource<T>()
    class Error<T>(message: String) : Resource<T>(message = message)
}
