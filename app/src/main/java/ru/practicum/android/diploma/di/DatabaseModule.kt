package ru.practicum.android.diploma.di

import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import ru.practicum.android.diploma.data.database.AppDatabase
import ru.practicum.android.diploma.data.database.VacancyDao

val databaseModule = module {
    single { provideAppDatabase(androidApplication()) }
    single { provideVacancyDao(get()) }
}

private fun provideAppDatabase(app: android.app.Application): AppDatabase {
    return Room.databaseBuilder(
        app,
        AppDatabase::class.java,
        "vacancy_database"
    )
        .addMigrations(AppDatabase.MIGRATION_3_4)
        .build()
}

private fun provideVacancyDao(database: AppDatabase): VacancyDao {
    return database.vacancyDao()
}
