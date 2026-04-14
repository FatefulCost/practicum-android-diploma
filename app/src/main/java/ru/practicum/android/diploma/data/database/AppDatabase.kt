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
        private const val TABLE_NAME = "favorite_vacancies"
        private const val PREVIOUS_DATABASE = 3
        private const val CURRENT_DATABASE = 4

        // Добавляем миграцию для обновления схемы БД
        val MIGRATION_3_4 = object : Migration(PREVIOUS_DATABASE, CURRENT_DATABASE) {
            override fun migrate(database: SupportSQLiteDatabase) {
                addColumnIfNotExists(database, "description", "TEXT")
                addColumnIfNotExists(database, "skillsJson", "TEXT")
                addColumnIfNotExists(database, "contactsName", "TEXT")
                addColumnIfNotExists(database, "contactsEmail", "TEXT")
                addColumnIfNotExists(database, "contactsPhone", "TEXT")
                addColumnIfNotExists(database, "vacancyUrl", "TEXT")

                // Копируем данные из старого поля skills в skillsJson
                copyColumnData(database, "skills", "skillsJson")
            }

        }

        private fun addColumnIfNotExists(database: SupportSQLiteDatabase, columnName: String, columnType: String) {
            val cursor = database.query("PRAGMA table_info($TABLE_NAME)")
            var columnExists = false
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                if (name == columnName) {
                    columnExists = true
                    break
                }
            }
            cursor.close()

            if (!columnExists) {
                database.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $columnName $columnType")
            }
        }

        private fun copyColumnData(database: SupportSQLiteDatabase, fromColumn: String, toColumn: String) {
            val cursor = database.query("PRAGMA table_info($TABLE_NAME)")
            var fromExists = false
            var toExists = false

            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                when (name) {
                    fromColumn -> fromExists = true
                    toColumn -> toExists = true
                }
            }
            cursor.close()

            if (fromExists && toExists) {
                database.execSQL("UPDATE $TABLE_NAME SET $toColumn = $fromColumn WHERE $fromColumn IS NOT NULL")
            }
        }
    }
}
