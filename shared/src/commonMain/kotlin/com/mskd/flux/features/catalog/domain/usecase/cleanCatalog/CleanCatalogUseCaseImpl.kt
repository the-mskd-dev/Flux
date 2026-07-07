package com.mskd.flux.features.catalog.domain.usecase.cleanCatalog

import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.core.domain.model.files.FileSource
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.repository.SourcesRepository

internal class CleanCatalogUseCaseImpl(
    private val getDeviceFilesUseCase: GetDeviceFilesUseCase,
    private val database: DatabaseRepository
) : CleanCatalogUseCase {
    override suspend fun invoke() {

        val deviceFiles = getDeviceFilesUseCase()
        database.deleteMediasNotInFiles(deviceFiles)

    }
}