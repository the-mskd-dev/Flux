package com.mskd.flux.features.files.data.usecase

import com.mskd.flux.features.files.domain.model.SourcesScan
import com.mskd.flux.features.files.domain.usecase.ScanSourcesUseCase
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ScanSourcesUseCaseImpl(
    private val sourcesProvider: SourcesProvider
) : ScanSourcesUseCase {
    override suspend fun invoke(): SourcesScan = coroutineScope {
        val scans = sourcesProvider.getSources()
            .map { source -> async { source to if (source.isAvailable()) source.getFiles() else null } }
            .awaitAll()

        SourcesScan(
            files = scans.flatMap { it.second.orEmpty() }.distinctBy { it.path },
            unavailableSources = scans.filter { it.second == null }.map { it.first },
        )
    }
}
