package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import kotlinx.coroutines.flow.first

/** Moves the NSFW Artworks to the private folder when it is enabled and includes them. */
class NsfwPrivateStep(
    private val database: DatabaseRepository,
    private val privateFolder: PrivateFolderDataStore,
) : SyncStep {

    override fun units(context: SyncContext) = 1

    override suspend fun run(context: SyncContext) {

        val state = privateFolder.flow.first()

        if (state.enabled && state.includeNsfw) database.setNsfwArtworksPrivate()

        context.progress.complete()

    }

}
