package com.mskd.flux.features.catalog.domain.sync.enrichment

import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException

/**
 * TMDB seasons and episodes of the show of a folder. The files of a season that fails are Fichiers en échec.
 *
 * @param missingSeasonIsUnknown True when the files of a season the show does not have are Fichiers non reconnus
 * rather than Fichiers en échec: the user chose the show (Identification manuelle), so the season will never be found.
 */
class SeasonsEnrichment(
    private val api: ApiRepository,
    private val missingSeasonIsUnknown: Boolean = false,
) : Enrichment {

    private companion object { const val TAG = "SeasonsEnrichment" }

    override fun units(folder: CatalogFolder) = folder.files.mapNotNull { it.season }.distinct().size

    override suspend fun enrich(draft: FolderDraft) {

        val artwork = draft.artwork?.takeUnless { it.isUnknown || it.type != ContentType.SHOW } ?: return
        val files = draft.folder.files

        files.mapNotNull { it.season }.distinct().forEach { season ->

            val seasonFiles = files.filter { it.season == season }

            try {

                val seasonAndEpisodes = api.getSeasonAndEpisodes(
                    artworkId = artwork.id,
                    season = season,
                    files = seasonFiles
                )

                if (seasonAndEpisodes == null && missingSeasonIsUnknown) {
                    Trace.info(TAG, "No season $season in artworkId ${artwork.id}: its files are unknown")
                    draft.medias += seasonFiles.map { Episode(it) }
                    return@forEach
                }

                val (fetchedSeason, episodes) = seasonAndEpisodes ?: error("No details for season $season of ${artwork.id}")

                draft.seasons += fetchedSeason
                draft.medias += episodes

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Trace.error(TAG, "Fail to get season for artworkId ${artwork.id} - season $season", e)
                draft.failedFiles += seasonFiles.size
            }

        }

    }

}
