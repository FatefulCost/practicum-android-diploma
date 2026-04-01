package ru.practicum.android.diploma.di

import org.koin.dsl.module
import ru.practicum.android.diploma.data.repository.FilterRepositoryImpl
import ru.practicum.android.diploma.data.repository.VacancyRepositoryImpl
import ru.practicum.android.diploma.domain.repository.FilterRepository
import ru.practicum.android.diploma.domain.repository.VacancyRepository

val repositoryModule = module {
    single<VacancyRepository> {
        VacancyRepositoryImpl(
            networkClient = get(),
            vacancyDao = get()
        )
    }

    single<FilterRepository> {
        FilterRepositoryImpl(
            networkClient = get(),
            sharedPreferences = get(),
            gson = get()
        )
    }
}
