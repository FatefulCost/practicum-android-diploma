package ru.practicum.android.diploma.di

import org.koin.dsl.module
import ru.practicum.android.diploma.data.database.AppDatabase

val databaseModule = module {
    single<AppDatabase> { AppDatabase() }
}
