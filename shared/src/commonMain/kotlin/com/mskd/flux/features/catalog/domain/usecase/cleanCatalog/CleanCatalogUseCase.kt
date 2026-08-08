package com.mskd.flux.features.catalog.domain.usecase.cleanCatalog

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase

class CleanCatalogUseCase(
    private val getDeviceFilesUseCase: GetDeviceFilesUseCase,
    private val database: DatabaseRepository
) {
    suspend operator fun invoke() {

        val deviceFiles = getDeviceFilesUseCase()
        database.deleteMediasNotInFiles(deviceFiles)

    }
}