package ru.practicum.android.diploma.data.dto

import com.google.gson.annotations.SerializedName

data class VacancyResponseDto(
    @SerializedName("found") val found: Int = 0,
    @SerializedName("pages") val pages: Int = 0,
    @SerializedName("page") val page: Int = 0,
    @SerializedName("items") val vacancies: List<VacancyDetailDto>? = emptyList()
)
