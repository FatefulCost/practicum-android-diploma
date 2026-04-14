package ru.practicum.android.diploma.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [VacancyEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vacancyDao(): VacancyDao

    companion object {
        // Добавляем миграцию для обновления схемы БД
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Добавляем новые столбцы
                try {
                    database.execSQL("ALTER TABLE favorite_vacancies ADD COLUMN description TEXT")
                } catch (e: Exception) { /* column already exists */
                }

                try {
                    database.execSQL("ALTER TABLE favorite_vacancies ADD COLUMN skillsJson TEXT")
                } catch (e: Exception) { /* column already exists */
                }

                try {
                    database.execSQL("ALTER TABLE favorite_vacancies ADD COLUMN contactsName TEXT")
                } catch (e: Exception) { /* column already exists */
                }

                try {
                    database.execSQL("ALTER TABLE favorite_vacancies ADD COLUMN contactsEmail TEXT")
                } catch (e: Exception) { /* column already exists */
                }

                try {
                    database.execSQL("ALTER TABLE favorite_vacancies ADD COLUMN contactsPhone TEXT")
                } catch (e: Exception) { /* column already exists */
                }

                try {
                    database.execSQL("ALTER TABLE favorite_vacancies ADD COLUMN vacancyUrl TEXT")
                } catch (e: Exception) { /* column already exists */
                }

                // Если есть старое поле skills, копируем данные в skillsJson
                try {
                    database.execSQL("UPDATE favorite_vacancies SET skillsJson = skills WHERE skills IS NOT NULL")
                } catch (e: Exception) { /* column skills doesn't exist */
                }
            }
        }
    }
}
