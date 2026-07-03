package com.mskd.flux.data.useCases.sources

import com.mskd.flux.data.repository.ddb.sources.SourcesRepository
import com.mskd.flux.model.domain.files.UserFolder
import kotlinx.coroutines.flow.Flow

class FlowSourcesUseCase(val repository: SourcesRepository) {

    operator fun invoke() : Flow<List<UserFolder>> {
        return repository.flowFolders()
    }

}