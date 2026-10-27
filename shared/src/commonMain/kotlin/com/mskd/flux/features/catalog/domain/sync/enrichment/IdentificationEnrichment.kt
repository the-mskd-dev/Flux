package com.mskd.flux.features.catalog.domain.sync.enrichment

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException

/** TMDB search of the Artwork of a folder: the TMDB ID has priority, then the name. */
class IdentificationEnrichment(private val api: ApiRepository) : Enrichment {

    private companion object { const val TAG = "IdentificationEnrichment" }

    override fun units(folder: CatalogFolder) = 1

    override suspend fun enrich(draft: FolderDraft) {

        val folder = draft.folder
        val file = folder.files.first()

        val artwork = getArtworkById(folder = folder, file = file) ?: api.getArtwork(file = file)

        draft.artwork = artwork

        // Fichiers non reconnus: saved as Médias without known Artwork
        if (artwork.isUnknown) draft.medias = folder.files.map { Episode(it) }

    }

    private suspend fun getArtworkById(folder: CatalogFolder, file: UserFile): Artwork? {

        val tmdbId = folder.tmdbId ?: return null
        val type = if (file.isEpisode) ContentType.SHOW else ContentType.MOVIE

        return try {
            api.getArtworkById(id = tmdbId, type = type)
                ?.takeUnless { it.isUnknown }
                ?: run {
                    Trace.info(TAG, "Artwork $tmdbId not found by TMDB ID for ${file.name}, search by name")
                    null
                }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Trace.error(TAG, "Fail to get artwork by TMDB ID $tmdbId for ${file.name}, search by name", e)
            null
        }

    }

}
