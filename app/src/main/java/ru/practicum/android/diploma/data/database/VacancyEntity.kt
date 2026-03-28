package ru.practicum.android.diploma.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_vacancies")
data class VacancyEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String? = null,
    val employerName: String,
    val employerLogo: String? = null,
    val salaryFrom: Int? = null,
    val salaryTo: Int? = null,
    val salaryCurrency: String? = null,
    val areaName: String,
    val experienceName: String? = null,
    val scheduleName: String? = null,
    val employmentName: String? = null,
    val contactsEmail: String? = null,
    val contactsPhone: String? = null,
    val url: String? = null,
    val isFavorite: Boolean = true,
    val addedTimestamp: Long = System.currentTimeMillis()
)
