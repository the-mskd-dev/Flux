package com.mskd.flux.features.catalog.domain.sync.enrichment

import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException

/** TMDB seasons and episodes of the show of a folder. The files of a season that fails are Fichiers en échec. */
class SeasonsEnrichment(private val api: ApiRepository) : Enrichment {

    private companion object { const val TAG = "SeasonsEnrichment" }

    override fun units(folder: CatalogFolder) = folder.files.mapNotNull { it.season }.distinct().size

    override suspend fun enrich(draft: FolderDraft) {

        val artwork = draft.artwork?.takeUnless { it.isUnknown || it.type != ContentType.SHOW } ?: return
        val files = draft.folder.files

        files.mapNotNull { it.season }.distinct().forEach { season ->

            val seasonFiles = files.filter { it.season == season }

            try {

                val (fetchedSeason, episodes) = api.getSeasonAndEpisodes(
                    artworkId = artwork.id,
                    season = season,
                    files = seasonFiles
                ) ?: error("No details for season $season of ${artwork.id}")

                draft.seasons += fetchedSeason
                draft.medias += episodes

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Trace.error(TAG, "Fail to get season for artworkId ${artwork.id} - season $season", e)
                draft.failedFiles += seasonFiles
            }

        }

    }

}
