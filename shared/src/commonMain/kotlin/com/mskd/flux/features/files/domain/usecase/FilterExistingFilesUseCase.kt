package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

interface FilterExistingFilesUseCase {
    suspend operator fun invoke(files: List<UserFile>) : List<UserFile>
}

class FilterExistingFilesUseCaseImpl(
    private val sourcesProvider: SourcesProvider
) : FilterExistingFilesUseCase {
    override suspend fun invoke(files: List<UserFile>): List<UserFile> = coroutineScope {
        sourcesProvider.getSources()
            .map { source -> async { source.filterExistingFiles(files = files) } }
            .awaitAll()
            .flatten()
    }
}