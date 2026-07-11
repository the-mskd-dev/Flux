package com.mskd.flux.features.sources.domain.usecase

import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.repository.SourcesRepository

class DeleteSourceUseCase(
    val repository: SourcesRepository
) {

    suspend operator fun invoke(folder: UserFolder, deleteMedias: Boolean) {
        repository.deleteFolder(folder = folder, deleteMedias = deleteMedias)
    }

}