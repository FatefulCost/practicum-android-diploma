package ru.practicum.android.diploma.data.dto

import com.google.gson.annotations.SerializedName

data class SalaryDto(
    @SerializedName("from") val from: Int?,
    @SerializedName("to") val to: Int?,
    @SerializedName("currency") val currency: String?
)
