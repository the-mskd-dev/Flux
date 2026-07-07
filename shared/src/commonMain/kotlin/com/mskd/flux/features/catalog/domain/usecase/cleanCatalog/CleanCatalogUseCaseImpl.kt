package com.mskd.flux.features.catalog.domain.usecase.cleanCatalog

import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase

internal class CleanCatalogUseCaseImpl(
    private val getDeviceFilesUseCase: GetDeviceFilesUseCase,
    private val database: DatabaseRepository
) : CleanCatalogUseCase {
    override suspend fun invoke() {

        val deviceFiles = getDeviceFilesUseCase()
        database.deleteMediasNotInFiles(deviceFiles)

    }
}