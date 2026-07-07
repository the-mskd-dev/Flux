package com.mskd.flux.features.catalog.domain.usecase.cleanCatalog

import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.core.domain.model.files.FileSource
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.repository.SourcesRepository

internal class CleanCatalogUseCaseImpl(
    private val sourcesRepository: SourcesRepository,
    private val getDeviceFilesUseCase: GetDeviceFilesUseCase,
    private val database: DatabaseRepository
) : CleanCatalogUseCase {
    override suspend fun invoke() {

        val deviceFiles = getDeviceFilesUseCase()

        val accessibleFolders = sourcesRepository
            .getFolders()
            .filter { it.status == UserFolder.Status.AVAILABLE }

        val inaccessibleFiles = deviceFiles
            .filter { file -> file.source == FileSource.SAF && accessibleFolders.none { file.path.startsWith(it.path) } }
            .toSet()

        database.deleteMediasNotInFiles(deviceFiles - inaccessibleFiles)

    }
}