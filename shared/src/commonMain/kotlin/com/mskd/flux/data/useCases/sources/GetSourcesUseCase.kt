package com.mskd.flux.data.useCases.sources

import com.mskd.flux.data.dataSources.CheckFolderAvailabilityDataSource
import com.mskd.flux.data.repository.ddb.sources.SourcesRepository
import com.mskd.flux.model.domain.files.FileSource
import com.mskd.flux.model.domain.files.UserFolder

class GetSourcesUseCase(
    val repository: SourcesRepository,
    val checkFolderDataSource: CheckFolderAvailabilityDataSource
) {

    suspend operator fun invoke() : List<UserFolder> {
        return repository.getFolders().map { folder ->
            if (folder.source == FileSource.LOCAL) {
                val currentStatus = checkFolderDataSource.isFolderAvailable(folder.path)
                folder.copy(status = currentStatus)
            } else {
                folder
            }
        }
    }

}