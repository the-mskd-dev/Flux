package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.features.catalog.domain.model.SyncType
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.progress.domain.ProgressTracker
import kotlinx.coroutines.flow.first

/** Moves the NSFW Artwork to the Private Folder when it is enabled and includes them. */
class NsfwPrivateStep(
    private val progressTracker: ProgressTracker,
    private val privateFolder: PrivateFolderDataStore,
) : SyncStep {

    override val syncTypes = setOf(SyncType.LIGHT, SyncType.FULL)

    override suspend fun units(context: SyncContext) = 1

    override suspend fun run(context: SyncContext) {

        val state = privateFolder.flow.first()

        if (state.enabled && state.includeNsfw) progressTracker.markNsfwArtworksPrivate()

        context.progress.complete()

    }

}
