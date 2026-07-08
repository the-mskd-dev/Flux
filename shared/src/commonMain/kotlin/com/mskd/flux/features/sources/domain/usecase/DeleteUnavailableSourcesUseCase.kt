package com.mskd.flux.features.sources.domain.usecase

import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.core.domain.model.files.FileSource
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.features.sources.domain.validator.UserFolderValidator

class DeleteUnavailableSourcesUseCase(
    val sources: SourcesRepository,
    val database: DatabaseRepository,
    val checkFolderDataSource: UserFolderValidator
) {

    suspend operator fun invoke() {
        val unavailableSources = sources.getFolders().filter {
            it.source == FileSource.LOCAL && checkFolderDataSource.isFolderAvailable(it.path) == UserFolder.Status.MISSING
        }
        sources.deleteFolders(folders = unavailableSources)
    }

}