package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import com.mskd.flux.features.catalog.domain.sync.enrichment.Enrichment
import com.mskd.flux.features.catalog.domain.sync.enrichment.FolderDraft
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

/**
 * Applies the chain of [enrichments] to every folder of the files to fetch, in parallel, then saves the Catalogue.
 * A folder that fails never stops the others: its files are Fichiers en échec, not persisted.
 */
class CatalogStep(
    private val database: DatabaseRepository,
    private val enrichments: List<Enrichment>,
    private val dispatcher: CoroutineDispatcher,
) : SyncStep {

    private companion object {
        const val TAG = "CatalogStep"
        const val SAVE_UNITS = 1
    }

    override fun units(context: SyncContext) =
        context.foldersToFetch.sumOf { folder -> enrichments.sumOf { it.units(folder) } } + SAVE_UNITS

    override suspend fun run(context: SyncContext) {

        val drafts = supervisorScope {
            context.foldersToFetch.map { folder -> async(dispatcher) { process(folder, context) } }.awaitAll()
        }

        val saved = drafts.filter { it.isSavable }

        context.savedArtworks = saved.mapNotNull { it.artwork }
        context.failedFiles = drafts.flatMap { it.failedFiles }

        // Upsert: nothing is wiped, private flags and user state (by file path) are kept by the save
        database.saveArtworks(context.savedArtworks)
        database.saveSeasons(saved.flatMap { it.seasons })
        // The primary key of medias is (id, artworkId): never save twice the same media identity
        database.saveSyncedMedias(saved.flatMap { it.medias }.distinctBy { it.mediaId to it.artworkId })

        context.progress.complete(SAVE_UNITS)

    }

    private suspend fun process(folder: CatalogFolder, context: SyncContext): FolderDraft {

        val draft = FolderDraft(folder)
        var failed = false

        enrichments.forEach { enrichment ->

            if (!failed) {
                try {
                    enrichment.enrich(draft)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Trace.error(TAG, "Fail to fetch ${folder.files.first().name}", e)
                    failed = true
                    draft.artwork = null
                    draft.failedFiles += folder.files
                }
            }

            context.progress.complete(enrichment.units(folder))

        }

        return draft

    }
}
