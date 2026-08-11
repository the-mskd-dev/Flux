package com.mskd.flux.core.database.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mskd.flux.core.database.data.model.GenreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(seasons: List<GenreEntity>)

    @Query("SELECT * FROM genres")
    fun flowGenres() : Flow<List<GenreEntity>>

    @Query("SELECT COUNT(*) FROM genres")
    suspend fun getGenresCount() : Int
    
}