package ru.practicum.android.diploma.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.domain.repository.VacancyRepository

class VacancyDetailViewModel(
    private val repository: VacancyRepository
) : ViewModel() {

    private val _vacancyState = MutableLiveData<VacancyDetailState>()
    val vacancyState: LiveData<VacancyDetailState> = _vacancyState

    private val _navigationEvent = MutableLiveData<NavigationEvent?>()
    val navigationEvent: LiveData<NavigationEvent?> = _navigationEvent

    private var currentVacancy: VacancyDetailDto? = null

    fun loadVacancy(vacancyId: String) {
        _vacancyState.value = VacancyDetailState.Loading

        viewModelScope.launch {
            repository.getVacancyDetails(vacancyId)
                .onSuccess { vacancy ->
                    currentVacancy = vacancy
                    _vacancyState.value = VacancyDetailState.Success(vacancy)
                }
                .onFailure { error ->
                    _vacancyState.value = VacancyDetailState.Error(error.message ?: "Unknown error")
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
}

sealed class VacancyDetailState {
    object Loading : VacancyDetailState()
    data class Success(val vacancy: VacancyDetailDto) : VacancyDetailState()
    data class Error(val message: String) : VacancyDetailState()
}

sealed class NavigationEvent {
    data class ShareLink(val url: String) : NavigationEvent()
    data class OpenEmail(val email: String) : NavigationEvent()
    data class OpenPhone(val phone: String) : NavigationEvent()
}
