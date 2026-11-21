package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.catalog.domain.model.SyncType
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import kotlinx.coroutines.flow.first

/** Moves the NSFW Artwork to the Private Folder when it is enabled and includes them. */
class NsfwPrivateStep(
    private val database: DatabaseRepository,
    private val privateFolder: PrivateFolderDataStore,
) : SyncStep {

    override fun isEnabled(context: SyncContext) = context.type != SyncType.LANGUAGE_CHANGE

    override suspend fun units(context: SyncContext) = 1

    override suspend fun run(context: SyncContext) {

        val state = privateFolder.flow.first()

        if (state.enabled && state.includeNsfw) database.setNsfwArtworksPrivate()

        context.progress.complete()

    }

}
