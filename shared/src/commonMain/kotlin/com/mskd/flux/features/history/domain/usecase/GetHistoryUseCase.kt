package com.mskd.flux.features.history.domain.usecase

import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.features.sources.domain.extension.isAvailableFor
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class GetHistoryUseCase(
    private val repository: HistoryRepository,
    private val sourcesUseCase: FlowSourcesUseCase
) {

    operator fun invoke() : Flow<PersistentList<HistoryEntry>> {
        return combine(
            repository.flow,
            sourcesUseCase()
        ) { entries, sources ->

            entries
                .filter { sources.isAvailableFor(file = it.media.file) }
                .sortedByDescending { it.timestamp }
                .toPersistentList()

        }.flowOn(Dispatchers.IO)
    }
}