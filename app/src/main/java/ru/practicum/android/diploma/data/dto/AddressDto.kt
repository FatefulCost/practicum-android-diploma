package ru.practicum.android.diploma.data.dto

import com.google.gson.annotations.SerializedName

data class AddressDto(
    @SerializedName("city") val city: String?,
    @SerializedName("street") val street: String?,
    @SerializedName("building") val building: String?,
    @SerializedName("fullAddress") val fullAddress: String?
)
