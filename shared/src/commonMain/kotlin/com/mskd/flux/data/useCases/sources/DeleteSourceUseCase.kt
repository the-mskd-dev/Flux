package com.mskd.flux.data.useCases.sources

import com.mskd.flux.data.repository.ddb.sources.SourcesRepository
import com.mskd.flux.model.domain.files.UserFolder

class DeleteSourceUseCase(
    val repository: SourcesRepository
) {

    suspend operator fun invoke(folder: UserFolder) {
        repository.deleteFolder(folder = folder)
    }

}