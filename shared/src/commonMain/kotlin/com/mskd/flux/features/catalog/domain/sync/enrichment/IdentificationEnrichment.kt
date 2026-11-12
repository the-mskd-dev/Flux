package com.mskd.flux.features.catalog.domain.sync.enrichment

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException

/**
 * Identification of the Artwork of a folder: the Manual identification has priority, then the TMDB ID in the name, then
 * the TMDB search by name. See docs/adr/0001-manual-identification-by-file-name-title.md.
 */
class IdentificationEnrichment(
    private val api: ApiRepository,
    private val database: DatabaseRepository,
) : Enrichment {

    private companion object { const val TAG = "IdentificationEnrichment" }

    override fun units(folder: CatalogFolder) = 1

    override suspend fun enrich(draft: FolderDraft) {

        val folder = draft.folder
        val file = folder.files.first()

        val artwork = getManuallyIdentifiedArtwork(folder = folder, file = file)
            ?: getArtworkById(folder = folder, file = file)
            ?: api.getArtwork(file = file)

        draft.artwork = artwork

        // Unrecognized files: saved as Media without known Artwork
        if (artwork.isUnknown) draft.medias = folder.files.map { Episode(it) }

    }

    /**
     * The manually identified Artwork of a known Media of the same type with the same (title, year) in its file name.
     * Every file of the folder must have that (title, year): a folder grouped by TMDB ID can mix file-name titles, and
     * then follows its TMDB ID.
     */
    private suspend fun getManuallyIdentifiedArtwork(folder: CatalogFolder, file: UserFile): Artwork? {

        val titleYear = folder.files.map { it.nameProperties.let { properties -> properties.title to properties.year } }.distinct().singleOrNull()
            ?: return null

        val media = database.getManuallyIdentifiedMedias().firstOrNull { media ->
            val properties = media.file.nameProperties
            media.file.isEpisode == file.isEpisode && (properties.title to properties.year) == titleYear
        } ?: return null

        return database.getArtwork(artworkId = media.artworkId)
            ?.also { Trace.info(TAG, "${file.name} follows the Manual identification of artwork ${it.id}") }

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
