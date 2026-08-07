package com.mskd.flux.core.database.data.repository

import com.mskd.flux.core.database.data.DetailsDao
import com.mskd.flux.core.database.data.mappers.toDomain
import com.mskd.flux.core.database.data.mappers.toEntity
import com.mskd.flux.core.database.domain.repository.DetailsRepository
import com.mskd.flux.core.model.artwork.Genre
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class DetailsRepositoryImpl(private val dao: DetailsDao) : DetailsRepository {

    override suspend fun saveGenres(genres: List<Genre>) {
        dao.insertGenres(genres.map { it.toEntity() })
    }

    override fun flowGenres(): Flow<List<Genre>> {
        return dao.flowGenres().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getGenres(): List<Genre> {
        return dao.getGenres().map { it.toDomain() }
    }

}