package com.mskd.flux.features.sources.domain.usecase

import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import kotlinx.coroutines.flow.Flow

class FlowSourcesUseCase(private val repository: SourcesRepository) {

    operator fun invoke() : Flow<List<UserFolder>> {
        return repository.flowFolders()
    }

}