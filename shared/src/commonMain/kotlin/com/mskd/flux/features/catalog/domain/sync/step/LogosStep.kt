package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

/**
 * Gives their Logo to the Artwork never searched for one (see [Artwork.logoPath]), in parallel within the TMDB call limit,
 * saving each Logo as soon as it is found. A failed lookup leaves the Artwork unsearched, retried at the next Synchro:
 * it is no Fichier en échec and never stops the Synchro.
 */
class LogosStep(
    private val database: DatabaseRepository,
    private val api: ApiRepository,
    private val tokenDataStore: TokenDataStore,
) : SyncStep {

    private companion object { const val TAG = "LogosStep" }

    /** The Artwork whose files the catalogue step fetches again get their Logo from the details call, not from this step. */
    override suspend fun units(context: SyncContext): Int {
        val fetchedPaths = context.filesToFetch.map { it.path }.toSet()
        val refreshedIds = database.getMedias().filter { it.file.path in fetchedPaths }.map { it.artworkId }.toSet()
        return artworksToSearch().count { it.id !in refreshedIds }
    }

    override suspend fun run(context: SyncContext) {

        // The catalogue step may have saved Artwork without Logo since the declaration: the progress widens by itself
        supervisorScope {
            artworksToSearch().map { artwork ->
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
    private suspend fun artworksToSearch(): List<Artwork> =
        if (!tokenDataStore.tokenIsAvailable()) emptyList()
        else database.getArtworks(includePrivates = true).filter { it.logoPath == null && !it.isUnknown }

}
