package com.mskd.flux.core.database.domain.repository

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mskd.flux.core.database.data.model.GenreEntity
import com.mskd.flux.core.model.artwork.Genre
import kotlinx.coroutines.flow.Flow

interface DetailsRepository {

    //region Genres

    suspend fun saveGenres(genres: List<Genre>)
    fun flowGenres() : Flow<List<Genre>>
    suspend fun getGenres() : List<Genre>

    //endregion

}