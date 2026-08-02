package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

interface GetDeviceFilesUseCase {
    suspend operator fun invoke() : List<UserFile>
}

class GetDeviceFilesUseCaseImpl(
    private val sourcesProvider: SourcesProvider
) : GetDeviceFilesUseCase {
    override suspend fun invoke(): List<UserFile> = coroutineScope {
        sourcesProvider.getSources()
            .map { source -> async { source.getFiles() } }
            .awaitAll()
            .flatten()
    }
}