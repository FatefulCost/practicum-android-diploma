package ru.practicum.android.diploma.ui.detail

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.practicum.android.diploma.data.database.VacancyEntity
import ru.practicum.android.diploma.data.dto.ContactsDto
import ru.practicum.android.diploma.data.dto.EmployerDto
import ru.practicum.android.diploma.data.dto.EmploymentDto
import ru.practicum.android.diploma.data.dto.ExperienceDto
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.SalaryDto
import ru.practicum.android.diploma.data.dto.ScheduleDto
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.domain.repository.VacancyRepository
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class VacancyDetailViewModel(
    private val repository: VacancyRepository
) : ViewModel() {

    private val _vacancyState = MutableLiveData<VacancyDetailState>()
    val vacancyState: LiveData<VacancyDetailState> = _vacancyState

    private val _isFavorite = MutableLiveData<Boolean>(false)
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _favoriteErrorEvent = MutableLiveData<FavoriteErrorEvent?>()
    val favoriteErrorEvent: LiveData<FavoriteErrorEvent?> = _favoriteErrorEvent

    private val _navigationEvent = MutableLiveData<NavigationEvent?>()
    val navigationEvent: LiveData<NavigationEvent?> = _navigationEvent

    private var currentVacancy: VacancyDetailDto? = null
    private var currentVacancyId: String? = null
    private val gson = Gson()

    fun loadVacancy(vacancyId: String) {
        currentVacancyId = vacancyId
        _vacancyState.value = VacancyDetailState.Loading

        viewModelScope.launch {
            repository.getVacancyDetails(vacancyId)
                .onSuccess { vacancy ->
                    currentVacancy = vacancy
                    _vacancyState.value = VacancyDetailState.Success(vacancy)
                    checkFavoriteStatus(vacancyId)
                }
                .onFailure { error ->
                    // Если сеть недоступна, загружаем из БД
                    if (error is UnknownHostException || error is SocketTimeoutException) {
                        loadVacancyFromDatabase(vacancyId)
                    } else {
                        _vacancyState.value = classifyError(error)
                    }
                }
        }
    }

    // Загружаем вакансию из БД со всеми данными
    private fun loadVacancyFromDatabase(vacancyId: String) {
        viewModelScope.launch {
            val favorites = repository.getFavoriteVacancies()
            val favorite = favorites.find { it.id == vacancyId }

            if (favorite != null) {
                val cachedVacancy = buildVacancyDetailDtoFromEntity(favorite)
                currentVacancy = cachedVacancy
                _vacancyState.value = VacancyDetailState.Success(cachedVacancy)
                checkFavoriteStatus(vacancyId)
            } else {
                _vacancyState.value = VacancyDetailState.Error(VacancyDetailErrorType.NO_INTERNET)
            }
        }
    }

    private fun buildVacancyDetailDtoFromEntity(favorite: VacancyEntity): VacancyDetailDto {
        val skills = restoreSkillsFromJson(favorite.skillsJson)
        val contacts = buildContactsFromEntity(favorite)

        return VacancyDetailDto(
            id = favorite.id,
            name = favorite.name,
            description = favorite.description,
            salary = buildSalaryFromEntity(favorite),
            address = null,
            experience = favorite.experienceName?.let { ExperienceDto("", it) },
            schedule = favorite.scheduleName?.let { ScheduleDto("", it) },
            employment = favorite.employmentName?.let { EmploymentDto("", it) },
            contacts = contacts,
            employer = EmployerDto("", favorite.employerName, favorite.employerLogo),
            area = FilterAreaDto(0, favorite.areaName, null, null),
            skills = skills,
            url = favorite.vacancyUrl,
            industry = null
        )
    }

    private fun restoreSkillsFromJson(skillsJson: String?): List<String>? {
        return skillsJson?.let {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, type)
        }
    }

    private fun buildSalaryFromEntity(favorite: VacancyEntity): SalaryDto? {
        return if (favorite.salaryFrom != null || favorite.salaryTo != null) {
            SalaryDto(favorite.salaryFrom, favorite.salaryTo, favorite.salaryCurrency)
        } else {
            null
        }
    }

    private fun buildContactsFromEntity(favorite: VacancyEntity): ContactsDto? {
        val hasContacts = favorite.contactsName != null ||
            favorite.contactsEmail != null ||
            favorite.contactsPhone != null

        return if (hasContacts) {
            ContactsDto(
                id = null,
                name = favorite.contactsName,
                email = favorite.contactsEmail,
                phone = favorite.contactsPhone?.let { listOf(it) }
            )
        } else {
            null
        }
    }


    private fun classifyError(error: Throwable): VacancyDetailState.Error {
        return when (error) {
            is UnknownHostException,
            is SocketTimeoutException -> VacancyDetailState.Error(VacancyDetailErrorType.NO_INTERNET)
            is HttpException -> when (error.code()) {
                HTTP_NOT_FOUND -> VacancyDetailState.Error(VacancyDetailErrorType.NOT_FOUND)
                else -> VacancyDetailState.Error(VacancyDetailErrorType.SERVER_ERROR)
            }
            else -> VacancyDetailState.Error(VacancyDetailErrorType.SERVER_ERROR)
        }
    }

    private suspend fun checkFavoriteStatus(vacancyId: String) {
        _isFavorite.value = repository.isFavorite(vacancyId)
    }

    fun toggleFavorite() {
        val vacancy = currentVacancy ?: return
        val previousState = _isFavorite.value ?: false

        viewModelScope.launch {
            try {
                if (previousState) {
                    repository.removeFromFavorites(vacancy.id)
                } else {
                    repository.addToFavorites(vacancy)
                }
                _isFavorite.value = !previousState
            } catch (e: SQLiteException) {
                Log.e(TAG, "Failed to toggle favorite for vacancy ${vacancy.id}", e)
                _isFavorite.value = previousState
                _favoriteErrorEvent.value = if (previousState) {
                    FavoriteErrorEvent.RemoveError
                } else {
                    FavoriteErrorEvent.AddError
                }
            }
        }
    }

    fun clearFavoriteErrorEvent() {
        _favoriteErrorEvent.value = null
    }

    fun openShareDialog() {
        val vacancy = currentVacancy ?: return
        val url = vacancy.url ?: return
        _navigationEvent.value = NavigationEvent.ShareLink(url)
    }

    fun openEmail() {
        val email = currentVacancy?.contacts?.email ?: return
        _navigationEvent.value = NavigationEvent.OpenEmail(email)
    }

    fun openPhone() {
        val phone = currentVacancy?.contacts?.phone?.firstOrNull() ?: return
        _navigationEvent.value = NavigationEvent.OpenPhone(phone)
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    companion object {
        private const val HTTP_NOT_FOUND = 404
        private const val TAG = "VacancyDetailViewModel"
    }
}

enum class VacancyDetailErrorType {
    NO_INTERNET,
    NOT_FOUND,
    SERVER_ERROR
}

sealed class VacancyDetailState {
    object Loading : VacancyDetailState()
    data class Success(val vacancy: VacancyDetailDto) : VacancyDetailState()
    data class Error(val errorType: VacancyDetailErrorType) : VacancyDetailState()
}

sealed class NavigationEvent {
    data class ShareLink(val url: String) : NavigationEvent()
    data class OpenEmail(val email: String) : NavigationEvent()
    data class OpenPhone(val phone: String) : NavigationEvent()
}

sealed class FavoriteErrorEvent {
    object AddError : FavoriteErrorEvent()
    object RemoveError : FavoriteErrorEvent()
}
