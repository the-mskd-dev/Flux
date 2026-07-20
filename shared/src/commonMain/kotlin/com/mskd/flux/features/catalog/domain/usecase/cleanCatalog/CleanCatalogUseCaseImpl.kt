package com.mskd.flux.features.catalog.domain.usecase.cleanCatalog

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase

class CleanCatalogUseCaseImpl(
    private val getDeviceFilesUseCase: GetDeviceFilesUseCase,
    private val database: DatabaseRepository
) : CleanCatalogUseCase {
    override suspend fun invoke() {

        val deviceFiles = getDeviceFilesUseCase()
        database.deleteMediasNotInFiles(deviceFiles)

    }
}