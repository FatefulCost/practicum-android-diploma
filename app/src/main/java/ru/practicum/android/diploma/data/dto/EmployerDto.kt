package ru.practicum.android.diploma.data.dto

import com.google.gson.annotations.SerializedName

data class EmployerDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("logo_urls") val logoUrls: LogoUrlsDto?,
    @SerializedName("url") val url: String?,
    @SerializedName("alternate_url") val alternateUrl: String?
)

data class LogoUrlsDto(
    @SerializedName("90") val logo90: String?,
    @SerializedName("240") val logo240: String?
)
