package ru.practicum.android.diploma.ui.vacancydetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.domain.repository.VacancyRepository
import ru.practicum.android.diploma.util.Resource

class VacancyDetailViewModel(
    private val vacancyRepository: VacancyRepository
) : ViewModel() {

    private val _vacancyDetails = MutableLiveData<Resource<VacancyDetailDto>>()
    val vacancyDetails: LiveData<Resource<VacancyDetailDto>> = _vacancyDetails

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _shareUrl = MutableLiveData<String?>()
    val shareUrl: LiveData<String?> = _shareUrl

    fun loadVacancyDetails(vacancyId: String) {
        viewModelScope.launch {
            _vacancyDetails.value = Resource.Loading()
            val result = vacancyRepository.getVacancyDetails(vacancyId)

            result.fold(
                onSuccess = { vacancy ->
                    _vacancyDetails.value = Resource.Success(vacancy)
                    checkIsFavorite(vacancyId)
                },
                onFailure = { error ->
                    _vacancyDetails.value = Resource.Error(error.message ?: "Ошибка загрузки вакансии")
                }
            )
        }
    }

    private fun checkIsFavorite(vacancyId: String) {
        viewModelScope.launch {
            val favorite = vacancyRepository.isFavorite(vacancyId)
            _isFavorite.value = favorite
        }
    }

    fun toggleFavorite(vacancyId: String) {
        viewModelScope.launch {
            val currentFavorite = _isFavorite.value ?: false
            if (currentFavorite) {
                vacancyRepository.removeFromFavorites(vacancyId)
                _isFavorite.value = false
            } else {
                val vacancy = _vacancyDetails.value?.data
                vacancy?.let {
                    vacancyRepository.addToFavorites(it)
                    _isFavorite.value = true
                }
            }
        }
    }

    fun shareVacancy() {
        val vacancy = _vacancyDetails.value?.data
        vacancy?.let {
            _shareUrl.value = it.url
        }
    }

    fun onShareCompleted() {
        _shareUrl.value = null
    }
}
