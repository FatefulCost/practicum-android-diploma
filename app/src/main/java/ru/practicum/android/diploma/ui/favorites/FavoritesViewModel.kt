package ru.practicum.android.diploma.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel для экрана избранного
 *
 *  Пока это просто заглушка
 *  Хранит список избранных вакансий (пока пустой)
 *  Подготовка для будущей логики
 *
 *  В Epic 3 добавим:
 *  Загрузка избранного из Room
 *  Добавление/удаление из избранного
 *  Обновление списка
 */
class FavoritesViewModel : ViewModel() {

    // Состояние экрана избранного
    private val _favoritesState = MutableLiveData<FavoritesState>()
    val favoritesState: LiveData<FavoritesState> = _favoritesState

    init {
        // Начальное состояние — пусто (ничего не добавлено)
        _favoritesState.value = FavoritesState.Empty
    }

    /**
     * Загрузить список избранного
     * В Epic 3 здесь будет запрос в Room
     */
    fun loadFavorites() {
        // Пока просто показываем, что список пуст
        _favoritesState.value = FavoritesState.Empty
    }

    /**
     * Добавить в избранное
     * В Epic 3 здесь будет реальное добавление
     */
    fun addToFavorites(vacancyId: String) {
        // Заглушка
        _favoritesState.value = FavoritesState.Message("Добавлено в избранное")
        // Через секунду убираем сообщение
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (_favoritesState.value is FavoritesState.Message) {
                _favoritesState.value = FavoritesState.Empty
            }
        }, 2000)
    }

    /**
     * Удалить из избранного
     * В Epic 3 здесь будет реальное удаление
     */
    fun removeFromFavorites(vacancyId: String) {
        // Заглушка
        _favoritesState.value = FavoritesState.Message("Удалено из избранного")
        // Через секунду убираем сообщение
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (_favoritesState.value is FavoritesState.Message) {
                _favoritesState.value = FavoritesState.Empty
            }
        }, 2000)
    }
}

/**
 * Состояния экрана избранного
 */
sealed class FavoritesState {
    // Список пуст
    object Empty : FavoritesState()

    // Идет загрузка
    object Loading : FavoritesState()

    // Есть список вакансий (будет использоваться в Epic 3)
    // data class Content(val vacancies: List<VacancyEntity>) : FavoritesState()

    // Временное сообщение (для демонстрации)
    data class Message(val text: String) : FavoritesState()

    // Ошибка
    data class Error(val message: String) : FavoritesState()
}
