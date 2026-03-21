package ru.practicum.android.diploma.data.dto

import com.google.gson.annotations.SerializedName

data class VacancyDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("salary") val salary: SalaryDto?,
    @SerializedName("address") val address: AddressDto?,
    @SerializedName("experience") val experience: ExperienceDto?,
    @SerializedName("schedule") val schedule: ScheduleDto?,
    @SerializedName("employment") val employment: EmploymentDto?,
    @SerializedName("contacts") val contacts: ContactsDto?,
    @SerializedName("employer") val employer: EmployerDto,
    @SerializedName("area") val area: FilterAreaDto,
    @SerializedName("skills") val skills: List<String>?,
    @SerializedName("url") val url: String?,
    @SerializedName("industry") val industry: FilterIndustryDto?
)
