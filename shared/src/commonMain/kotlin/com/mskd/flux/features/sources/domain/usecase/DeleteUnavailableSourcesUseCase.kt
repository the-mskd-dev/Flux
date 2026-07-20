package com.mskd.flux.features.sources.domain.usecase

import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.features.sources.domain.validator.UserFolderValidator

class DeleteUnavailableSourcesUseCase(
    val sources: SourcesRepository,
    val checkFolderDataSource: UserFolderValidator
) {

    suspend operator fun invoke() {
        val unavailableSources = sources.getFolders().filter {
            it.source != FileSource.LOCAL && !checkFolderDataSource.isFolderAvailable(it.path)
        }
        sources.deleteFolders(folders = unavailableSources, deleteMedias = true)
    }

}