package ru.practicum.android.diploma.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.database.VacancyEntity
import ru.practicum.android.diploma.domain.repository.VacancyRepository

class FavoritesViewModel(
    private val repository: VacancyRepository
) : ViewModel() {

    private val _favoritesState = MutableLiveData<FavoritesState>()
    val favoritesState: LiveData<FavoritesState> = _favoritesState

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        _favoritesState.value = FavoritesState.Loading
        viewModelScope.launch {
            val favorites = repository.getFavoriteVacancies()
            _favoritesState.value = if (favorites.isEmpty()) {
                FavoritesState.Empty
            } else {
                FavoritesState.Content(favorites)
            }
        }
    }
}

sealed class FavoritesState {
    object Empty : FavoritesState()
    object Loading : FavoritesState()
    data class Content(val vacancies: List<VacancyEntity>) : FavoritesState()
    data class Error(val message: String) : FavoritesState()
}
