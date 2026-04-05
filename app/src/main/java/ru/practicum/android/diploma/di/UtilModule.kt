package ru.practicum.android.diploma.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.practicum.android.diploma.util.NetworkUtils

val utilModule = module {
    single { NetworkUtils(androidContext()) }
}
