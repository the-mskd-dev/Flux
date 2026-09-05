package com.mskd.flux.features.history.data.repository

import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.history.data.dao.HistoryDao
import com.mskd.flux.features.history.data.mapper.toDomain
import com.mskd.flux.features.history.data.mapper.toHistoryEntity
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepositoryImpl(private val dao: HistoryDao) : HistoryRepository {

    override val flow: Flow<List<HistoryEntry>> = dao.flow()
        .map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(media: Media) {
        dao.upsert(entity = media.toHistoryEntity())
    }

    override suspend fun delete(artworkId: Long) {
        dao.delete(artworkId = artworkId)
    }

    override suspend fun clear() {
        dao.clear()
    }

}