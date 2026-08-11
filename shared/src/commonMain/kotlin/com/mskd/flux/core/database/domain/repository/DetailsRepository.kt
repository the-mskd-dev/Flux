package com.mskd.flux.core.database.domain.repository

import com.mskd.flux.core.model.artwork.Genre
import kotlinx.coroutines.flow.Flow

interface DetailsRepository {

    //region Genres

    suspend fun saveGenres(genres: List<Genre>)
    fun flowGenres() : Flow<List<Genre>>
    suspend fun getGenresCount() : Int

    //endregion

}