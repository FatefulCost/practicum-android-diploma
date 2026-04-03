package ru.practicum.android.diploma.di

import android.content.Context
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val storageModule = module {
    // SharedPreferences
    single {
        androidContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    // Gson
    single { Gson() }

    // FilterStorage
    single { ru.practicum.android.diploma.data.storage.FilterStorage(androidContext()) }
}
