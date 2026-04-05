// di/ViewModelModule.kt
package ru.practicum.android.diploma.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.practicum.android.diploma.ui.about.AboutViewModel
import ru.practicum.android.diploma.ui.detail.VacancyDetailViewModel
import ru.practicum.android.diploma.ui.favorites.FavoritesViewModel
import ru.practicum.android.diploma.ui.filter.FilterViewModel
import ru.practicum.android.diploma.ui.filter.industry.IndustrySelectionViewModel
import ru.practicum.android.diploma.ui.filter.location.RegionSelectionViewModel
import ru.practicum.android.diploma.ui.filter.location.WorkLocationViewModel
import ru.practicum.android.diploma.ui.root.RootViewModel
import ru.practicum.android.diploma.ui.search.SearchViewModel

val viewModelModule = module {
    viewModel { RootViewModel() }
    viewModel { SearchViewModel(get(), get(), get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { FilterViewModel(get(), get()) }
    viewModel { VacancyDetailViewModel(get()) }
    viewModel { WorkLocationViewModel(get()) }

    // RegionSelectionViewModel требует filterRepository
    viewModel { RegionSelectionViewModel(get()) }

    viewModel { IndustrySelectionViewModel() }
    viewModel { AboutViewModel() }
}
