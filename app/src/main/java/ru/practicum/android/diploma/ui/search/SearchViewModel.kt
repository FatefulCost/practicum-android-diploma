package ru.practicum.android.diploma.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//  ViewModel для экрана поиска
//
//  Пока просто заглушка
//  Хранит текст поискового запроса, чтобы не терялся при повороте
//  Позже добавим логику
class SearchViewModel : ViewModel() {

    // Текст поискового запроса (сохраняется при повороте)
    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    // Состояние поиска (загрузка, результат, ошибка)
    // Пока просто заглушка, в Epic 1 будет настоящая логика
    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    init {
        // Начальное состояние — пусто
        _searchState.value = SearchState.Empty
    }

    /**
     * Обновить текст поискового запроса
     * Вызывается из фрагмента при вводе текста
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query

        // В Epic 1 здесь будет запускаться debounced поиск
        // Пока просто меняем состояние
        if (query.isNotBlank()) {
            _searchState.value = SearchState.HasQuery(query)
        } else {
            _searchState.value = SearchState.Empty
        }
    }

    /**
     * Очистить поисковый запрос
     */
    fun clearSearchQuery() {
        _searchQuery.value = ""
        _searchState.value = SearchState.Empty
    }

    /**
     * Запустить поиск (заглушка)
     * В Epic 1 здесь будет реальный поиск через репозиторий
     */
    fun performSearch() {
        val query = _searchQuery.value ?: return
        if (query.isNotBlank()) {
            // В Epic 1 здесь будет загрузка из сети
            _searchState.value = SearchState.Loading
            // Пока просто имитируем, что поиск не реализован
            _searchState.value = SearchState.NotImplemented
        }
    }
}

/**
 * Состояния экрана поиска
 */
sealed class SearchState {
    // Ничего не введено
    object Empty : SearchState()

    // Есть введенный текст (но поиск не запущен)
    data class HasQuery(val query: String) : SearchState()

    // Идет загрузка
    object Loading : SearchState()

    // Поиск еще не реализован (для Epic 0)
    object NotImplemented : SearchState()
}
