package com.mskd.flux.features.sources.domain.usecase

import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.features.sources.domain.model.UserFolder

class DeleteSourceUseCase(
    val repository: SourcesRepository
) {

    suspend operator fun invoke(folder: UserFolder) {
        repository.deleteFolder(folder = folder)
    }

}