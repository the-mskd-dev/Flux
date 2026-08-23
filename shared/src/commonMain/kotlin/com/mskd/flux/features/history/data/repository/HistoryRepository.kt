package com.mskd.flux.features.history.data.repository

import com.mskd.flux.features.history.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {

    suspend fun flow(): Flow<List<HistoryEntry>>

    suspend fun insert(entry: HistoryEntry)

    suspend fun delete(entry: HistoryEntry)

    suspend fun clear()

}