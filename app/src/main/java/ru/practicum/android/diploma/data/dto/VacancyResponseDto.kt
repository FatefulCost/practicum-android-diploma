package ru.practicum.android.diploma.data.dto

import com.google.gson.annotations.SerializedName

data class VacancyResponseDto(
    @SerializedName("found") val found: Int,
    @SerializedName("pages") val pages: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("items") val vacancies: List<VacancyDetailDto>? = emptyList()
)
