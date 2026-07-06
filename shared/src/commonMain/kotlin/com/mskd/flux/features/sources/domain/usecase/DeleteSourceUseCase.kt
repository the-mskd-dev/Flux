package com.mskd.flux.features.sources.domain.usecase

import com.mskd.flux.features.catalog.domain.usecase.cleanCatalog.CleanCatalogUseCase
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.repository.SourcesRepository

class DeleteSourceUseCase(
    val repository: SourcesRepository,
    val cleanCatalogUseCase: CleanCatalogUseCase
) {

    suspend operator fun invoke(folder: UserFolder) {
        repository.deleteFolder(folder = folder)
        cleanCatalogUseCase()
    }

}