package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import com.mskd.flux.features.files.domain.usecase.ScanSourcesUseCase

/** Scans the Sources and decides which files the catalogue step has to fetch. */
class ScanStep(
    private val database: DatabaseRepository,
    private val scanSourcesUseCase: ScanSourcesUseCase,
) : SyncStep {

    override fun units(context: SyncContext) = 1

    override suspend fun run(context: SyncContext) {

        val known = database.getMedias().map { it.file }
        val scan = scanSourcesUseCase()

        context.scan = scan
        context.knownFiles = known
        // Fichiers en échec are not persisted: they are not known, so a Synchro légère fetches them again
        context.filesToFetch = if (context.full) scan.files else {
            scan.files.filter { file -> known.none { it.path == file.path && it.name == file.name } }
        }
    }

}
