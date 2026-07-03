package com.mskd.flux.features.sources.domain.useCase

import com.mskd.flux.features.sources.domain.validator.UserFolderValidator
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.model.domain.files.FileSource
import com.mskd.flux.features.sources.domain.model.UserFolder

class GetSourcesUseCase(
    val repository: SourcesRepository,
    val checkFolderDataSource: UserFolderValidator
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