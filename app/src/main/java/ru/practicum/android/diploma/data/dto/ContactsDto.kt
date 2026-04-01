package ru.practicum.android.diploma.data.dto

import com.google.gson.annotations.SerializedName

data class ContactsDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: List<String>?
)
