package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Files seen when scanning the Sources, and the Sources that could not be read. */
class SourcesScan(
    val files: List<UserFile>,
    val unavailableSources: List<FilesDataSource>,
) {

    /**
     * The files of [knownFiles] that still exist: the ones seen by the scan, plus the ones that belong to a
     * Source indisponible. Any other known file is a Fichier manquant.
     */
    fun existingFiles(knownFiles: List<UserFile>): List<UserFile> =
        files + knownFiles.filter { file -> unavailableSources.any { it.contains(file) } }

}

interface ScanSourcesUseCase {
    suspend operator fun invoke(): SourcesScan
}

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
