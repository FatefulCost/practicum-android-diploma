package ru.practicum.android.diploma.ui.root

import androidx.lifecycle.ViewModel


//  ViewModel для RootActivity
//  Пока просто хранит состояние приложения
//  В будущем можно будет добавить остальную логику

class RootViewModel : ViewModel() {
    // Текущий выбранный экран
    var currentDestinationId = 0
}
