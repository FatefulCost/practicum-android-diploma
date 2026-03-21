package ru.practicum.android.diploma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface VacancyDao {

    @Query("SELECT * FROM favorite_vacancies ORDER BY addedTimestamp DESC")
    suspend fun getAllFavorites(): List<VacancyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vacancy: VacancyEntity)

    @Query("DELETE FROM favorite_vacancies WHERE id = :vacancyId")
    suspend fun delete(vacancyId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_vacancies WHERE id = :vacancyId)")
    suspend fun isFavorite(vacancyId: String): Boolean
}
