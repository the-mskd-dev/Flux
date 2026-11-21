package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.catalog.domain.model.SyncKind
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep

/** Removes the Missing files right after the scan, whatever happens next. */
class MissingFilesStep(private val database: DatabaseRepository) : SyncStep {

    override fun isEnabled(context: SyncContext) = context.kind != SyncKind.LANGUAGE_CHANGE

    override suspend fun units(context: SyncContext) = 1

    override suspend fun run(context: SyncContext) {
        database.deleteMediasNotInFiles(context.scan.existingFiles(knownFiles = context.knownFiles))
        context.progress.complete()
    }

}
