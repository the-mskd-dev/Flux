package com.mskd.flux.features.catalog.domain.usecase.cleanCatalog

import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.features.files.domain.usecase.GetFilesUseCase

internal class CleanCatalogUseCaseImpl(
    private val getFilesUseCase: GetFilesUseCase,
    private val database: DatabaseRepository
) : CleanCatalogUseCase {
    override suspend fun invoke() {
        val allFiles = getFilesUseCase()
        database.deleteMediasNotInFiles(allFiles)
    }
}