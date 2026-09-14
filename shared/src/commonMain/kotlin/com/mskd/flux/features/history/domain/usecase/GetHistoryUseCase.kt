package com.mskd.flux.features.history.domain.usecase

import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetHistoryUseCase(
    private val repository: HistoryRepository,
    private val filterExistingFilesUseCase: FilterExistingFilesUseCase
) {

    operator fun invoke() : Flow<PersistentList<HistoryEntry>> {
        return repository.flow.map { entries ->

            val files = entries.map { it.media.file }

            val existingFiles = filterExistingFilesUseCase(files = files)
                .mapTo(hashSetOf()) { it.path }

            entries
                .filter { it.media.file.path in existingFiles }
                .sortedByDescending { it.timestamp }
                .toPersistentList()
        }.flowOn(Dispatchers.IO)

    }
}