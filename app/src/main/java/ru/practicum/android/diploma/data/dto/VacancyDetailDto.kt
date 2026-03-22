package ru.practicum.android.diploma.data.dto

import com.google.gson.annotations.SerializedName

data class VacancyDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("salary") val salary: SalaryDto?,
    @SerializedName("address") val address: AddressDto? = null,
    @SerializedName("experience") val experience: ExperienceDto? = null,
    @SerializedName("schedule") val schedule: ScheduleDto? = null,
    @SerializedName("employment") val employment: EmploymentDto? = null,
    @SerializedName("contacts") val contacts: ContactsDto? = null,
    @SerializedName("employer") val employer: EmployerDto,
    @SerializedName("area") val area: FilterAreaDto,
    @SerializedName("skills") val skills: List<String>?,
    @SerializedName("url") val url: String?,
    @SerializedName("industry") val industry: FilterIndustryDto? = null
)
