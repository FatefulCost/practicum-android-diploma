package ru.practicum.android.diploma.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.practicum.android.diploma.ui.detail.VacancyDetailViewModel
import ru.practicum.android.diploma.ui.filter.FilterViewModel
import ru.practicum.android.diploma.ui.root.RootViewModel
import ru.practicum.android.diploma.ui.search.SearchViewModel

val viewModelModule = module {
    viewModel { RootViewModel() }

    viewModel { SearchViewModel(get(), get()) }

    viewModel { ru.practicum.android.diploma.ui.favorites.FavoritesViewModel() }

    viewModel { FilterViewModel(get()) }

    viewModel { VacancyDetailViewModel(get()) }
}
