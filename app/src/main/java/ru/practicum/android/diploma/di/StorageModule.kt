package ru.practicum.android.diploma.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val storageModule = module {
    // нужно для кэширования регионов и отраслей
    single {
        androidContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }
}
