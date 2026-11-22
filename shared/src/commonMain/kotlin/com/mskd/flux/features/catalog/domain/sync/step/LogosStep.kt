package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.catalog.domain.model.SyncType
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

/**
 * Updates the Logo of Artwork in the Catalog (see [Artwork.logoPath]), in parallel within the TMDB call limit,
 * saving each Logo as soon as it is found.
 *
 * During a Language change, every Artwork except the Unknown Artwork and those with a custom Logo is searched again
 * in the new data language; otherwise only Artwork never searched before (`logoPath == null`) without a custom Logo
 * are queried. A failed lookup keeps the previous Logo, records no Failed file, and never stops the Sync.
 */
class LogosStep(
    private val database: DatabaseRepository,
    private val api: ApiRepository,
    private val tokenDataStore: TokenDataStore,
) : SyncStep {

    private companion object { const val TAG = "LogosStep" }

    override val syncTypes = setOf(SyncType.LIGHT, SyncType.FULL, SyncType.LANGUAGE_CHANGE)

    /** The Artwork whose files the catalog step fetches again get their Logo from the details call, not from this step. */
    override suspend fun units(context: SyncContext): Int {
        val artworks = artworksToSearch(context)
        val fetchedPaths = context.filesToFetch.map { it.path }.toSet()
        if (fetchedPaths.isEmpty()) return artworks.size
        val refreshedIds = database.getMedias().filter { it.file.path in fetchedPaths }.map { it.artworkId }.toSet()
        return artworks.count { it.id !in refreshedIds }
    }

    override suspend fun run(context: SyncContext) {

        // The catalog step may have saved Artwork without Logo since the declaration: the progress widens by itself
        supervisorScope {
            artworksToSearch(context).map { artwork ->
                async(Dispatchers.IO) {
                    search(artwork)
                    context.progress.complete()
                }
            }.awaitAll()
        }

    }

    private suspend fun search(artwork: Artwork) {
        try {
            val logoPath = api.getLogo(artworkId = artwork.id, type = artwork.type)
            database.setArtworkLogo(artworkId = artwork.id, logoPath = logoPath)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Trace.error(TAG, "Fail to search the Logo of ${artwork.title}", e)
        }
    }

    /** None without token: the lookups could only fail. */
    private suspend fun artworksToSearch(context: SyncContext): List<Artwork> {
        if (!tokenDataStore.tokenIsAvailable()) return emptyList()
        val refreshAll = context.type == SyncType.LANGUAGE_CHANGE
        return database.getArtworks(includePrivates = true)
            .filter { !it.isUnknown && it.customLogoPath.isNullOrBlank() && (refreshAll || it.logoPath == null) }
    }

}
