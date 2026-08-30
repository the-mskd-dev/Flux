package com.mskd.flux.features.history.domain.repository

import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.history.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {

    val flow: Flow<List<HistoryEntry>>

    suspend fun insert(media: Media)

    suspend fun delete(artworkId: Long)

    suspend fun clear()

}