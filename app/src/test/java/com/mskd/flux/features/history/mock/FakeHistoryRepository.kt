package com.mskd.flux.features.history.mock

import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeHistoryRepository(
    entries: Flow<List<HistoryEntry>>
) : HistoryRepository {

    constructor(entries: List<HistoryEntry>) : this(flowOf(entries))

    override val flow: Flow<List<HistoryEntry>> = entries

    override suspend fun insert(media: Media) {}

    override suspend fun delete(artworkId: Long) {}

    override suspend fun clear() {}
}