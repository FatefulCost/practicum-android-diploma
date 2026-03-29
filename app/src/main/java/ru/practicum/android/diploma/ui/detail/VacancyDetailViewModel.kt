package ru.practicum.android.diploma.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
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

    private val _navigationEvent = MutableLiveData<NavigationEvent?>()
    val navigationEvent: LiveData<NavigationEvent?> = _navigationEvent

    private var currentVacancy: VacancyDetailDto? = null
    private var currentVacancyId: String? = null

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
                    _vacancyState.value = classifyError(error)
                }
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

    private fun checkFavoriteStatus(vacancyId: String) {
        viewModelScope.launch {
            _isFavorite.value = repository.isFavorite(vacancyId)
        }
    }

    fun toggleFavorite() {
        val vacancy = currentVacancy ?: return
        viewModelScope.launch {
            val currentlyFavorite = _isFavorite.value ?: false
            if (currentlyFavorite) {
                repository.removeFromFavorites(vacancy.id)
                _isFavorite.value = false
            } else {
                repository.addToFavorites(vacancy)
                _isFavorite.value = true
            }
        }
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
