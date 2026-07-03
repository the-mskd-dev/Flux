package com.mskd.flux.data.useCases.sources

import com.mskd.flux.data.repository.ddb.sources.SourcesRepository
import com.mskd.flux.model.domain.files.FileSource
import com.mskd.flux.model.domain.files.UserFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FlowSourcesUseCase(
    val repository: SourcesRepository,
    val checkFolderUseCase: CheckFolderAvailabilityUseCase
) {

    operator fun invoke() : Flow<List<UserFolder>> {
        return repository.flowFolders()
            .map { folders ->
                folders.map { folder ->
                    if (folder.source == FileSource.LOCAL) {
                        val currentStatus = checkFolderUseCase(folder.path)
                        folder.copy(status = currentStatus)
                    } else {
                        folder
                    }
                }
            }
    }

}