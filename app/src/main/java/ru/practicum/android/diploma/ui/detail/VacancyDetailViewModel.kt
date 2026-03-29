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

    private val _favoriteErrorEvent = MutableLiveData<FavoriteErrorEvent?>()
    val favoriteErrorEvent: LiveData<FavoriteErrorEvent?> = _favoriteErrorEvent

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

    // suspend, чтобы вызываться в контексте уже запущенной корутины
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
            } catch (e: Exception) {
                // Откатываем состояние при ошибке операции с БД
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
