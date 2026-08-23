package com.mskd.flux.features.history.domain.repository

import com.mskd.flux.features.history.data.dao.HistoryDao
import com.mskd.flux.features.history.data.mapper.toDomain
import com.mskd.flux.features.history.data.mapper.toEntity
import com.mskd.flux.features.history.data.repository.HistoryRepository
import com.mskd.flux.features.history.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepositoryImpl(private val dao: HistoryDao) : HistoryRepository {

    override suspend fun flow(): Flow<List<HistoryEntry>> {
        return dao.flow().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun insert(entry: HistoryEntry) {
        dao.insert(entry = entry.toEntity())
    }

    override suspend fun delete(entry: HistoryEntry) {
        dao.delete(artworkId = entry.artworkId)
    }

    override suspend fun clear() {
        dao.clear()
    }

}